/*
 *  Copyright 2014 Carnegie Mellon University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package edu.cmu.lti.oaqa.util;

import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;

import java.util.*;



/**
 * Solr based answer evidence collector.
 * 
 * <p>Created from Leo's NTCIR Hypothesis scorer, which itself
 * originated from Alkesh's simpler Solr scorer.</p>
 *
 * @author Leonid Boytsov
 * @author Alkesh Patel
 */

public class SolrAnswerCandidateEvidencer  {
  static boolean DEBUG_INFO = true;

  SolrServer mSolrServer = null;
  
  String     mServerUrl;
  String     mCoreName;
  String     mFieldName;

  int   mMatchPct        = 80;  // seems to be a good default value
  float mSlopeCoeff      = 1.5f;// seems to be a good default value
  int   mTopSearchResult = 10;  // seems to be a good default value
  
  float mDiscountMult    = 0.5f; // seems to be a good default value
  
  String[] stopwList = new String[] { "'", "word", "a", "about", "also",
      "an", "and", "another", "any", "are", "as", "at", "back", "be",
      "because", "been", "being", "but", "by", "can", "could", "did",
      "do", "each", "end", "even", "for", "from", "get", "go", "had",
      "have", "he", "her", "here", "his", "how", "i", "if", "in", "into",
      "is", "it", "just", "may", "me", "might", "much", "must", "my",
      "no", "not", "of", "off", "on", "only", "or", "other", "our",
      "out", "should", "so", "some", "still", "such", "than", "that",
      "the", "their", "them", "then", "there", "these", "they", "this",
      "those", "to", "too", "try", "two", "under", "up", "us", "was",
      "we", "were", "what", "when", "where", "which", "while", "who",
      "why", "will", "with", "within", "without", "would", "you", "your",
      "www", "com", "org", "edu", "net", "en" };

  HashSet<String> mStopWordHash;
  
  /**
   * @param serverUrl			URL of the server.
   * @param coreName			Name of the core.
   * @param fieldName		 	Search field name.
   * @param matchPct			Percentage of matching words, 
   * 							use 100 to require all non-stop words to be present.
   * @param slopeCoeff			Slope coefficient for Solr phrase queries.
   * @param topSearchResult		Number of top results that participate in computation of the discounted score.
   * @param discountMult		A multiplier that used to compute the discounted score.
   */
  public SolrAnswerCandidateEvidencer(String serverUrl, String coreName, String fieldName,
                               int matchPct, float slopeCoeff, int topSearchResult,
                               float discountMult) {
  
    mSolrServer = new HttpSolrServer(serverUrl + coreName);
  
    mFieldName = fieldName;
    
    mMatchPct = matchPct;
    mSlopeCoeff = slopeCoeff;
    mTopSearchResult = topSearchResult;
    mDiscountMult = discountMult;
  
    mStopWordHash = new HashSet<String>();
    for (String s : stopwList)
      mStopWordHash.add(s);  
  }
  
  class QueryParseRes {
	  String query;

	  int wordQty;

	  public QueryParseRes(String query, int wordQty) {
		  super();
		  this.query = query;
		  this.wordQty = wordQty;
	  }
  }
  
  public EvidencingResult scoreAssertion(String assertionText) throws SolrServerException {
    return scoreQuery(assertionText, 
    						mFieldName, 
    						mMatchPct , 
    						mSlopeCoeff,
    						mTopSearchResult);
  }
  
  ArrayList<String> getQueryWords(String question) {
    String[] qWords = question.replaceAll("\\W", " ").split(" +");

    ArrayList<String> nostop = new ArrayList<String>();

    for (String s : qWords) {
      s = s.replace("[.,]$", "");
      if (!mStopWordHash.contains(s.toLowerCase())) nostop.add(s);
    }
       
    return nostop;
  }
  
  private QueryParseRes createBagOfWordQuery(String question) {
	ArrayList<String> nostop = getQueryWords(question);

	StringBuilder res = new StringBuilder();

	for (int i = 0; i < nostop.size(); ++i) {
		if (i > 0)
			res.append(' ');
		res.append(nostop.get(i));
	}

	return new QueryParseRes(res.toString(), nostop.size());
  }
  
  private EvidencingResult scoreQuery( 
		  String 	assertionText, 
		  String 	fieldName, 
		  int 		matchPct, // how many words should be present
		  float 	slopeCoeff, // slope coeff for Solr query
		  int       topSearchResult
		  ) throws SolrServerException {
	  if (assertionText.isEmpty()) {
		  return new EvidencingResult(0f, 0f,0);
	  }

	  QueryParseRes q = createBagOfWordQuery(assertionText);

	  String query = String.format(
			  "_query_: \"{!edismax df=%s mm=%d%c pf=%s ps=%d} %s \"",
			  fieldName, matchPct, '%', fieldName,
			  (int) Math.round(slopeCoeff * q.wordQty), q.query);

	  HashMap<String, String> hshParams = new HashMap<String, String>();

	  if (DEBUG_INFO) {
		  System.out.println("Query: " + query);
	  }

	  hshParams.put("q", query);
	  hshParams.put("rows", String.valueOf(topSearchResult));
	  hshParams.put("fl", "ID,score");
	  SolrParams solrParams = new MapSolrParams(hshParams);
	  QueryResponse qryResponse = mSolrServer.query(solrParams, METHOD.POST);
	  SolrDocumentList results = qryResponse.getResults();

	  float discScore = 0f; // discounted score

	  float discountCoeff = 1;

	  for (int i = 0; i < Math.min(results.getNumFound(), topSearchResult); ++i) {
		  float v = (Float)results.get(i).getFieldValue("score");
		  discScore += v * discountCoeff;
		  discountCoeff *= mDiscountMult;
	  }

	  return new EvidencingResult(results.getMaxScore(),
			  discScore,
			  results.getNumFound());
  }
  
  public static void main(String args[]) throws Exception {
	  String serverUrl = args[0];
	  String coreName  = args[1];
	  String fieldName = args[2];
	  String query     = args[3];
	  
	  /* 
	   * These are some good defaults
	   */
	  int matchPct = 80;
	  float slopeCoeff = 1.5f;
	  int topSearchResult = 10;
      float discountMult = 0.5f;	  
	  /* end of good defaults */
	  
	  SolrAnswerCandidateEvidencer ev = 
			  new SolrAnswerCandidateEvidencer(serverUrl, coreName, fieldName,
										  		matchPct, slopeCoeff, topSearchResult,
										  		discountMult);
	  
	  EvidencingResult res = ev.scoreAssertion(query);
	  
	  System.out.println(query);
	  System.out.println("Discounted score: " + res.mDiscountScore + 
			  			 " qty: " + res.mQty +
			  			 " top score: " + res.mTopscore
			  			 );
  }
  
}

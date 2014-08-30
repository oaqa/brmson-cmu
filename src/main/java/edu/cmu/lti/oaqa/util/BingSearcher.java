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

import java.util.ArrayList;
import java.util.Hashtable;

import edu.cmu.lti.oaqa.util.WebSearchCache;

/**
 * A class to query bing via Bing API.
 * 
 * @author Leonid Boytsov
 *
 */

public class BingSearcher {
  private String              mCacheId  = "Bing";
  private WebSearchCache      mRetrievalCache = null;  
  private Hashtable<String, ArrayList<SearchResult>> mCacheStorage = null;;  
  private int                 mResNum = 100;  
  private String              mAccountKey;

  
  /**
   * 
   * Initialize a Bing retrieval class.
   * 
   * @param accountKey      A Bing API key for the Web search,  
   *                        can be retrieved at https://datamarket.azure.com/dataset/bing/searchweb.
   * @param cachePath       A path to the file, where we cache retrieved results.
   * @param resNum          A maximum number of results to return.
   * 
   */
  BingSearcher(String accountKey, 
              String  cachePath, 
              int     resNum) {
    mAccountKey     = accountKey; 
    mResNum         = resNum;
    mRetrievalCache = new WebSearchCache(cachePath);
    mCacheStorage = mRetrievalCache.loadCache(mCacheId);
    if (mCacheStorage == null)
      mCacheStorage = new Hashtable<String, ArrayList<SearchResult>>();

  }
  
  public ArrayList<SearchResult> retrieveDocuments(String query) throws Exception {
    ArrayList<SearchResult> resultL =  new ArrayList<SearchResult>(); 
    query = query.trim();
    if (query.isEmpty()) return resultL;

    String requestURL = BingSearcherUtil.BuildRequest(query, mResNum);

    System.out.println("Bing Search : " + query);
    BingSearcherUtil.getResults(mAccountKey, 
                                mRetrievalCache, mCacheStorage,
                                resultL, query, requestURL, mCacheId);
    
    System.out.println("Result size: " + resultL.size());


    return resultL;
  }
  
  
  public static void main(String[] args) throws Exception {
    String accountId = args[0];
    String cachePath = args[1];
    String query     = args[2];
    
    BingSearcher web = new BingSearcher(accountId, cachePath, 50);
    
    System.out.println("Query: '" + query + "'");
    System.out.println("Cache path: " + cachePath);
    
    ArrayList<SearchResult> res = web.retrieveDocuments(query);   
    
    for (int i = 0; i < res.size(); ++i) {
      SearchResult o = res.get(i);
      System.out.println(i + " " + o.getDocID() + " " + o.getAnswer());
    }
  }

}

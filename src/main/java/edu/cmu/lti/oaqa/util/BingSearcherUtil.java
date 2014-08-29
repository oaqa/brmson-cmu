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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.ws.commons.util.NamespaceContextImpl;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/* 
 * A helper class to retrieve web search results using the BING API.
 * 
 * The class requests results from server in the XML format. The full (and very bad)
 * description of the API can be found here (Bing Search API – Web Results Only):
 * 
 * https://datamarket.azure.com/dataset/bing/searchweb
 * 
 *  @author Leonid Boytsov (re-wrote & upgraded code to work with the new
 *                          Bing API, instead of the obsolete Live Search API.)
 *  @author Rui Liu
 * 
 */
public class BingSearcherUtil {
	// TODO: not thread-safe
	static XPathFactory factory = null;
	static XPath xpath = null;
	static XPathExpression expr = null;

	/*
	 * Builds a URL of search query to retrieve results from the Server
	 */
	public static String BuildRequest(String queryString, int numResults)
			throws URISyntaxException {
		// Note that the query should be in single quotes!
		URI QueryURI = new URI("https", null /* user info */,
				"api.datamarket.azure.com", -1 /* port */,
				"/Bing/SearchWeb/v1/Web", "Query='" + queryString + "'&$top="
						+ numResults + "&$format=atom", null /* fragment */);

		return QueryURI.toString();
	}

	public static Document GetResponse(String requestURL, String AccountKey)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		Document doc = null;
		DocumentBuilder db = dbf.newDocumentBuilder();

		if (db != null) {
			URL url = new URL(requestURL);
			URLConnection uc = url.openConnection();
			// The username is empty, the Account key is a password
			String userpass = AccountKey + ":" + AccountKey;
			String basicAuth = "Basic "
					+ new String(new Base64().encode(userpass.getBytes()));
			uc.setRequestProperty("Authorization", basicAuth);

			BufferedReader br = new BufferedReader(new InputStreamReader(
					uc.getInputStream(), "utf-8"));
			String input = "", line = "";

			while ((line = br.readLine()) != null) {
				input += line + "\n";
			}
			br.close();

			// When Bing returns an error, it is just a plain string,
			// not an XML starting with tag <feed

			if (!input.substring(0, 10).matches("^\\s*<feed\\s.*")) {
				throw new SAXException("Bing search failed, error: " + input);
			}

			StringReader reader = new StringReader(input);
			InputSource inputSource = new InputSource(reader);

			doc = db.parse(inputSource);
		}

		return doc;
	}

	/*
	 * Parses XML and extract results
	 */

	public static ArrayList<SearchResult> ProcessResponse(Document doc, String query)
			throws XPathExpressionException {
		factory = XPathFactory.newInstance();
		xpath = factory.newXPath();

		NamespaceContextImpl ctx = new NamespaceContextImpl();

		/*
		 * Prefix mapping for the following XML root tag:
		 * 
		 * <feed xmlns:base=
		 * "https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Web"
		 * xmlns:d="http://schemas.microsoft.com/ado/2007/08/dataservices"
		 * xmlns:
		 * m="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata"
		 * xmlns="http://www.w3.org/2005/Atom">
		 * 
		 * NOTE: the default namespace can use any prefix, not necessarily
		 * default. Yet, exactly the same prefix should also be used in XPATH
		 * expressions
		 */
		ctx.startPrefixMapping("base",
				"https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/Web");
		ctx.startPrefixMapping("d",
				"http://schemas.microsoft.com/ado/2007/08/dataservices");
		ctx.startPrefixMapping("m",
				"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
		ctx.startPrefixMapping("default", "http://www.w3.org/2005/Atom");
		xpath.setNamespaceContext(ctx);

		NodeList nodes = (NodeList) xpath.evaluate(
				"/default:feed/default:entry", doc, XPathConstants.NODESET);
		ArrayList<SearchResult> Reply = new ArrayList<SearchResult>();

		for (int i = 0; i < nodes.getLength(); i++) {
			try {
				Node CurrNode = nodes.item(i);

				String title = (String) xpath.evaluate(
						"default:content/m:properties/d:Title/text()",
						CurrNode, XPathConstants.STRING);
				String desc = (String) xpath.evaluate(
						"default:content/m:properties/d:Description/text()",
						CurrNode, XPathConstants.STRING);
				String url = (String) xpath.evaluate(
						"default:content/m:properties/d:Url/text()", CurrNode,
						XPathConstants.STRING);

				String DocText = "";
				if (!title.isEmpty())
					DocText += title + "\n";
				if (!desc.isEmpty())
					DocText += desc + "\n";

				if (!DocText.isEmpty()) {
				  SearchResult res = new SearchResult(DocText, query, url, i);
					res.setScore(-i);

					Reply.add(res);
				}
			} catch (XPathExpressionException e) {
				System.err
						.printf("[ERROR] cannot parse element # %d, ignoring, error: %s\n",
								i + 1, e.toString());
			}
		}

		System.out.println("Bing reply size: " + Reply.size());

		return Reply;
	}
	
	public static void getResults(
	    String AccountKey,
	    WebSearchCache retrievalCache, 
	    Hashtable<String, ArrayList<SearchResult>> cacheBing,
	    List<SearchResult> resultL, String question,
      String requestURL, String sourceID) {

    // Don't clear results here!!!
    // resultL.clear();
    Document doc;

    if (cacheBing.containsKey(requestURL)) {
      System.out.println("Bing Cache Entry Found");
      resultL.addAll(cacheBing.get(requestURL));
    } else {
      System.out.println("Not in Bing  Local cache");
      try {
        doc = BingSearcherUtil.GetResponse(requestURL, AccountKey);
        if (doc != null) {
          try {
            List<SearchResult> tmpResult = BingSearcherUtil
                .ProcessResponse(doc, question);
            resultL.addAll(tmpResult);
            
            ArrayList<SearchResult> cacheEntry = new ArrayList<SearchResult>();
            
            for (SearchResult resEntry : tmpResult) {
              cacheEntry.add(resEntry);
            }
            
            cacheBing.put(requestURL, cacheEntry);

            // Let's save the result set even if it's empty
            retrievalCache.saveCache(cacheBing, sourceID);
            
          } catch (XPathExpressionException e) {
            e.printStackTrace();
          }
        }
      } catch (ParserConfigurationException e) {
        e.printStackTrace();
      } catch (SAXException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }
	
}

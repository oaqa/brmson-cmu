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

import java.io.Serializable;

/**
 * <p>A <code>SearchResult</code> is a data structure representing a result returned
 * by a search engine.</p>
 * 
 * <p>This is a simplified version of the Ephyra Result class, 
 * which was originally created by Nico Schlaefer.</p> 
 * 
 * <p>This class implements the interfaces <code>Comparable</code> and
 * <code>Serializable</code>. Note: it has a natural ordering that is
 * inconsistent with <code>equals()</code>.</p>
 * 
 * @author Nico Schlaefer, Leonid Boytsov
 * @version 2014-08-29
 */
public class SearchResult implements Comparable<SearchResult>, Serializable {
	/** Version number used during deserialization. */
	private static final long serialVersionUID = 20070501;
	
	/** The answer string. */
	private String answer;
	/** A confidence measure for the answer, initially 0. */
	private float score = 0;
	/** A normalized confidence measure for the answer (optional). */
	private float normScore = 0;
	/** The <code>Query</code> that was used to obtain the answer (optional). */
	private String query;
	/** The ID (e.g. a URL) of a document containing the answer (optional). */
	private String docID;
	/** The ID of the document in the search engine cache (optional). */
	private String cacheID;
	/** The hit position of the answer, starting from 0 (optional). */
	private int hitPos = -1;
	
	/**
	 * Creates a <code>Result</code> object and sets the answer string.
	 * 
	 * @param answer answer string
	 */
	public SearchResult(String answer) {
		this.answer = answer;
	}
	
	/**
	 * Creates a <code>Result</code> object and sets the answer string and the
	 * <code>Query</code> that was used to obtain the answer.
	 * 
	 * @param answer answer string
	 * @param query <code>Query</code> object
	 */
	public SearchResult(String answer, String query) {
		this(answer);
		
		this.query = query;
	}
	
	/**
	 * Creates a <code>Result</code> object and sets the answer string, the
	 * <code>Query</code> that was used to obtain the answer and the ID of a
	 * document that contains it.
	 * 
	 * @param answer answer string
	 * @param query <code>Query</code> object
	 * @param docID document ID
	 */
	public SearchResult(String answer, String query, String docID) {
		this(answer,query);
		
		this.docID = docID;
	}
	
	/**
	 * Creates a <code>Result</code> object and sets the answer string, the
	 * <code>Query</code> that was used to obtain the answer, the ID of a
	 * document that contains it and the hit position.
	 * 
	 * @param answer answer string
	 * @param query <code>Query</code> object
	 * @param docID document ID
	 * @param hitPos hit position, starting from 0
	 */
	public SearchResult(String answer, String query, String docID, int hitPos) {
		this(answer, query, docID);
		
		this.hitPos = hitPos;
	}
	
	/**
	 * Compares two results by comparing their scores.
	 * 
	 * @param result the result to be compared
	 * @return a negative integer, zero or a positive integer as this result is
	 *         less than, equal to or greater than the specified result
	 */
	public int compareTo(SearchResult result) {
		float diff = score - result.getScore();
		
		if (diff < 0)
			return -1;
		else if (diff > 0)
			return 1;
		else
			return 0;
//			return answer.compareTo(result.getAnswer());  // tie-breaking
	}
	
	/**
	 * Indicates whether an other result is equal to this one.
	 * Two results are considered equal if the answer strings are equal.
	 * 
	 * @param o the object to be compared
	 * @return <code>true</code> iff the objects are equal
	 */
	public boolean equals(Object o) {
		// if objects incomparable, return false
		if (!(o instanceof SearchResult)) return false;
		SearchResult result = (SearchResult) o;
		
		return answer.equals(result.answer);
	}
	
	/**
	 * Returns the hash code of the answer string as a hash code for the result.
	 * 
	 * @return hash code
	 */
	public int hashCode() {
		return answer.hashCode();
	}
	
	/**
	 * Returns the answer string.
	 * 
	 * @return answer string
	 */
	public String getAnswer() {
		return answer;
	}
	
	/**
	 * Returns the confidence score of the result.
	 * 
	 * @return confidence score
	 */
	public float getScore() {
		return score;
	}
	
	/**
	 * Returns the normalized score of the result.
	 * 
	 * @return normalized score
	 */
	public float getNormScore() {
		return normScore;
	}
	
	/**
	 * Returns the <code>Query</code> that was used to obtain this result, or
	 * <code>null</code> if it is not set.
	 * 
	 * @return <code>Query</code> used to obtain this result or
	 * 		   <code>null</code>
	 */
	public String getQuery() {
		return query;
	}
	
	/**
	 * Returns the ID of a document that contains the answer or
	 * <code>null</code> if it is not set.
	 * 
	 * @return document ID or <code>null</code>
	 */
	public String getDocID() {
		return docID;
	}
	
	/**
	 * Returns the ID of the document in the search engine cache or
	 * <code>null</code> if it is not set.
	 * 
	 * @return ID of the cached document or <code>null</code>
	 */
	public String getCacheID() {
		return cacheID;
	}
	
	/**
	 * Returns the hit position of the result, starting from 0, or -1 if it is
	 * not set.
	 * 
	 * @return hit position or -1
	 */
	public int getHitPos() {
		return hitPos;
	}
	
	/**
	 * Sets the answer string.
	 * 
	 * @param answer the answer string
	 */
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	
	/**
	 * Sets the confidence score of this result.
	 * 
	 * @param score confidence score
	 */
	public void setScore(float score) {
		this.score = score;
	}
	
	/**
	 * Increments the confidence score by the given value.
	 * 
	 * @param value the value to be added to the score
	 */
	public void incScore(float value) {
		score += value;
	}
	
	/**
	 * Sets the normalized score of this result.
	 * 
	 * @param normScore normalized score
	 */
	public void setNormScore(float normScore) {
		this.normScore = normScore;
	}
	
	/**
	 * Sets the ID of a document that contains the answer.
	 * 
	 * @param docID document ID
	 */
	public void setDocID(String docID) {
		this.docID = docID;
	}
	
	/**
	 * Sets the ID of the document in the search engine cache.
	 * 
	 * @param cacheID ID of the cached document
	 */
	public void setCacheID(String cacheID) {
		this.cacheID = cacheID;
	}
	
	
	/**
	 * Returns a copy of this <code>Result</code> object.
	 * 
	 * @return copy of this object
	 */
	public SearchResult getCopy() {
		SearchResult result = new SearchResult(answer, query, docID, hitPos);
		result.score = score;
		result.normScore = normScore;
		result.cacheID = cacheID;

		return result;
	}	
}

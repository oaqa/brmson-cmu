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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;

public class WebSearchCache {
	private String path;
	private Hashtable<String, Hashtable<String, ArrayList<SearchResult>>> tableInMemory 
	              = new Hashtable<String, Hashtable<String, ArrayList<SearchResult>>>();

	public WebSearchCache(String cachePath) {
		this.path=cachePath;
	}

	public Hashtable<String, ArrayList<SearchResult>> loadCache(String RetrievalEngine) {
		// If already in memory, then return back
		if (tableInMemory.containsKey(RetrievalEngine))
			tableInMemory.get(RetrievalEngine);
		if (tableInMemory.isEmpty()){
			//System.out.println("tableInMemory is still empty");
		}
			
		String fileName = path + RetrievalEngine + "Cache.txt";
		//System.out.println("Reading " + RetrievalEngine + " Cache File");
		ObjectInputStream inputStream = null;
		Hashtable<String, ArrayList<SearchResult>> table = null;

		try {
			inputStream = new ObjectInputStream(new FileInputStream(fileName));
			try {
				table = (Hashtable<String, ArrayList<SearchResult>>) inputStream.readObject();
			} catch (ClassNotFoundException e) {
			  e.printStackTrace();
			}
			inputStream.close();
		} catch (FileNotFoundException e) {
			System.err.println("Cache file '" + fileName + "' doesn't exist!");
			//e.printStackTrace();
			return null;
		} catch (IOException e) {
		} finally {
		}

		if (table != null)
			tableInMemory.put(RetrievalEngine, table);
		if (tableInMemory.isEmpty())
			System.out.println("tableInMemory after the put command is still empty");
		return table;
	}

	public void saveCache(Hashtable<String, ArrayList<SearchResult>> table, 
	                      String RetrievalEngine) {

		if (table == null) {
			System.err.println("Save cache : Table is null");
			return;
		}

		if (tableInMemory.isEmpty()) {
			System.out
					.println("tableInMemory is still empty at the save cache stage");
		}

		tableInMemory.put(RetrievalEngine, table);

		System.out.println("Writing " + RetrievalEngine + " Cache File");

		String fileName = path + RetrievalEngine + "Cache.txt";
		ObjectOutputStream outputStream = null;
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(fileName));
			outputStream.writeObject(table);
			outputStream.close();
		} catch (Exception e) {
		  e.printStackTrace();
		} 
	}
}

package com.data2semantics.syncproject.daemon.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;


/**
 * Resource which has only one representation.
 * 
 */
public class Util {
	
	public static void executeQuery(String uri, String query) throws IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(uri);
		post.addHeader("Content-type", "application/x-www-form-urlencoded");
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("update", query));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		client.execute(post);
	}
	
	
	/**
	 * Compare text files, execute differences (i.e. new queries), and log new executed queries
	 * 
	 */
	public static void processTextFileChanges(File queriesFile, File executedQueriesFile, String delimiter, String tripleStoreUri) throws Exception {
		// Get the object of DataInputStream
		DataInputStream inSrc = new DataInputStream(new FileInputStream(queriesFile));
		DataInputStream inDest = new DataInputStream(new FileInputStream(executedQueriesFile));
		BufferedReader brSrc = new BufferedReader(new InputStreamReader(inSrc));
		BufferedReader brDest = new BufferedReader(new InputStreamReader(inDest));
		String srcLine;
		boolean firstline = true;
		String changes = "";
		// Read File Line By Line
		while ((srcLine = brSrc.readLine()) != null) {
			if (!srcLine.equals(brDest.readLine())) {
				changes += (firstline? "" : "\n") + srcLine;
			}
			firstline = false;

		}
		//System.out.println("Wrinting Changes: '" + changes + "'");
		inSrc.close();
		inDest.close();
		
		executeChanges(changes, delimiter, tripleStoreUri);
		storeChanges(executedQueriesFile, changes);
			
	}
	
	/**
	 * Execute string containing changes
	 * 
	 * @param changes String containing changes
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private static void executeChanges(String changes, String delimiter, String tripleStoreUri) throws IOException {
		String[] queries = changes.split(delimiter);
		for (String query: queries) {
			query = query.trim();
			if (query.length() > 0) {
//				System.out.println("Executing: " + query);
				executeQuery(tripleStoreUri, query);
			}
		}
	}
	
	/**
	 * Store (the just executed) changes in log file, to keep track of what has been executed
	 * 
	 * @param changes
	 * @param executedQueriesFile
	 * @throws IOException 
	 */
	private static void storeChanges(File file, String changes) throws IOException {
		FileWriter fw;
		if (file.length() <= 1) {
			fw = new FileWriter(file);
		} else {
			fw = new FileWriter(file, true);
		}
		
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(changes);
	    bw.close();
	}
	
	
}
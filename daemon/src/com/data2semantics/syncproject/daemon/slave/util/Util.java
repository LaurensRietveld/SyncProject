package com.data2semantics.syncproject.daemon.slave.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;


/**
 * Resource which has only one representation.
 * 
 */
public class Util {
	
	public static void executeQuery(String uri, String query) throws IOException {
		/**When using httpclient 4.0:
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(uri);
		System.out.println("Executing on " + uri + ": " + query);
		post.addHeader("Content-type", "application/x-www-form-urlencoded");
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("update", query));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		client.execute(post);*/
		//System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        HttpClient client = new HttpClient();
        BufferedReader br = null;
        PostMethod method = new PostMethod(uri);
        method.addParameter("update", query);

		try {
			int returnCode = client.executeMethod(method);
			if (returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
				System.err.println("The Post method is not implemented by this URI");
				// still consume the response body
				method.getResponseBodyAsString();
			} else {
				br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
				String readLine;
				while (((readLine = br.readLine()) != null)) {
					System.err.println(readLine);
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			method.releaseConnection();
			if (br != null) {
				try {
					br.close();
				} catch (Exception fe) {
					fe.printStackTrace();
					System.exit(1);
				}
			}
		}
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
				System.out.println("Executing: " + query);
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
		fw = new FileWriter(file, true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(changes);
	    bw.close();
	}
	
	
}
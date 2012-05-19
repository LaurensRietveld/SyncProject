package com.data2semantics.syncproject.daemon.slave.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

//import org.apache.commons.httpclient.NameValuePair;
//import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Resource which has only one representation.
 * 
 */
public class Query {
	
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
        
        HttpClient client = new HttpClient();
        BufferedReader br = null;
        PostMethod method = new PostMethod(uri);
        method.addParameter("update", query);

        try{
          int returnCode = client.executeMethod(method);

          if(returnCode == HttpStatus.SC_NOT_IMPLEMENTED) {
            System.err.println("The Post method is not implemented by this URI");
            // still consume the response body
            method.getResponseBodyAsString();
          } else {
            br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
            String readLine;
            while(((readLine = br.readLine()) != null)) {
              System.err.println(readLine);
          }
          }
        } catch (Exception e) {
          System.err.println(e);
        } finally {
          method.releaseConnection();
          if(br != null) try { br.close(); } catch (Exception fe) {}
        }
	}
}
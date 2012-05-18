package com.data2semantics.syncproject.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ServerResource;
import com.data2semantics.syncproject.EntryPoint;
import com.data2semantics.syncproject.util.QueryLog;
import com.data2semantics.syncproject.util.QueryTypes;
import com.data2semantics.syncproject.util.Util;

/**
 * Resource which has only one representation.
 * 
 */
public class Query extends ServerResource {
	private String sparqlQuery;
	private String sparqlQueryType;
	//TODO: How to fix these media types... Results from sesame endpoint are still messed up: they are xml, but with a wrong xsl tag or something
	private MediaType responseMediaType = MediaType.APPLICATION_SPARQL_RESULTS_XML;
//	private MediaType responseMediaType = MediaType.APPLICATION_SPARQL_RESULTS_JSON;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.restlet.resource.Resource#getApplication()
	 */
	@Override
	public EntryPoint getApplication() {
		return (EntryPoint) super.getApplication();
	}
	
	/**
	 * Execute query and return result in representation object. If query is empty, the query form is returned
	 * 
	 * @param sparqlQuery
	 * @return
	 */
	public Representation processQuery(String sparqlQuery, String sparqlQueryType) {
		this.sparqlQuery = sparqlQuery;
		this.sparqlQueryType = sparqlQueryType;
		Representation result;
        if (sparqlQuery.length() > 0) {
        	result = this.executeQuery();
		} else {
			result = Util.getQueryForm(getApplication(), false, getReference().toString());
		}
        return result;
	}
	
	private Representation executeQuery() {
		String uri;
    	if (sparqlQueryType == QueryTypes.SELECT) {
    		uri = getApplication().getConfig().getString("master.tripleStore.selectUri");
    	} else {
    		uri = getApplication().getConfig().getString("master.tripleStore.updateUri");
    	}
    	Representation queryResult;
    	if (sparqlQueryType == QueryTypes.SELECT && this.sparqlQuery.length() < 2000) {
    		queryResult = executeGETQuery(uri);
    	} else {
    		queryResult = executePOSTQuery(uri);
    		
    	}
    	return queryResult;
	}
	
	private Representation executeGETQuery(String uri) {
		String queryResult = "";
		ClientResource resource = new ClientResource(uri + "?" + sparqlQueryType + "=" + sparqlQuery + "&Accept=" + responseMediaType.getName());  
		resource.setNext(new Client(new Context(), Protocol.HTTP));
		Representation result = null;
		try {
			result = resource.get(responseMediaType);
			try {
				QueryLog.log(this, QueryLog.PLAIN_TEXT_FILE);
			} catch (IOException e) {
				queryResult += "\nFailed to log query: " + e.getMessage();
			} catch (NoSuchFieldException e) {
				queryResult += "\nFailed to log query: " + e.getMessage();
			}
		} catch (Exception e) {
			queryResult = "Error in executing query on " + uri + ": " + e.getMessage();
			getApplication().getLogger().log(Level.SEVERE, queryResult);
		}
		if (result == null) {
			result = new StringRepresentation(queryResult, responseMediaType);
		}
		return result;
	}
	

	private Representation executePOSTQuery(String uri) {
		String queryResult = "";
		/**
		TODO: Execution does not work with restlet somehow.. 
		Therefore, use the http commons stuff. Should find out whether this was a restlet bug, or
		Form form = new Form();
		form.add(this.sparqlQueryType, sparqlQuery);
		ClientResource resource = new ClientResource(uri);
		try {
			queryResult = resource.post(form.getWebRepresentation()).getText();
		} catch (Exception e) {
			queryResult = "Error in executing query on " + uri + ": " + e.getMessage();
			e.printStackTrace();
			getApplication().getLogger().log(Level.SEVERE, queryResult);
		}**/
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(uri);
		post.addHeader("Content-type", "application/x-www-form-urlencoded");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair(this.sparqlQueryType, this.sparqlQuery));
			post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
			HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				queryResult += line + "\n";
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return new StringRepresentation(queryResult, responseMediaType);
	}
	
	public String getSparqlQuery() {
		return this.sparqlQuery;
	}
	
	public String getSparqlQueryType() {
		return this.sparqlQueryType;
	}

	
}
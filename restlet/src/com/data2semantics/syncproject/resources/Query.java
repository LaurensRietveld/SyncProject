package com.data2semantics.syncproject.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
import com.data2semantics.syncproject.logging.QueryLog;
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
	private int mode = 1;
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
	
	/**
	 * Execute query (and log) query
	 * 
	 * @return Representation containing query result
	 */
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
				QueryLog.log(this);
			} catch (Exception e) {
				result = new StringRepresentation("\nFailed to log query: " + e.getMessage());
			}
		} catch (Exception e) {
			result = new StringRepresentation("Error in executing query on " + uri + ": " + e.getMessage());
			getLogger().severe(queryResult);
		}
		if (result == null) {
			result = new StringRepresentation(queryResult, responseMediaType);
		}
		return result;
	}
	

	private Representation executePOSTQuery(String uri) {
		Representation result;
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
			try {
				QueryLog.log(this);
			} catch (Exception e) {
				result = new StringRepresentation("\nFailed to log query: " + e.getMessage());
			}
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			String queryResult = "";
			while ((line = rd.readLine()) != null) {
				queryResult += line + "\n";
			}
			result = new StringRepresentation(queryResult, this.responseMediaType);

		} catch (IOException e) {
			e.printStackTrace();
			result = new StringRepresentation(e.getMessage());
		}
		return result;
	}
	
	/**
	 * Get sparql query 
	 * @return String
	 */
	public String getSparqlQuery() {
		return this.sparqlQuery;
	}
	
	/**
	 * Get type of SPARQL query, either 'update' or 'query' (select)
	 * @return String
	 */
	public String getSparqlQueryType() {
		return this.sparqlQueryType;
	}
	
	/**
	 * Set mode of restlet to work in. (e.g. log text queries, dump triples as xml, use db, central server)
	 * possible values: PLAIN_TEXT_FILE = 1, DB = 2, EXPORT_GRAPHS = 3, CENTRAL_SERVER = 4
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	/**
	 * Get mode of restlet to work in. (e.g. log text queries, dump triples as xml, use db, central server)
	 * possible values: PLAIN_TEXT_FILE = 1, DB = 2, EXPORT_GRAPHS = 3, CENTRAL_SERVER = 4
	 * @return int
	 */
	public int getMode() {
		return this.mode;
	}

	
}
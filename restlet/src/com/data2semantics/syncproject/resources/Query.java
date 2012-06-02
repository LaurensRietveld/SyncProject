package com.data2semantics.syncproject.resources;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
import com.data2semantics.syncproject.logging.QueryLog;
import com.data2semantics.syncproject.util.QueryTypes;
import com.data2semantics.syncproject.util.Util;
import com.typesafe.config.Config;

public class Query {
	private String sparqlQuery;
	private MediaType responseMediaType = MediaType.APPLICATION_SPARQL_RESULTS_XML;
	private Config config;
	private MainServerResource main;
	private Logger logger;
	
	public Query(String sparqlQuery, MainServerResource main) {
		this.sparqlQuery = sparqlQuery;
		config = main.getApplication().getConfig();
		logger = main.getLogger();
		this.main = main;
	}
	
	private Representation executePOSTQuery(String uri) throws Exception {
		
		/*TODO: Execution does not work with restlet somehow.. 
		Therefore, use the http commons stuff. Should find out whether this was a restlet bug, or
		Form form = new Form();
		form.add(this.sparqlQueryType, sparqlQuery);
		ClientResource resource = new ClientResource(uri);
		String queryResult = "bla";
		try {
			queryResult = resource.post(form.getWebRepresentation()).getText();
		} catch (Exception e) {
			queryResult = "Error in executing query on " + uri + ": " + e.getMessage();
			e.printStackTrace();
			getLogger().severe(queryResult);
		}
		return new StringRepresentation(queryResult);*/
		Representation result;
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(uri);
		post.addHeader("Content-type", "application/x-www-form-urlencoded");
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair(main.getSparqlQueryType(), this.sparqlQuery));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
 
		HttpResponse response = client.execute(post);
		QueryLog.log(this);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		String queryResult = "";
		while ((line = rd.readLine()) != null) {
			queryResult += line + "\n";
		}
		result = new StringRepresentation(queryResult, this.responseMediaType);
		return result;
	}
	
	/**
	 * Get sparql query 
	 * @return String
	 */
	public String getSparqlQuery() {
		return this.sparqlQuery;
	}
	
	public MainServerResource getMain() {
		return this.main;
	}
	
	public Config getConfig() {
		return this.config;
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
	/**
	 * Execute query and return result in representation object. If query is empty, the query form is returned
	 * 
	 * @param sparqlQuery
	 * @return
	 * @throws Exception 
	 */
	public Representation processQuery() throws Exception {
		Representation result;
        if (sparqlQuery == null || sparqlQuery.length() > 0) {
        	logger.info("executing query");
        	result = this.executeQuery();
		} else {
			logger.info("no query to executy. Loading query form");
			result = Util.getQueryForm(main.getApplication(), false, main.getReference().toString());
		}
        return result;
	}
	
	/**
	 * Execute query (and log) query
	 * 
	 * @return Representation containing query result
	 * @throws Exception 
	 */
	private Representation executeQuery() throws Exception {
		String uri;
    	if (main.getSparqlQueryType() == QueryTypes.SELECT) {
    		uri = config.getString("master.tripleStore.selectUri");
    	} else {
    		uri = config.getString("master.tripleStore.updateUri");
    	}
    	Representation queryResult;
    	if (main.getSparqlQueryType() == QueryTypes.SELECT && this.sparqlQuery.length() < 2000) {
    		queryResult = executeGETQuery(uri);
    	} else {
    		//queryResult = executeGETQuery(uri);
    		queryResult = executePOSTQuery(uri);
    		
    	}
    	return queryResult;
	}
	
	private Representation executeGETQuery(String uri) throws Exception {
		String queryResult = "";
		ClientResource resource = new ClientResource(uri + "?" + main.getSparqlQueryType() + "=" + sparqlQuery + "&Accept=" + responseMediaType.getName());  
		resource.setNext(new Client(new Context(), Protocol.HTTP));
		Representation result = null;
		result = resource.get(responseMediaType);
		QueryLog.log(this);
		if (result == null) {
			result = new StringRepresentation(queryResult, responseMediaType);
		}
		//The restlet is having memory leaks. This might solve this issue
		//(http://restlet-discuss.1400322.n2.nabble.com/resource-leak-after-Post-quot-form-quot-td7186110.html)
		resource.release();
		resource.getResponse().getEntity().exhaust(); 
		return result;
	}
}
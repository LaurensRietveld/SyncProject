package com.data2semantics.syncproject.stores;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;

/**
 * The server side implementation of the RPC service.
 */
public class Sparql {

	public static String ECULTURE2 = "http://eculture2.cs.vu.nl:5020/sparql/";
	
	
	
	
	/**
	 * Execute query
	 * 
	 * @param queryString
	 */
	public static ResultSet query(String endpoint, String queryString) {
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet results = queryExecution.execSelect();
		return results;
	}

	
	public static void queryPrintResult(String endpoint, String queryString) {
		ResultSet results = Sparql.query(endpoint, queryString);
		ResultSetFormatter.out(System.out, results);
	}
	
	public static String queryGetString(String endpoint, String queryString) {
		ResultSet results = Sparql.query(endpoint, queryString);
		return ResultSetFormatter.asText(results);
	}
	
	

}

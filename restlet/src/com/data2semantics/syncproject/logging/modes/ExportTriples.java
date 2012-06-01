package com.data2semantics.syncproject.logging.modes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;


public class ExportTriples {
    public static void export(com.data2semantics.syncproject.resources.Query query, String endpoint, File exportFile) throws Exception{
    	//Always create (or overwrite) new file
    	if (exportFile.exists()) {
    		exportFile.delete();
    	}
		exportFile.createNewFile();
    	ResultSet result = query(endpoint, "SELECT * WHERE {?subject ?predicate ?object}");
    	query.getLogger().info("going to process query");
    	FileWriter fileWriter = new FileWriter(exportFile.getAbsolutePath(),true);
        BufferedWriter bufferWritter = new BufferedWriter(fileWriter);
    	while (result.hasNext()) {
    		QuerySolution solution = result.next();
    		String writeString = getString(solution.get("subject")) + " " + 
	        		getString(solution.get("predicate")) + " " + 
	        		getString(solution.get("object")) + "\n";
			bufferWritter.write(writeString);
	        
	        
		}
    	bufferWritter.close();
    }
    
	/**
	 * Execute query
	 * 
	 * @param queryString
	 */
	private static ResultSet query(String endpoint, String queryString) {
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet results = queryExecution.execSelect();
		return results;
	}
	private static String getString(RDFNode rdfNode) {
		String result = "";
		if (rdfNode.isLiteral()) {
			result = rdfNode.toString();
			try {
				Integer.parseInt(result);
				//Keep result value as it is
			} catch (Exception e) {
				//apparently a string, so add quotes
				result = "'" + result + "'";
			}

		} else if (rdfNode.isAnon()) {
			//Make it into a uri
			result = "<" + rdfNode.toString() + ">";
			
		} else if (rdfNode.isResource() || rdfNode.isURIResource()) {
			result = "<" + rdfNode.toString() + ">";
		}
		return result;
	}
}

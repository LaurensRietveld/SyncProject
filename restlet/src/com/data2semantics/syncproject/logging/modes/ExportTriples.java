package com.data2semantics.syncproject.logging.modes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.data2semantics.syncproject.util.Util;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.typesafe.config.Config;


public class ExportTriples {
	
	
    public static void export(com.data2semantics.syncproject.resources.Query query, String endpoint, File exportFile) throws Exception{
    	//empty file if it exists (do not create new one: file permissions get screwed up"
    	if (exportFile.exists()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(exportFile));
            bw.write("");
            bw.flush();
            bw.close();
    	}
    	ResultSet result = query(endpoint, "SELECT * WHERE {?subject ?predicate ?object}");
    	query.getLogger().info("going to process query and write all results to file");
    	
    	writeResultToFile(result, exportFile);
    	Config config = query.getApplication().getConfig();
    	File destFile = new File(config.getString("slave.serverLocation") + ":" + config.getString("slave.xmlDumpDir") + "/" + config.getString("mode3.dumpFile"));
    	Util.rsync(exportFile, destFile);
    }
    
	/**
	 * Execute query
	 * 
	 * @param endpoint Endpoint Uri
	 * @param queryString
	 * 
	 * @return ResultSet
	 */
	private static ResultSet query(String endpoint, String queryString) {
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet results = queryExecution.execSelect();
		return results;
	}
	
	private static void writeResultToFile(ResultSet result, File exportFile) throws IOException {
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
	 * For an rdf node (taken from a 'querysolution'), get a formatted string, so that this node can easily be used in the insert query of the slave
	 * @param rdfNode
	 * @return
	 */
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

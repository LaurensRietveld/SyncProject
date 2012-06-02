package com.data2semantics.syncproject.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.util.QueryTypes;
import com.data2semantics.syncproject.util.Util;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.typesafe.config.Config;


public class ExportTriples extends GenericLogger{
	
	public ExportTriples(boolean batchLogging, MainServerResource main) {
		super(batchLogging, main);
	}
	

    public void log(com.data2semantics.syncproject.resources.Query query) throws Exception {
    	//Do nothing. No need to perform logging for each query, as the query itself is not logged.
    }
    
    
	public void loggingCallback() throws Exception {
		//No need to export when data hasnt changed (i.e. only on update)
		if (getMain().getSparqlQueryType() == QueryTypes.UPDATE) {
			Config config = getMain().getApplication().getConfig();
			
	    	String endpoint = config.getString("master.tripleStore.selectUri");
			File exportFile = new File(config.getString("master.xmlDumpDir") + "/" + config.getString("mode3.dumpFile"));
	    	//empty file if it exists (do not create new one: file permissions get screwed up)
	    	if (exportFile.exists()) {
	            BufferedWriter bw = new BufferedWriter(new FileWriter(exportFile));
	            bw.write("");
	            bw.flush();
	            bw.close();
	    	}
	    	ResultSet result = query(endpoint, "SELECT * WHERE {?subject ?predicate ?object}");
	    	getMain().getLogger().info("going to process query and write all results to file");
	    	
	    	writeResultToFile(result, exportFile);
	    	String destFile = config.getString("slave.serverLocation") + ":" + config.getString("slave.xmlDumpDir") + "/" + config.getString("mode3.dumpFile");
	    	Util.rsync(exportFile, destFile);
		}
	}
    
    
	/**
	 * Execute query
	 * 
	 * @param endpoint Endpoint Uri
	 * @param queryString
	 * 
	 * @return ResultSet
	 */
	private ResultSet query(String endpoint, String queryString) {
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet results = queryExecution.execSelect();
		return results;
	}
	
	private void writeResultToFile(ResultSet result, File exportFile) throws IOException {
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
	private String getString(RDFNode rdfNode) {
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

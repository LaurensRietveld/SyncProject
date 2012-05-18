package com.data2semantics.syncproject.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import com.data2semantics.syncproject.resources.Query;

public class QueryLog {
	
	public static final int PLAIN_TEXT_FILE = 1;
	public static final int DB = 2;
	public static final int EXPORT_GRAPHS = 3;
	
	
	public static void log(Query query, int logType) throws NoSuchFieldException, IOException {
        switch (logType) {
            case PLAIN_TEXT_FILE:
            	logToTextFile(query);
            	break;
            case EXPORT_GRAPHS:
            	SesameExport.export(query.getApplication().getConfig().getString(""), "dd", new File("sdf"));
        	default:
        		throw new NoSuchFieldException("No valid logtype provided");
        }
	}
	
	/**
	 * Log a query. This only occurs when query is executed on server. However, the query may have failed! 
	 * TODO: check whether query response contains errors. If so, probably do not store (or store separately)..
	 * 
	 * @param query Query String
	 * @param queryType Type of query, either select or update
	 * @throws IOException 
	 */
	private static void logToTextFile(Query query) throws IOException {
		String filename = query.getApplication().getConfig().getString("master.queryLogDir") + "/" + query.getSparqlQueryType() + ".log";
		File file = new File(filename);
	    if(!file.exists()){
	    	query.getApplication().getLogger().log(Level.WARNING, "Log file does not existing. Creating one: " + file.getPath());
	    	file.createNewFile();
	    }
	    query.getApplication().getLogger().info("Writing query: " + query.getSparqlQuery());
	    FileWriter fw = new FileWriter(filename, true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(query.getApplication().getConfig().getString("queryDelimiter") + query.getSparqlQuery());
	    bw.close();
	}
	
}

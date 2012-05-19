package com.data2semantics.syncproject.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import com.data2semantics.syncproject.resources.Query;
import com.typesafe.config.Config;

public class QueryLog {
	
	public static final int PLAIN_TEXT_FILE = 1;
	public static final int DB = 2;
	public static final int EXPORT_GRAPHS = 3;
	public static final int CENTRAL_SERVER = 4;
	
	
	public static void log(Query query) throws Exception {
		Config config = query.getApplication().getConfig();
		int logType = query.getMode();
		query.getApplication().getLogger().info("Using mode " + Integer.toString(logType));
        switch (logType) {
            case PLAIN_TEXT_FILE:
            	logToTextFile(query);
            	break;
            case EXPORT_GRAPHS:
            	//No need to export graphs when it is a simple select query
            	if (query.getSparqlQueryType().equals("update")) {
            		SesameExport.export(
            			new File(config.getString("master.exportToXmlJar")),
            			config.getString("master.tripleStore.sesameApi"),
            			"master",
            			new File(config.getString("master.queryLogDir") + "/dump.xml"));
            	}
            	break;
        	default:
        		throw new NoSuchFieldException("ERROR: No valid logtype provided");
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
		Config config = query.getApplication().getConfig();
		String filename = config.getString("master.queryLogDir") + "/";
		if (query.getSparqlQueryType().equals("update")) {
			filename += config.getString("mode1.updateFile");
		} else {
			filename += config.getString("mode1.queryFile");
		}
		File file = new File(filename);
	    if(!file.exists()){
	    	query.getApplication().getLogger().log(Level.WARNING, "Log file does not existing. Creating one: " + file.getPath());
	    	file.createNewFile();
	    }
	    query.getApplication().getLogger().info("Writing query: " + query.getSparqlQuery());
	    FileWriter fw = new FileWriter(filename, true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(query.getApplication().getConfig().getString("mode1.queryDelimiter") + query.getSparqlQuery());
	    bw.close();
	}
	

}

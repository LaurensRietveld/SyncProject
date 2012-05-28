package com.data2semantics.syncproject.logging.modes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.data2semantics.syncproject.resources.Query;
import com.typesafe.config.Config;

public class TextLogger {

	/**
	 * Log a query. This only occurs when query is executed on server. However, the query may have failed! 
	 * TODO: check whether query response contains errors. If so, probably do not store (or store separately)..
	 * 
	 * @param query Query String
	 * @param queryType Type of query, either select or update
	 * @throws IOException 
	 */
	public static void log(Query query) throws IOException {
		Config config = query.getApplication().getConfig();
		String filename = config.getString("master.queryLogDir") + "/";
		if (query.getSparqlQueryType().equals("update")) {
			filename += config.getString("mode1.updateFile");
		} else {
			filename += config.getString("mode1.queryFile");
		}
		File file = new File(filename);
		writeToFile(query.getLogger(), file, config.getString("mode1.queryDelimiter") + query.getSparqlQuery());
	}
	
	public static void writeToFile(Logger logger, File file, String string) throws IOException {
		if(!file.exists()){
	    	logger.log(Level.WARNING, "Log file does not existing. Creating one: " + file.getPath());
	    	file.createNewFile();
	    }
	    logger.info("Writing to file: " + string);
	    FileWriter fw = new FileWriter(file, true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(string);
	    bw.close();
	}
}

package com.data2semantics.syncproject.logging;

import java.io.File;

import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.resources.Query;
import com.data2semantics.syncproject.util.Util;
import com.typesafe.config.Config;

public class TextLogger extends GenericLogger{
	private String destFile;
	private File logFile;
	private String delimiter;
	public TextLogger(boolean batchLogging, MainServerResource main) {
		super(batchLogging, main);
		Config config = main.getApplication().getConfig();
		destFile = config.getString("slave.serverLocation") + ":" + config.getString("slave.queryLogDir") + "/" + config.getString("mode1.updateFile");
		delimiter = config.getString("mode1.queryDelimiter");
		
		String logFileName = config.getString("master.queryLogDir") + "/";
		if (getMain().getSparqlQueryType().equals("update")) {
			logFileName += config.getString("mode1.updateFile");
		} else {
			logFileName += config.getString("mode1.queryFile");
		}
		logFile = new File(logFileName);
	}
	
	/**
	 * Log a query. This only occurs when query is executed on server. However, the query may have failed! 
	 * TODO: check whether query response contains errors. If so, probably do not store (or store separately)..
	 * 
	 * @param query Query String
	 * @param queryType Type of query, either select or update
	 * @throws Exception 
	 */
	public void log(Query query) throws Exception {
		Util.writeToFile(query.getMain().getLogger(), logFile, delimiter + query.getSparqlQuery());
	}
	
	public void loggingCallback() throws Exception {
		Util.rsync(logFile, destFile);
	}
}

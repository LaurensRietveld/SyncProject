package com.data2semantics.syncproject.master.logging;

import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.resources.Query;

public class GenericLogger {
	private boolean batchLogging;
	private MainServerResource main;
	public static final int LOG_QUERIES_RSYNC = 1;
	public static final int LOG_QUERIES_DB = 2;
	public static final int SERIALIZE_GRAPH_RSYNC = 3;
	public static final int LOG_QUERIES_GIT = 4;
	public static final int SERIALIZE_GRAPH_GIT = 5;
	public static final int SERIALIZE_GRAPH_DB = 6;
	
	public GenericLogger(boolean batchLogging, MainServerResource main) {
		this.batchLogging = batchLogging;
		this.main = main;
	}
	
	public void log(Query query) throws Exception {
		throw new Exception("Generic logger call. Overwrite this method for proper use");
	}
	
	/**
	 * Callback function to perform after one, or batch of queries, have been executed
	 * @throws Exception 
	 */
	public void loggingCallback() throws Exception {
		throw new Exception("Generic logger call. Overwrite this method or just dont use it");
	}
	
	public boolean useBatchLogging() {
		return this.batchLogging;
	}

	public MainServerResource getMain() {
		return main;
	}

}

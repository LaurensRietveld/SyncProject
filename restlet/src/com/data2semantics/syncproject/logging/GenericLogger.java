package com.data2semantics.syncproject.logging;

import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.resources.Query;

public class GenericLogger {
	private boolean batchLogging;
	private MainServerResource main;
	public static final int PLAIN_TEXT_FILE = 1;
	public static final int DB = 2;
	public static final int EXPORT_GRAPHS = 3;
	public static final int CENTRAL_SERVER = 4;
	
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

package com.data2semantics.syncproject.slave.modes;

import java.io.File;

import com.data2semantics.syncproject.resources.UpdateSlave;
import com.data2semantics.syncproject.util.Util;
import com.typesafe.config.Config;

public class ExecuteQueriesFromText extends Mode implements ModeInterface {
	public static int MODE = 1;
	private File queriesFile;
	private File executedQueriesFile;
	private String delimiter;
	private String tripleStoreUri;
	
	public ExecuteQueriesFromText(UpdateSlave main, Config config, String key) throws Exception {
		super(main, config, key);
		
		//this.initPreparedStatements();
		queriesFile = new File(config.getString("slave.queryLogDir") + "/" + config.getString("queryLogMode.updateFile"));
		executedQueriesFile = new File(config.getString("slave.queryLogDir") + "/" + config.getString("queryLogMode.executedQueriesFile"));
		delimiter = config.getString("queryLogMode.queryDelimiter");
		tripleStoreUri = config.getString("slave.tripleStore.updateUri");
		runDaemon();
	}

	/**
	 * Start daemon (infinite loop)
	 * @throws Exception
	 */
	public void runDaemon() throws Exception {
		System.out.println(Util.getTime() + "- Running slave daemon in mode: " + Integer.toString(MODE));
		storeKey();
		while (true) {
			process();
			sleep(this.sleepInterval);
		}
	}
	
	/**
	 * Open and process any new changes in the file containing query logs. If files dont exist, create them
	 * @throws Exception 
	 */
	public void process() throws Exception {
		if (queriesFile.exists() && executedQueriesFile.exists()) {
			if ((int)queriesFile.length() != (int)executedQueriesFile.length()) {
				System.out.println(Util.getTime() + "execute queries (mode" + Integer.toString(MODE) + ")");
				Util.processTextFileChanges(queriesFile, executedQueriesFile, delimiter, tripleStoreUri);
				System.out.println(Util.getTime() + "done");
				storeExperimentInfo(MODE);
			}
		} else {
			System.out.println("no files exist to write to and read from");
		}
	}
	
		

}

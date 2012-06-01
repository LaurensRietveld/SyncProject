package com.data2semantics.syncproject.daemon.slave.modes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.data2semantics.syncproject.daemon.slave.util.Util;
import com.typesafe.config.Config;

public class SlaveTextQuery extends SlaveMode implements ModeInterface {
	public static int MODE = 1;
	private File queriesFile;
	private File executedQueriesFile;
	private String delimiter;
	private String tripleStoreUri;
	
	public SlaveTextQuery(Config config) throws Exception {
		super(config);
		
		
		//this.initPreparedStatements();
		queriesFile = new File(config.getString("slave.queryLogDir") + "/" + config.getString("mode1.updateFile"));
		executedQueriesFile = new File(queriesFile.getAbsolutePath() + ".old");
		delimiter = config.getString("mode1.queryDelimiter");
		tripleStoreUri = config.getString("slave.tripleStore.updateUri");
		runDaemon();
	}

	/**
	 * Start daemon (infinite loop)
	 * @throws Exception
	 */
	public void runDaemon() throws Exception {
		System.out.println("Running slave daemon in mode: " + Integer.toString(MODE));
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
//			if (!executedQueriesFile.exists() && (int)queriesFile.length() < 4) {
//				Runtime.getRuntime().exec("cp " + queriesFile.getAbsolutePath() + " " + executedQueriesFile.getAbsolutePath());
//				sleep(2);
//			}
			if ((int)queriesFile.length() != (int)executedQueriesFile.length()) {
				System.out.print(".");
				Util.processTextFileChanges(queriesFile, executedQueriesFile, delimiter, tripleStoreUri);
				System.out.println(".");
				storeExperimentInfo(MODE);
			}
		} else {
			System.out.println("no files exist to write to and read from");
		}
	}
	
		

}

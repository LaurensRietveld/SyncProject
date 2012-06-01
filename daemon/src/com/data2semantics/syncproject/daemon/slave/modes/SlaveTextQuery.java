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
		if (!queriesFile.exists()) {
			queriesFile.createNewFile();
		}

		if ((int)queriesFile.length() < 3) {
			//ugly workaround. On creation of the first file, the length seems to differ depending on whether java/shell/php creates it.
			//So, when the file is close to empty, do not create a new 'executedqueries' file but copy the old one.
			Runtime.getRuntime().exec("cp " + queriesFile.getAbsolutePath() + " " + executedQueriesFile.getAbsolutePath());
			sleep(2);
		}
		if ((int)queriesFile.length() != (int)executedQueriesFile.length()) {
			System.out.print(".");
			Util.processTextFileChanges(queriesFile, executedQueriesFile, delimiter, tripleStoreUri);
			System.out.println(".");
			storeExperimentInfo(MODE);
		}
	}
	
		

}

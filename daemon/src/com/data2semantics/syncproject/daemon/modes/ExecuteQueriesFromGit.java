package com.data2semantics.syncproject.daemon.modes;

import java.io.File;
import java.io.IOException;

import com.data2semantics.syncproject.daemon.util.Util;
import com.typesafe.config.Config;

public class ExecuteQueriesFromGit extends Mode implements ModeInterface {
	public static int MODE = 4;
	private String delimiter;
	private String tripleStoreUri;
	private File gitPath;
	private File executedQueries;
	private File queriesFile;
	private ProcessBuilder gitPull;

	public ExecuteQueriesFromGit(Config config) throws Exception {
		super(config);
		this.gitPath = new File(config.getString("slave.git.dir") + "/" + config.getString("slave.git.repoDir"));
		if (!gitPath.exists()) {
			throw new IOException("Git path does not exist: " + gitPath.getAbsolutePath());
		}
		this.executedQueries = new File(config.getString("slave.git.dir") + "/" + config.getString("queryLogMode.executedQueriesFile"));
		if (!executedQueries.exists()) {
			System.out.println("File to store executed queries in does not exist. Making one.");
			executedQueries.createNewFile();
		}
		this.queriesFile = new File(gitPath.getAbsoluteFile() + "/" + config.getString("queryLogMode.updateFile"));
		delimiter = config.getString("queryLogMode.queryDelimiter");
		tripleStoreUri = config.getString("slave.tripleStore.updateUri");
		
		//Set pull command
        String[] pullCmd = new String[]{"git", "pull"};
        gitPull = new ProcessBuilder(pullCmd);
        gitPull.directory(gitPath);
        
		runDaemon();
	}

	/**
	 * Start daemon (infinite loop)
	 * @throws Exception
	 */
	public void runDaemon() throws Exception {
		System.out.println(Util.getTime() + "- Running slave daemon in mode: " + Integer.toString(MODE));
		while (true) {
			process();
			sleep(this.sleepInterval);
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void process() throws Exception {
		boolean changed = Util.executeCmd(gitPull);
		
		if (!queriesFile.exists()) {
//			System.out.println("File containing queries executed on master does not exist. Wait.");
		} else if (changed) {
			//Something changed
			System.out.println(Util.getTime() + "execute queries (mode" + Integer.toString(MODE) + ")");
			Util.processTextFileChanges(queriesFile, executedQueries, delimiter, tripleStoreUri);
			System.out.println(Util.getTime() + "done");
			storeExperimentInfo(MODE);
		}
		
		
		
	}
	

}

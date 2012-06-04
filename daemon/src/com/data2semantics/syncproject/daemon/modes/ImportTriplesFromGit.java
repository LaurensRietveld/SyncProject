package com.data2semantics.syncproject.daemon.modes;

import java.io.File;
import java.io.IOException;
import com.data2semantics.syncproject.daemon.util.Util;
import com.typesafe.config.Config;

public class ImportTriplesFromGit extends Mode implements ModeInterface {
	public static int MODE = 5;
	private File dumpFile;
	private String updateUri;
	private File gitPath;
	private ProcessBuilder gitPull;
	
	public ImportTriplesFromGit(Config config) throws Exception {
		super(config);
		
		this.gitPath = new File(config.getString("slave.git.dir") + "/" + config.getString("slave.git.repoDir"));
		if (!gitPath.exists()) {
			throw new IOException("Git path does not exist: " + gitPath.getAbsolutePath());
		}
		this.dumpFile = new File(gitPath.getAbsolutePath() + "/" + config.getString("serializationMode.dumpFile"));
		this.updateUri = config.getString("slave.tripleStore.updateUri");
		
        String[] pullCmd = new String[]{"git", "pull"};
        gitPull = new ProcessBuilder(pullCmd);
        gitPull.directory(gitPath);
		
		runDaemon();
	}
	
	
	/**
	 * Process xml graph dump. Always inserts graph on first run. After that, check if file has been modified, and ignores it if not.
	 * @throws Exception 
	 */
	public void process() throws Exception {
		boolean changed = Util.executeCmd(gitPull);
		if (!dumpFile.exists()) {
			//System.out.println("WARNING: dump file does not exist");
		} else if (changed) {
			System.out.print(Util.getTime() + "importing (mode" + Integer.toString(MODE) +")  ==> ");
			Util.importDumpFile(dumpFile, updateUri);
			System.out.println(Util.getTime() + "done");
			storeExperimentInfo(MODE);
		}
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
}

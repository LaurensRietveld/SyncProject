package com.data2semantics.syncproject.daemon.slave.modes;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.data2semantics.syncproject.daemon.slave.util.Util;
import com.typesafe.config.Config;

public class SlaveGit extends SlaveMode implements ModeInterface {
	public static int MODE = 4;
	private String delimiter;
	private String tripleStoreUri;
	private File gitPath;
	private File executedQueries;
	private File queriesFile;
	private ProcessBuilder gitPull;
//	ProcessBuilder gitPush;
//	ProcessBuilder gitCommit;
	public SlaveGit(Config config) throws Exception {
		super(config);
		this.gitPath = new File(config.getString("slave.gitDir") + "/" + config.getString("mode4.repoDir"));
		if (!gitPath.exists()) {
			throw new IOException("Git path does not exist: " + gitPath.getAbsolutePath());
		}
		this.executedQueries = new File(config.getString("slave.gitDir") + "/" + config.getString("mode4.executedQueries"));
		if (!executedQueries.exists()) {
			System.out.println("File to store executed queries in does not exist. Making one.");
			executedQueries.createNewFile();
		}
		this.queriesFile = new File(gitPath.getAbsoluteFile() + "/" + config.getString("mode4.updateFile"));
		delimiter = config.getString("mode1.queryDelimiter");
		tripleStoreUri = config.getString("slave.tripleStore.updateUri");
		
		//Set pull command
        String[] pullCmd = new String[]{"git", "pull"};
        gitPull = new ProcessBuilder(pullCmd);
        gitPull.directory(gitPath);
        
//        //Set push command
//        String[] pushCmd = new String[]{"git", "push"};
//        gitPush = new ProcessBuilder(pushCmd);
//        gitPush.directory(gitPath);
//        
//        //Set commit command
//        String[] commitCmd = new String[]{"git", "commit", "."};
//        gitCommit = new ProcessBuilder(commitCmd);
//        gitCommit.directory(gitPath);
        
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
	 * 
	 * @throws Exception
	 */
	public void process() throws Exception {
		boolean changed = executeCmd(gitPull);
		
		if (!queriesFile.exists()) {
			System.out.println("File containing queries executed on master does not exist. Wait.");
		} else if (changed) {
			//Something changed
			Util.processTextFileChanges(queriesFile, executedQueries, delimiter, tripleStoreUri);
		}
		
		
		
	}
	
	/**
	 * Execute command (e.g. git pull, push or commit)
	 * 
	 * @param cmd
	 * @throws Exception In case git encounters error (e.g. failed merges)
	 * 
	 * @returns boolean False if nothing changed ('already up to date'), true otherwise
	 */
	public boolean executeCmd(ProcessBuilder cmd) throws Exception {
		boolean result = true;
        Process process;
		process = cmd.start();
        int val = process.waitFor();

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        
        while ((line = br.readLine()) != null) {
        	if (val != 0) {
        		//Output this for debugging purposes. Something went wrong.
        		System.out.println(line);
        		continue;
        	}
        	if (line.equals("Already up-to-date.")) {
        		result = false;
        	}
        	break;
        }
        if (val != 0) {
            throw new Exception("Exception excecuting " + cmd.command().toString() +"; return code = " + val);
        }
        return result;
	}
}

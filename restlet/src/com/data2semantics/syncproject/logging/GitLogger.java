package com.data2semantics.syncproject.logging;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.resources.Query;
import com.data2semantics.syncproject.util.Util;
import com.typesafe.config.Config;

public class GitLogger extends GenericLogger{
	
	private ProcessBuilder gitPush;
	private ProcessBuilder gitCommit;
	private ProcessBuilder gitAdd;
	private Config config;
	private File gitPath;
	private File logFile;
	
	public GitLogger(boolean batchLogging, MainServerResource main) throws IOException {
		super(batchLogging, main);
		
		gitPath = new File(config.getString("master.gitDir") + "/" + config.getString("mode4.repoDir"));
		if (!gitPath.exists() || !gitPath.canExecute()) {
			throw new IOException("Git dir does not exist, or cannot execute");
		}
		logFile = new File(gitPath.getAbsolutePath() + "/" + config.getString("mode4.updateFile"));
		
		
		config = main.getApplication().getConfig();
		//Set push command
		String[] pushCmd = new String[]{"git", "push"};
		gitPush = new ProcessBuilder(pushCmd);
		gitPush.directory(gitPath);
		 
		//Set commit command
		String[] commitCmd = new String[]{"git", "commit", "-m", "'" + getTime() + "'", logFile.getName()};
		gitCommit = new ProcessBuilder(commitCmd);
		gitCommit.directory(gitPath);
		
		//Set add command
		String[] addCmd = new String[]{"git", "add", logFile.getName()};
		gitAdd = new ProcessBuilder(addCmd);
		gitAdd.directory(gitPath);
		
		
	}
	
	/**
	 * Log query by first writing query to file, and then committing/pushing it to a git server
	 * 
	 * @param query
	 * @throws Exception 
	 */
	public void log(Query query) throws Exception {
		if (!logFile.exists()) {
	    	query.getLogger().warning("Log file does not existing. Creating one: " + logFile.getPath());
	    	logFile.createNewFile();
	    	executeCmd(gitAdd);
		}
		Util.writeToFile(query.getLogger(), logFile, config.getString("mode1.queryDelimiter") + query.getSparqlQuery());
		

	}
	
	public void loggingCallback() throws Exception {
		executeCmd(gitCommit);
		executeCmd(gitPush);
	}
	
	private String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		Date dt = new Date();
		return sdf.format(dt); // formats to 09/23/2009 13:53:28
	}
	
	
	/**
	 * Execute command (e.g. git pull, push or commit)
	 * 
	 * @param cmd
	 * @throws Exception In case git encounters error (e.g. failed merges)
	 * 
	 * @returns boolean False if nothing changed ('already up to date'), true otherwise
	 */
	private boolean executeCmd(ProcessBuilder cmd) throws Exception {
		boolean result = true;
        Process process;
		process = cmd.start();
        int val = process.waitFor();

        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        String output = "";
        while ((line = br.readLine()) != null) {
        	if (val != 0) {
        		//Output this for debugging purposes. Something went wrong.
        		output += line;
        		continue;
        	}
        	if (line.equals("Already up-to-date.")) {
        		result = false;
        	}
        	break;
        }
        if (val != 0) {
            throw new Exception("Exception excecuting " + cmd.command().toString() +"; return code = " + val + "; command output = " + output);
        }
        return result;
	}
}
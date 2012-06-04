package com.data2semantics.syncproject.logging;


import java.io.File;
import java.io.IOException;
import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.resources.Query;
import com.data2semantics.syncproject.util.Util;
import com.typesafe.config.Config;

public class LogQueriesGit extends GenericLogger{
	
	private ProcessBuilder gitPush;
	private ProcessBuilder gitCommit;
	private ProcessBuilder gitAdd;
	private Config config;
	private File gitPath;
	private File logFile;
	
	public LogQueriesGit(boolean batchLogging, MainServerResource main) throws IOException {
		super(batchLogging, main);
		config = main.getApplication().getConfig();
		gitPath = new File(config.getString("master.git.dir") + "/" + config.getString("master.git.repoDir"));
		if (!gitPath.exists() || !gitPath.canExecute()) {
			throw new IOException("Git dir does not exist, or cannot execute");
		}
		logFile = new File(gitPath.getAbsolutePath() + "/" + config.getString("queryLogMode.updateFile"));
		
		//Set push command
		String[] pushCmd = new String[]{"git", "push"};
		gitPush = new ProcessBuilder(pushCmd);
		gitPush.directory(gitPath);
		 
		//Set commit command
		String[] commitCmd = new String[]{"git", "commit", "-m", "'" + Util.getTime() + "'", logFile.getName()};
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
	    	Util.executeCmd(gitAdd);
		}
		Util.writeQueryToFile(query.getLogger(), logFile, config.getString("queryLogMode.queryDelimiter") + query.getSparqlQuery());
		

	}
	
	public void loggingCallback() throws Exception {
		int i = 0;
		while (i < 5) {
			try {
				
				Util.executeCmd(gitCommit);
				Util.executeCmd(gitPush);
			} catch (Exception e) {
				i++;
				getMain().getLogger().severe("Exception, but retrying... " + e.getMessage());
			}
		}
		if (i == 5) {
			throw new Exception("Unable to perform git and push commands, even after 5 tries.");
		}
	}

}

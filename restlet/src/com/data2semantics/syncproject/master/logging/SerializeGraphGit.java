package com.data2semantics.syncproject.master.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.util.QueryTypes;
import com.data2semantics.syncproject.util.Util;
import com.hp.hpl.jena.query.ResultSet;
import com.typesafe.config.Config;


public class SerializeGraphGit extends GenericLogger{
	private ProcessBuilder gitPush;
	private ProcessBuilder gitCommit;
	private ProcessBuilder gitAdd;
	private Config config;
	private File gitPath;
	private File exportFile;
	private String endpoint;
	public SerializeGraphGit(boolean batchLogging, MainServerResource main) throws IOException {
		super(batchLogging, main);
		config = main.getApplication().getConfig();
		endpoint = config.getString("master.tripleStore.selectUri");
		gitPath = new File(config.getString("master.git.dir") + "/" + config.getString("master.git.repoDir"));
		if (!gitPath.exists() || !gitPath.canExecute()) {
			throw new IOException("Git dir does not exist, or cannot execute");
		}
		exportFile = new File(gitPath.getAbsolutePath() + "/" + config.getString("serializationMode.dumpFile"));
		//Set push command
		String[] pushCmd = new String[]{"git", "push"};
		gitPush = new ProcessBuilder(pushCmd);
		gitPush.directory(gitPath);
		 
		//Set commit command
		String[] commitCmd = new String[]{"git", "commit", "-m", "'" + Util.getTime() + "'", exportFile.getName()};
		gitCommit = new ProcessBuilder(commitCmd);
		gitCommit.directory(gitPath);
		
		//Set add command
		String[] addCmd = new String[]{"git", "add", exportFile.getName()};
		gitAdd = new ProcessBuilder(addCmd);
		gitAdd.directory(gitPath);
	}
	

    public void log(com.data2semantics.syncproject.resources.Query query) throws Exception {
    	//Do nothing. No need to perform logging for each query, as the query itself is not logged.
    }
    
    
	public void loggingCallback() throws Exception {
		//No need to export when data hasnt changed (i.e. only on update)
		if (getMain().getSparqlQueryType() == QueryTypes.UPDATE) {
			
	    	//empty file if it exists (do not create new one: file permissions get screwed up)
	    	if (exportFile.exists()) {
	            BufferedWriter bw = new BufferedWriter(new FileWriter(exportFile));
	            bw.write("");
	            bw.flush();
	            bw.close();
	    	} else {
	    		exportFile.createNewFile();
	    		Util.executeCmd(gitAdd);
	    	}
	    	ResultSet result = Util.query(endpoint, "SELECT * WHERE {?subject ?predicate ?object}");
	    	getMain().getLogger().info("going to process query and write all results to file");
	    	Util.writeSerializationToFile(result, exportFile);
			Util.executeCmd(gitCommit);
			Util.executeCmd(gitPush);
		}
	}
}

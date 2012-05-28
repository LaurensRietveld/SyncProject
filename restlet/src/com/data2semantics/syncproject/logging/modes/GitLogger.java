package com.data2semantics.syncproject.logging.modes;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import com.data2semantics.syncproject.resources.Query;
import com.typesafe.config.Config;

public class GitLogger {

	
	
	/**
	 * Log query by first writing query to file, and then committing/pushing it to a git server
	 * 
	 * @param query
	 * @throws IOException
	 * @throws NoFilepatternException
	 * @throws NoHeadException
	 * @throws NoMessageException
	 * @throws ConcurrentRefUpdateException
	 * @throws JGitInternalException
	 * @throws WrongRepositoryStateException
	 * @throws InvalidRemoteException
	 */
	public static void log(Query query) throws IOException, NoFilepatternException, NoHeadException, NoMessageException, ConcurrentRefUpdateException, JGitInternalException, WrongRepositoryStateException, InvalidRemoteException {
		Config config = query.getApplication().getConfig();
		File logFile = new File(config.getString("master.git.dir") + "/" + "update.log");
		TextLogger.writeToFile(query.getLogger(), logFile, config.getString("mode1.queryDelimiter") + query.getSparqlQuery());
		
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		File gitDir = new File(query.getApplication().getConfig().getString("master.git.dir") + "/.git");
		if (!gitDir.exists() || !gitDir.canExecute()) {
			throw new IOException("Git dir does not exist, or cannot execute");
		}
		Repository repository = builder.setGitDir(gitDir)
		  .readEnvironment() // scan environment GIT_* variables
		  .findGitDir() // scan up the file system tree
		  .build();
		Git git = new Git(repository);
		
		git.add().addFilepattern(".").call();
		git.commit().setMessage("committed query on " + getTime()).call();
        git.push().call();
        
	}
	
	private static String getTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SS");
		Date dt = new Date();
		return sdf.format(dt); // formats to 09/23/2009 13:53:28.238
	}
}

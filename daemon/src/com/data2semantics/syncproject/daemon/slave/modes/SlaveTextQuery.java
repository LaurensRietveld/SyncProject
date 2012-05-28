package com.data2semantics.syncproject.daemon.slave.modes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import com.data2semantics.syncproject.daemon.slave.util.Query;
import com.typesafe.config.Config;

public class SlaveTextQuery extends SlaveMode implements ModeInterface {
	public static int MODE = 1;
	private File queriesFile;
	private File executedQueriesFile;
	private Config config;
	
	public SlaveTextQuery(Config config) {
		super(config);
		queriesFile = new File(config.getString("slave.queryLogDir") + "/" + config.getString("mode1.updateFile"));
		executedQueriesFile = new File(queriesFile.getAbsolutePath() + ".old");
	}

	/**
	 * Start daemon (infinite loop)
	 * @throws Exception
	 */
	public void runDaemon() throws Exception {
		System.out.println("Running slave daemon in mode: " + Integer.toString(MODE));
		while (true) {
			sleep(this.sleepInterval);
			process();
		}
	}
	
	/**
	 * Open and process any new changes in the file containing query logs. If files dont exist, create them
	 * @throws Exception 
	 */
	public void process() throws Exception {
		if (!queriesFile.exists()) {
			try {
				System.out.println("Log file to retrieve queries from does not exist. Creating new one: " + queriesFile.getPath());
				queriesFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!executedQueriesFile.exists()) {
			try {
				System.out.println("Log file with already executed queries does not exist. Creating new one: " + executedQueriesFile.getPath());
				executedQueriesFile.createNewFile();
			} catch (IOException e) {
				System.out.println("Failed creating file for old queries: " + e.getMessage());
				System.exit(1);
			}
		}
		if (queriesFile.length() != executedQueriesFile.length()) {
			//System.out.println("Something changed. Processing changes..\n");
			processChanges();
		}
	}
	
	/**
	 * Compare files, and execute differences (i.e. new queries)
	 * 
	 */
	private void processChanges() throws Exception {
		// Get the object of DataInputStream
		DataInputStream inSrc = new DataInputStream(new FileInputStream(queriesFile));
		DataInputStream inDest = new DataInputStream(new FileInputStream(executedQueriesFile));
		BufferedReader brSrc = new BufferedReader(new InputStreamReader(inSrc));
		BufferedReader brDest = new BufferedReader(new InputStreamReader(inDest));
		String srcLine;
		boolean firstline = true;
		String changes = "";
		// Read File Line By Line
		while ((srcLine = brSrc.readLine()) != null) {
			if (!srcLine.equals(brDest.readLine())) {
				changes += (firstline? "" : "\n") + srcLine;
			}
			firstline = false;

		}
		//System.out.println("Wrinting Changes: '" + changes + "'");
		inSrc.close();
		inDest.close();
		this.executeChanges(changes);
		this.storeChanges(changes);
			
	}
	
	/**
	 * Execute string containing changes
	 * 
	 * @param changes String containing changes
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private void executeChanges(String changes) throws IOException {
		String[] queries = changes.split(this.config.getString("mode1.queryDelimiter"));
		for (String query: queries) {
			query = query.trim();
			if (query.length() > 0) {
				System.out.println("Executing: " + query);
				Query.executeQuery(config.getString("slave.tripleStore.updateUri"), query);
			}
		}
	}
	
	/**
	 * Store (the just executed) changes in log file, to keep track of what has been executed
	 * 
	 * @param changes
	 * @param executedQueriesFile
	 * @throws IOException 
	 */
	private void storeChanges(String changes) throws IOException {
		FileWriter fw;
		fw = new FileWriter(executedQueriesFile, true);
	    BufferedWriter bw = new BufferedWriter(fw);
	    bw.write(changes);
	    bw.close();
	}
}

package com.data2semantics.syncproject.slave;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class SlaveDaemon {
	static final String DEFAULT_CONFIG_FILE = "https://raw.github.com/LaurensRietveld/SyncProject/master/WebContent/WEB-INF/config/config.conf";
	private URL configFile;
	private Config config;
	
	SlaveDaemon() {
		try {
			this.configFile = new URL(DEFAULT_CONFIG_FILE);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
		}
	}
	SlaveDaemon(String configFile) {
		try {
			this.configFile = new URL(configFile);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void runDaemon() {
		loadConfigFile();
		while (true) {
			sleep(this.config.getInt("slave.daemon.checkInterval"));
			processQueryFile();
			
		}
	}
	
	private void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	private void loadConfigFile() {
		//Load typesafe config
		try {
			this.config = ConfigFactory.parseURL(configFile);
		} catch (Exception e) {
			System.out.println("ERROR: Failed loading config from " + this.configFile.toString() + ": " + e.getMessage());
			System.exit(1);
		}
	}
	
	private void processQueryFile() {
		File logFile = new File(this.config.getString("slave.queryLogDir") + "/update.log");
		File oldQueriesFile = new File(this.config.getString("slave.queryLogDir") + "/update.log.old");
		if (!logFile.exists()) {
			System.out.println("ERROR: Log file to retrieve queries from does not exist: " + logFile.getName());
			System.exit(1);
		}
		if (!oldQueriesFile.exists()) {
			try {
				oldQueriesFile.createNewFile();
			} catch (IOException e) {
				System.out.println("Failed creating file for old queries: " + e.getMessage());
				System.exit(1);
			}
		}
		if (logFile.length() != oldQueriesFile.length()) {
			System.out.println("Something changed. Processing changes..\n");
			processChanges(logFile, oldQueriesFile);
		}
	}
	
	private void processChanges(File srcFile, File destFile) {
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream srcStream = new FileInputStream(srcFile);
			FileInputStream destStream = new FileInputStream(destFile);
			// Get the object of DataInputStream
			DataInputStream inSrc = new DataInputStream(srcStream);
			DataInputStream inDest = new DataInputStream(destStream);
			BufferedReader brSrc = new BufferedReader(new InputStreamReader(inSrc));
			BufferedReader brDest = new BufferedReader(new InputStreamReader(inDest));
			String strLineSrc;
			String changes = "";
			// Read File Line By Line
			while ((strLineSrc = brSrc.readLine()) != null) {
				if (strLineSrc != brDest.readLine()) {
					//Lines are different: save these lines
					changes += strLineSrc + "\n";
				}
			}
			// Close the input stream
			inSrc.close();
			inDest.close();
			
			this.executeChanges(changes);
			this.storeChanges(changes, destFile);
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	private void executeChanges(String changes) {
		System.out.println("executing changes: " + changes);
		String[] queries = changes.split(this.config.getString("queryDelimiter"));
		for (int i = 0; i < queries.length; i++) {
			
		}
	}
	
	private void storeChanges(String changes, File destFile) {
		FileWriter fw;
		try {
			fw = new FileWriter(destFile, true);
		    BufferedWriter bw = new BufferedWriter(fw);
		    bw.write(changes);
		    bw.close();
		} catch (IOException e) {
			System.out.println("failed writing log changes to .old file: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SlaveDaemon daemon;
		if (args.length > 0) {
			daemon = new SlaveDaemon(args[0]);
		} else {
			System.out.println("No config file passed on as parameter. Using " + SlaveDaemon.DEFAULT_CONFIG_FILE);
			daemon = new SlaveDaemon();
		}
		daemon.runDaemon();
		
	}

}

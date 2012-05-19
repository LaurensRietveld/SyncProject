package com.data2semantics.syncproject.daemon.slave;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.client.ClientProtocolException;

import com.data2semantics.syncproject.daemon.slave.util.Query;
import com.typesafe.config.Config;

public class SlaveDaemon {
	protected Config config;
	protected int mode;
	protected int checkInterval;
	public SlaveDaemon(Config config, int mode) {
		this.config = config;
		this.mode = mode;
		this.checkInterval = config.getInt("slave.daemon.checkInterval");
	}
	
	public void runDaemon() {
		System.out.println("Running slave daemon");
		while (true) {
			sleep(this.checkInterval);
			processFiles();
		}
	}
	
	protected void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	
	protected void processFiles() {
		System.out.println("Missing implementation of processFiles");
		System.exit(1);
	}
	
	private void processChanges(File srcFile, File destFile) {
		try {
			// Get the object of DataInputStream
			DataInputStream inSrc = new DataInputStream(new FileInputStream(srcFile));
			DataInputStream inDest = new DataInputStream(new FileInputStream(destFile));
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
			try {
				this.executeChanges(changes);
				this.storeChanges(changes, destFile);
			} catch (Exception e) {
				System.out.println("Failed executing query on slave");
				e.printStackTrace();
				System.exit(1);
			}
			
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	private void executeChanges(String changes) throws ClientProtocolException, IOException {
		String[] queries = changes.split(this.config.getString("master1.queryDelimiter"));
		for (String query: queries) {
			query = query.trim();
			if (query.length() > 0) {
				System.out.println("Executing: " + query);
				Query.executeQuery(config.getString("slave.tripleStore"), query);
			}
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
}

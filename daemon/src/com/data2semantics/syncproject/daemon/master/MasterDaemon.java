package com.data2semantics.syncproject.daemon.master;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.typesafe.config.Config;

public class MasterDaemon {

	private Config config;
	
	public MasterDaemon(Config config) {
		this.config = config;
	}
	
	public void runDaemon() {
		System.out.println("Running master daemon");
		Map<String, File> files = new HashMap<String, File>();
		files.put("srcFile", new File(this.config.getString("master.queryLogDir") + "/update.log"));
		files.put("destFile", new File(this.config.getString("slave.queryLogDir") + "/update.log"));
		for (Map.Entry<String, File> entry : files.entrySet()) {
			File file = entry.getValue();
		    if (!file.exists()) {
		    	try {
		    		System.out.println("File does not exist. Creating new file: " + file.getPath());
					entry.getValue().createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
		    }
		}
		while (true) {
			rsync(files.get("srcFile"), files.get("destFile"));
			sleep(this.config.getInt("master.daemon.syncInterval"));
			
		}
	}
	
	private void rsync(File srcFile, File destFile) {
		// Currently uses passwordless SSH keys to login to sword
        String[] cmd = new String[]{"rsync", "-a", srcFile.getPath(), destFile.getPath()};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p;
		try {
			p = pb.start();
	        int val = p.waitFor();
	        if (val != 0) {
	            throw new Exception("Exception during RSync; return code = " + val);
	        }
		} catch (Exception e) {
			System.out.println("Failed performing rsync: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
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
}

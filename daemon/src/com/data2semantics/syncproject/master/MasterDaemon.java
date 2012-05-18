package com.data2semantics.syncproject.master;

import java.net.MalformedURLException;
import java.net.URL;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class MasterDaemon {
	static final String DEFAULT_CONFIG_FILE = "https://raw.github.com/LaurensRietveld/SyncProject/master/WebContent/WEB-INF/config/config.conf";
	private URL configFile;
	private Config config;
	
	MasterDaemon() {
		try {
			this.configFile = new URL(DEFAULT_CONFIG_FILE);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
		}
	}
	MasterDaemon(String configFile) {
		try {
			this.configFile = new URL(configFile);
		} catch (MalformedURLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void runDaemon() {
		loadConfigFile();
		String srcFile = this.config.getString("master.queryLogDir") + "/update.log";
		String destFile = this.config.getString("slave.queryLogDir") + "/update.log";
		while (true) {
			rsync(srcFile, destFile);
			sleep(this.config.getInt("master.daemon.syncInterval"));
			
		}
	}
	
	private void rsync(String srcFile, String destFile) {
		// Currently uses passwordless SSH keys to login to sword
        String[] cmd = new String[]{"rsync", "-a", srcFile, destFile};
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
	private void loadConfigFile() {
		//Load typesafe config
		try {
			this.config = ConfigFactory.parseURL(configFile);
		} catch (Exception e) {
			System.out.println("ERROR: Failed loading config from " + this.configFile.toString() + ": " + e.getMessage());
			System.exit(1);
		}
	}
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MasterDaemon daemon;
		if (args.length > 0) {
			daemon = new MasterDaemon(args[0]);
		} else {
			System.out.println("No config file passed on as parameter. Using " + MasterDaemon.DEFAULT_CONFIG_FILE);
			daemon = new MasterDaemon();
		}
		daemon.runDaemon();
		
	}

}

package com.data2semantics.syncproject.daemon;

import java.net.URL;

import com.data2semantics.syncproject.daemon.slave.SlaveDaemon;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class Daemon {
	static final String DEFAULT_CONFIG_FILE = "https://raw.github.com/LaurensRietveld/SyncProject/master/WebContent/WEB-INF/config/config.conf";
	
	Daemon (String mode) {
		
	}
	
	Daemon(String mode, String configFile) {
		
	}
	
	private URL configFile;
	private Config config;
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
		Daemon daemon;
//		if (args.length > 0) {
//			daemon = new Daemon(args[0]);
//		} else {
//			System.out.println("No config file passed on as parameter. Using " + Daemon.DEFAULT_CONFIG_FILE);
//			daemon = new Daemon();
//		}
	}

}

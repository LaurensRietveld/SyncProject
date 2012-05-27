package com.data2semantics.syncproject.daemon.slave;

import com.typesafe.config.Config;

public class SlaveDaemon {
	public Config config;
	public int mode;
	public int checkInterval;
	public SlaveDaemon(Config config, int mode) {
		this.config = config;
		this.mode = mode;
		this.checkInterval = config.getInt("slave.daemon.checkInterval");
	}
	
	/**
	 * Sleep for x seconds
	 * 
	 * @param seconds
	 */
	protected void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * Start daemon (infinite loop)
	 */
	public void runDaemon() {
		System.out.println("Running slave daemon in mode: " + Integer.toString(mode));
		while (true) {
			sleep(this.checkInterval);
			process();
		}
	}
	/**
	 * 
	 */
	public void process() {}

	
}

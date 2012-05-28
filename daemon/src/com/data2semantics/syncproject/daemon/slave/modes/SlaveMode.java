package com.data2semantics.syncproject.daemon.slave.modes;

import com.typesafe.config.Config;

public class SlaveMode {
	protected Config config;
	protected int sleepInterval;
	
	SlaveMode(Config config) {
		this.config = config;
		this.sleepInterval = config.getInt("slave.daemon.checkInterval");	
	}
	
	/**
	 * Sleep for x seconds
	 * 
	 * @param seconds
	 */
	public static void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
}

package com.data2semantics.syncproject.daemon.master;

import com.typesafe.config.Config;

public class MasterDaemon {

	private Config config;
	
	public MasterDaemon(Config config) {
		this.config = config;
	}
	
	public void runDaemon() {
		System.out.println("Running master daemon");
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
}

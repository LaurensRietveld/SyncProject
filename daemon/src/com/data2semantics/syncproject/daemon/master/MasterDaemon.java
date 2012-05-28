package com.data2semantics.syncproject.daemon.master;

import java.io.File;
import java.io.IOException;

import com.typesafe.config.Config;

public class MasterDaemon {

	private Config config;
	private int mode;
	public MasterDaemon(Config config, int mode) {
		this.config = config;
		this.mode = mode;
	}
	
	public void runDaemon() {
		System.out.println("Running master daemon in mode: " + Integer.toString(this.mode));
		File srcFile = null; //src file is on same server as daemon, so use regular File object
		String destFile = null; //Dest file is (probably) on other server. Use string instead
		if (this.mode == 1) {
			srcFile = new File(this.config.getString("master.queryLogDir") + "/" + this.config.getString("mode1.updateFile"));
			destFile = this.config.getString("slave.serverLocation") + ":" + this.config.getString("slave.queryLogDir") + "/" + this.config.getString("mode1.updateFile");
		} else if (this.mode == 2) {
			System.out.println("Sync of database is handled by mysql replication. No need for this daemon. Exiting...");
			System.exit(0);
		} else if (this.mode == 3) {
			srcFile = new File(this.config.getString("master.xmlDumpDir") + "/" + this.config.getString("mode3.dumpFile"));
			destFile = this.config.getString("slave.serverLocation") + ":" + this.config.getString("slave.xmlDumpDir") + "/" + this.config.getString("mode3.dumpFile");
		} else {
			System.out.println("Invalid option to run master daemon in");
			System.exit(1);
		}
	    if (!srcFile.exists()) {
	    	try {
	    		System.out.println("File does not exist. Creating new file: " + srcFile.getPath());
				srcFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		while (true) {
			rsync(srcFile, destFile);
			sleep(this.config.getInt("master.daemon.syncInterval"));
		}
	}
	
	private void rsync(File srcFile, String destFile) {
		// Currently uses passwordless SSH keys to login to sword
        String[] cmd = new String[]{"rsync", "-a", srcFile.getPath(), destFile};
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

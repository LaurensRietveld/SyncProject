package com.data2semantics.syncproject.daemon.modes;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.typesafe.config.Config;

public class Mode {
	protected Config config;
	protected int sleepInterval;
	protected Connection expConnection;
	protected PreparedStatement insertExperimentInto;
	protected PreparedStatement insertKey;
	private String hostname;
	private String key;
	Mode(Config config, String key) throws Exception {
		this.hostname = java.net.InetAddress.getLocalHost().getHostName();
		this.key = key;
		this.config = config;
		this.sleepInterval = config.getInt("slave.daemon.checkInterval");	
		Class.forName(config.getString("experiments.db.javaDriver"));
		this.expConnection = DriverManager.getConnection(config.getString("experiments.db.connection"));
		//this.expConnection = DriverManager.getConnection("jdbc:mysql://localhost/Experiments?user=syncProject");
		this.expConnection.setAutoCommit(false);
		insertExperimentInto = expConnection.prepareStatement(
			"INSERT INTO Daemon (Mode,Node)\n" +
			"VALUES (?, ?)");
		insertKey = expConnection.prepareStatement("INSERT INTO DaemonRunning (`Key`) VALUES (?)");
	}
	
	/**
	 * Sleep for x milliseconds
	 * 
	 * @param milliseconds
	 */
	public static void sleep(int milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
	
	public void storeExperimentInfo(int mode) throws SQLException, UnknownHostException {
		insertExperimentInto.setInt(1, mode);
		insertExperimentInto.setString(2, hostname);
		insertExperimentInto.executeUpdate();
	}
	
	/**
	 * Store key in database. This way, the script running the experiments can check whether the daemon has finished initializing
	 * @throws SQLException 
	 */
	public void storeKey() throws SQLException {
		if (key.length() > 0) {
			insertKey.setString(1, key);
			insertKey.executeUpdate();
		}
	}
	
	
}

package com.data2semantics.syncproject.daemon.slave.modes;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.typesafe.config.Config;

public class SlaveMode {
	protected Config config;
	protected int sleepInterval;
	protected Connection expConnection;
	protected PreparedStatement expPreparedStatement;
	private String hostname;
	SlaveMode(Config config) throws Exception {
		this.hostname = java.net.InetAddress.getLocalHost().getHostName();
		this.config = config;
		this.sleepInterval = config.getInt("slave.daemon.checkInterval");	
		Class.forName(config.getString("experiments.db.javaDriver"));
//		this.expConnection = DriverManager.getConnection(config.getString("experiments.db.connection"));
		this.expConnection = DriverManager.getConnection("jdbc:mysql://localhost/Experiments?user=syncProject");
		this.expConnection.setAutoCommit(false);
		expPreparedStatement = expConnection.prepareStatement(
			"INSERT INTO Daemon (Mode,Node)\n" +
			"VALUES (?, ?)");
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
	
	public void storeExperimentInfo(int mode) throws SQLException, UnknownHostException {
		expPreparedStatement.setInt(1, mode);
		expPreparedStatement.setString(2, hostname);
		expPreparedStatement.executeUpdate();
	}
}

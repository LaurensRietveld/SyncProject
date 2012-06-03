package com.data2semantics.syncproject.daemon.modes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.data2semantics.syncproject.daemon.util.Util;
import com.typesafe.config.Config;

public class ImportTriplesFromDb extends Mode implements ModeInterface {
	public static int MODE = 6;
	private Connection connection;
	private HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	private String lastUpdate = "";
	public ImportTriplesFromDb(Config config) throws Exception {
		super(config);
		Class.forName(config.getString("slave.db.javaDriver"));
		this.connection = DriverManager.getConnection(config.getString("slave.db.connection"));
		this.connection.setAutoCommit(false);
		this.initPreparedStatements();
		runDaemon();
	}
	
	/**
	 * Start daemon (infinite loop)
	 * @throws Exception
	 */
	public void runDaemon() throws Exception {
		System.out.println("Running slave daemon in mode: " + Integer.toString(MODE));
		while (true) {
			process();
			sleep(this.sleepInterval);
		}
	}
	
	
	/**
	 * Open and process any new changes in the file containing query logs. If files dont exist, create them
	 * @throws IOException 
	 * @throws SQLException 
	 */
	public void process() throws IOException, SQLException {
		
		
		
		if (!tableLastUpdated().equals(lastUpdate)) {

			
			String queryString = "INSERT DATA {\n";
			
			ResultSet result = preparedStatements.get("getTriplesToImport").executeQuery();
			while (result.next()) {
				queryString += result.getString("subject") + " " +
						result.getString("predicate") + " " +
						result.getString("object") + " .\n";
			}
			if (queryString.length() > 0) {
				queryString = "INSERT DATA {\n" + queryString + "}";
				System.out.print(".");
				Util.executeQuery(config.getString("slave.tripleStore.updateUri"), queryString);
				System.out.println(".");
				storeExperimentInfo(MODE);
			}
		}
	}
	
	private void initPreparedStatements() throws SQLException {
		preparedStatements.put("getTriplesToImport", connection.prepareStatement("SELECT * FROM Serialization)"));
		preparedStatements.put("getLastUpdateTime", connection.prepareStatement("SHOW TABLE STATUS FROM SyncProject LIKE 'Serialization'"));
		//preparedStatements.put("deleteAllTriples", connection.prepareStatement("TRUNCATE TABLE Serialization"));
		//preparedStatements.put("setAsExecuted", connection.prepareStatement("INSERT INTO ExecutedOnSlave SET QueryId = ?"));
	}
	
	private String tableLastUpdated() throws SQLException {
		ResultSet result = preparedStatements.get("getLastUpdateTime").executeQuery();
		result.next();
		return result.getString("Update_time");
	}
	
}

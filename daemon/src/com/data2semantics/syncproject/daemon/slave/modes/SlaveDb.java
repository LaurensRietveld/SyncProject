package com.data2semantics.syncproject.daemon.slave.modes;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import com.data2semantics.syncproject.daemon.slave.util.Query;
import com.typesafe.config.Config;

public class SlaveDb extends SlaveMode implements ModeInterface {
	public static int MODE = 2;
	private Connection connection;
	private HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	public SlaveDb(Config config) throws Exception {
		super(config);
		Class.forName(config.getString("slave.db.javaDriver"));
		this.connection = DriverManager.getConnection(config.getString("slave.db.connection"));
		this.connection.setAutoCommit(false);
		this.initPreparedStatements();
	}
	
	/**
	 * Start daemon (infinite loop)
	 * @throws Exception
	 */
	public void runDaemon() throws Exception {
		System.out.println("Running slave daemon in mode: " + Integer.toString(MODE));
		while (true) {
			sleep(this.sleepInterval);
			process();
		}
	}
	
	
	/**
	 * Open and process any new changes in the file containing query logs. If files dont exist, create them
	 * @throws IOException 
	 */
	public void process() throws IOException {
		
		ResultSet newQueries;
		try {
			newQueries = preparedStatements.get("getNewQueries").executeQuery();
			boolean hasResult = false;
			while (newQueries.next()) {
				hasResult = true;
				String query = newQueries.getString("Query");
				Query.executeQuery(config.getString("slave.tripleStore.updateUri"), query);
				this.storeExecutedQuery(newQueries.getInt("QueryId"));
			}
			if (hasResult) {
				executeBatch();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void initPreparedStatements() throws SQLException {
		PreparedStatement preparedStatement;
		preparedStatement = connection.prepareStatement(
				"SELECT * FROM QueryLog\n" + 
						"WHERE QueryType = 'update' \n" + 
						"AND NOT EXISTS (\n" + 
							"SELECT * FROM ExecutedOnSlave \n" + 
							"WHERE QueryLog.QueryId = ExecutedOnSlave.QueryId)");
		preparedStatements.put("getNewQueries", preparedStatement);
		
		preparedStatement = preparedStatements.put("setAsExecuted", connection.prepareStatement("INSERT INTO ExecutedOnSlave SET QueryId = ?"));
	}
	
	private void storeExecutedQuery(int queryId) throws SQLException {
		PreparedStatement preparedStatement = preparedStatements.get("setAsExecuted");
		preparedStatement.setInt(1, queryId);
		preparedStatement.addBatch();
	}
	
	private void executeBatch() throws SQLException {
		int[] updateCounts = preparedStatements.get("setAsExecuted").executeBatch();
	    for (int i=0; i<updateCounts.length; i++) {
	        if (updateCounts[i] == Statement.EXECUTE_FAILED) {
	            System.out.println("ERROR: Failed to execute query..");
	            System.exit(1);
	        }
	    }
	}
}
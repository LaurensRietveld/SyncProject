package com.data2semantics.syncproject.daemon.slave;

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

public class SlaveDb extends SlaveDaemon {
	public static int MODE = 2;
	private Connection connection;
	private HashMap<String, PreparedStatement> preparedStatements = new HashMap<String, PreparedStatement>();
	
	public SlaveDb(Config config) {
		super(config, MODE);
		// This will load the MySQL driver, each DB has its own driver
		try {
			Class.forName(config.getString("slave.db.javaDriver"));
			this.connection = DriverManager.getConnection(config.getString("slave.db.connection"));
			this.connection.setAutoCommit(false);
			this.initPreparedStatements();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Open and process any new changes in the file containing query logs. If files dont exist, create them
	 */
	public void process() {
		
		ResultSet newQueries;
		try {
			newQueries = preparedStatements.get("getNewQueries").executeQuery();
			boolean hasResult = false;
			while (newQueries.next()) {
				hasResult = true;
				String query = newQueries.getString("Query");
				try {
					Query.executeQuery(config.getString("slave.tripleStore.updateUri"), query);
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
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
	
	private void storeExecutedQuery(int queryId) {
		PreparedStatement preparedStatement = preparedStatements.get("setAsExecuted");
		try {
			preparedStatement.setInt(1, queryId);
			preparedStatement.addBatch();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void executeBatch() {
		try {
			int[] updateCounts = preparedStatements.get("setAsExecuted").executeBatch();
		    for (int i=0; i<updateCounts.length; i++) {
		        if (updateCounts[i] == Statement.EXECUTE_FAILED) {
		            System.out.println("ERROR: Failed to execute query..");
		            System.exit(1);
		        }
		    }
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(1);
		}

	}
}

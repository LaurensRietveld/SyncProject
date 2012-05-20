package com.data2semantics.syncproject.logging.modes;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.data2semantics.syncproject.resources.Query;
import com.typesafe.config.Config;

public class DbLogger {

	/**
	 * Log a query. This only occurs when query is executed on server. However, the query may have failed! 
	 * TODO: check whether query response contains errors. If so, probably do not store (or store separately)..
	 * 
	 * @param query Query String
	 * @param queryType Type of query, either select or update
	 * @throws IOException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public static void log(Query query) throws IOException, SQLException, ClassNotFoundException {
		Config config = query.getApplication().getConfig();

		// This will load the MySQL driver, each DB has its own driver
		Class.forName(config.getString("master.db.javaDriver"));
		// Setup the connection with the DB
		Connection connect = DriverManager.getConnection(config.getString("master.db.connection"));

		PreparedStatement preparedStatement = connect.prepareStatement(
				"INSERT INTO QueryLog (QueryType,Query)\n" +
				"VALUES (?, ?)");
		
		preparedStatement.setString(1, query.getSparqlQueryType());
		preparedStatement.setString(2, query.getSparqlQuery());
		preparedStatement.executeUpdate();
	}
}

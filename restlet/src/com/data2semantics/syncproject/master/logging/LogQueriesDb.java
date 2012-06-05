package com.data2semantics.syncproject.master.logging;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import com.typesafe.config.Config;
import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.resources.Query;
import java.sql.SQLException;
import java.io.IOException;

public class LogQueriesDb extends GenericLogger{

	public LogQueriesDb(boolean batchLogging, MainServerResource main) {
		super(batchLogging, main);
	}
	
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
	public void log(Query query) throws IOException, SQLException, ClassNotFoundException {
		Config config = query.getConfig();

		// This will load the MySQL driver, each DB has its own driver
		Class.forName(config.getString("master.db.javaDriver"));
		// Setup the connection with the DB
		Connection connect = DriverManager.getConnection(config.getString("master.db.connection"));

		PreparedStatement preparedStatement = connect.prepareStatement(
				"INSERT INTO QueryLog (QueryType,Query)\n" +
				"VALUES (?, ?)");
		
		preparedStatement.setString(1, query.getMain().getSparqlQueryType());
		preparedStatement.setString(2, query.getSparqlQuery());
		preparedStatement.executeUpdate();
		query.getLogger().info("executed DB query");
		connect.close();
	}
	
	public void loggingCallback() {
		//Do nothing. The callback function is essentially done by mysql, which already replicates the data to the slave
	}
}

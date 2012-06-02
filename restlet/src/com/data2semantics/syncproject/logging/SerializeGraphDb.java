package com.data2semantics.syncproject.logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.data2semantics.syncproject.resources.MainServerResource;
import com.data2semantics.syncproject.util.QueryTypes;
import com.data2semantics.syncproject.util.Util;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.typesafe.config.Config;


public class SerializeGraphDb extends GenericLogger{
	Config config;
	Connection connection;
	public SerializeGraphDb(boolean batchLogging, MainServerResource main) throws ClassNotFoundException, SQLException {
		super(batchLogging, main);
		config = main.getApplication().getConfig();
		// This will load the MySQL driver, each DB has its own driver
		Class.forName(config.getString("master.db.javaDriver"));
		// Setup the connection with the DB
		connection = DriverManager.getConnection(config.getString("master.db.connection"));
		connection.setAutoCommit(false);
	}
	

    public void log(com.data2semantics.syncproject.resources.Query query) throws Exception {
    	//Do nothing. No need to perform logging for each query, as the query itself is not logged.
    }
    
    
	public void loggingCallback() throws Exception {
		//No need to export when data hasnt changed (i.e. only on update)
		if (getMain().getSparqlQueryType() == QueryTypes.UPDATE) {
	    	String endpoint = config.getString("master.tripleStore.selectUri");
	    	ResultSet result = Util.query(endpoint, "SELECT * WHERE {?subject ?predicate ?object}");
	    	getMain().getLogger().info("going to process query and insert results in DB");
	    	clearDb();
	    	writeToDb(result);
		}
		
		
	}
	
	private void writeToDb(ResultSet result) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"INSERT INTO Serialization (Subject, Predicate, Object)\n" +
				"VALUES (?, ?, ?)");
		while (result.hasNext()) {
			QuerySolution solution = result.next();
			preparedStatement.setString(1, Util.getNodeAsString(solution.get("subject")));
			preparedStatement.setString(2, Util.getNodeAsString(solution.get("predicate")));
			preparedStatement.setString(3, Util.getNodeAsString(solution.get("object")));
			preparedStatement.addBatch();
		}
		preparedStatement.executeBatch();
		
	}
	
	private void clearDb() throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(
				"TRUNCATE TABLE Serialization");
		preparedStatement.execute();
	}

}

package com.mcdimensions.bungeesuitebukkit.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
	private static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
	
	private final String username, password;
	private final String connectionURL;
	
	public Database(String host, String database, String port, String username,
			String password) throws DatabaseDependencyException, SQLException {
		this.username = username;
		this.password = password;
		connectionURL = "jdbc:mysql://" + host +":" + port + "/" + database;
		// fail fast
		try {
			Class.forName(DRIVER_MYSQL);
		} catch (ClassNotFoundException e) {
			throw new DatabaseDependencyException("Could not find class for Mysql database drivers", e);
		}
		// attempt connection
		Connection connection = getConnection();
		connection.close();
	}
	
	/**
	 * Returns a connection for the current database. It is up to the caller to
	 * close any resources once the connection has been used.
	 * 
	 * @return Connection for the current database.
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException {
		return DriverManager.getConnection(connectionURL, username, password);
	}
	
	/**
	 * Executes the given query as an update/insert.
	 * 
	 * @param query
	 *            Query to execute
	 * @return Number of rows updated/inserted.
	 * @throws SQLException
	 *             if any errors occur while executing the query.
	 */
	public int updateQuery(String query) throws SQLException {
		Connection connection = getConnection();
		Statement statement = connection.createStatement();
		int rowsUpdated = statement.executeUpdate(query);
		statement.close();
		connection.close();
		return rowsUpdated;
	}

	/**
	 * Executes the query to determine existence of a result.
	 * 
	 * @param query
	 *            Query to execute. Must be a query that returns a result set.
	 * @return True if any results are returned, otherwise false.
	 * @throws SQLException
	 *             if any errors occur while executing the query.
	 */
	public boolean existenceQuery(String query) throws SQLException {
		Object result = singleResultQuery(query);
		return result != null;
	}
	
	/**
	 * Executes the given query and returns the first result as a string. If no
	 * results are returned, returns null. If the result is null, returns an
	 * empty string.
	 * 
	 * @param query
	 *            Query to execute. It is up to the caller to ensure that the
	 *            query returns at most one result, or returns results in
	 *            expected order.
	 * @return The first result in the result set. If no results are returned
	 *         returns null. If a null result is returned, returns an empty
	 *         string.
	 * @throws SQLException
	 *             if any errors occur while executing the query.
	 */
	public String singleResultStringQuery(String query) throws SQLException {
		Connection connection = getConnection();
		ResultSet results = sqlQuery(query, connection);
		String result = null;
		if (results.next()) {
			result = results.getString(1);
			if (result == null) {
				result = "";
			}
		}
		results.close();
		connection.close();
		return result;
	}
	
	public Object singleResultQuery(String query) throws SQLException {
		Connection connection = getConnection();
		ResultSet results = sqlQuery(query, connection);
		Object result = null;
		if (results.next()) {
			result = results.getObject(1);
		}
		results.close();
		connection.close();
		return result;
	}

	public boolean doesTableExist(String table) throws SQLException {
		return checkTable(table);
	}

	/**
	 * Executes the query with the specified connection. It is up to the caller
	 * to ensure that the connection is properly initialised beforehand and
	 * closed afterwards.
	 * 
	 * @param query
	 *            Query to execute.
	 * @param connection
	 *            Connection with which to execute the query.
	 * @return Result of the query execution.
	 * @throws SQLException
	 *             if any errors occur while executing the query.
	 */
	public ResultSet sqlQuery(String query, Connection connection) throws SQLException {
		Statement statement = connection.createStatement();
		ResultSet result = statement.executeQuery(query);
		return result;
	}

	protected boolean checkTable(String table) throws SQLException {
		Connection connection = getConnection();
		DatabaseMetaData dbm = connection.getMetaData();
		ResultSet tables = dbm.getTables(null, null, table, null);
		boolean result = tables.next();
		tables.close();
		connection.close();
		return result;
	}
}

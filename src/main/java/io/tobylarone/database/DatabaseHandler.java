package io.tobylarone.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import io.tobylarone.Config;

/**
 * DatabaseHandler class
 */
public class DatabaseHandler {

    private Connection conn;
    private Statement s;
    private ResultSet r;

    /**
     * DatabaseHandler constructor
     */
    public DatabaseHandler() {
        Config config = new Config();
        String host = config.getProperty("host");
        String dbname = config.getProperty("dbname");
        String dbuser = config.getProperty("dbuser");
        String dbpass = config.getProperty("dbpass");
        String dbssl = config.getProperty("dbSSL");
        try {
            conn = DriverManager.getConnection("jdbc:mysql://" + host + "/" + dbname + "?"
                                            + "user=" + dbuser + "&password=" + dbpass
                                            + "&useSSL=" + dbssl);
            s = null;
            r = null;
        } catch (SQLException e) {
            printException(e);
        }
    }

    public ResultSet select(String table, String[] inputFields) {
        try {
            s = conn.createStatement();
            String fields = String.join(", ", inputFields);
            String query = "SELECT " + fields + " FROM " + table;

            if (s.execute(query)) {
                r = s.getResultSet();
            }

        } catch (SQLException e) {
            printException(e);
        }
        return r;
    }

    public boolean insert() {

        return true;
    }

    /**
     * 
     */
    public void closeQuery() {
        if(r != null) {
            try {
                r.close();
            } catch (SQLException e) {
                printException(e);
            }
            r = null;
        }
        if(s != null) {
            try {
                s.close();
            } catch (SQLException e) {
                printException(e);
            }
            s = null;
        }
    }

    /**
     * Closes the database connection
     * See {@link Connection}
     */
    public void closeConnection() {
        try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    private void printException(SQLException e) {
        System.out.println("SQLException: " + e.getMessage());
        System.out.println("SQLState: " + e.getSQLState());
        System.out.println("ErrorCode: " + e.getErrorCode());
    }
}

package io.tobylarone.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.tobylarone.Config;

/**
 * DatabaseHandler class
 */
public class DatabaseHandler {

    private Connection conn;

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

        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("ErrorCode: " + e.getErrorCode());
        }
    }

    public void close() {
        try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
}

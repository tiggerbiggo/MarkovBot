package io.tobylarone.markov.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.tobylarone.markov.Config;
import io.tobylarone.markov.model.LocalMessage;
import io.tobylarone.markov.model.LocalUser;

/**
 * DatabaseHandler class
 */
public class DatabaseHandler {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseHandler.class);
    private Connection conn;
    private PreparedStatement ps;
    private ResultSet r;

    /**
     * DatabaseHandler constructor.
     * Establishes connection to the database
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
                                            + "&useSSL=" + dbssl
                                            + "&useUnicode=yes&characterEncoding=UTF-8");
            r = null;
        } catch (SQLException e) {
            printException(e);
        }
    }

    /**
     * 
     */
    public ResultSet selectId(String table, String[] inputFields, String targetField, Object id) {
        try {
            String fields = String.join(", ", inputFields);
            String query = "SELECT " + fields + " FROM " + table 
            + " WHERE " + targetField + "= ?";
            
            ps = conn.prepareStatement(query);
            ps.setObject(1, id);
            r = ps.executeQuery();

        } catch (SQLException e) {
            printException(e);
        }
        return r;
    }

    /**
     * 
     */
    public ResultSet select(String table, String[] inputFields) {
        try {
            String fields = String.join(", ", inputFields);
            String query = "SELECT " + fields + " FROM " + table;
            ps = conn.prepareStatement(query);

            r = ps.executeQuery();

        } catch (SQLException e) {
            printException(e);
        }
        return r;
    }

    public <T> void insertBatch(String table, List<T> list) {
        String query = "";
        if (list.get(0) instanceof LocalMessage) {
            query = "INSERT INTO " + table + " (user_id, message, discord_message_id) VALUES (?, ?, ?)";
        }
        try {
            ps = conn.prepareStatement(query);
            int counter = 0;
            for (T item : list) {
                LocalMessage m = (LocalMessage) item;
                if (item instanceof LocalMessage) {
                    ps.setInt(1, m.getUserId());
                    ps.setString(2, m.getMessage());
                    ps.setString(3, m.getDiscordMessageId());
                }
                ps.addBatch();
                counter++;
                if (counter % 1000 == 0 || counter == list.size()) {
                    ps.executeBatch();
                }
            }
        } catch (SQLException e) {
            printException(e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    printException(e);
                }
            }
        }
    }

    /**
     * 
     */
    public <T> void insert(String table, T object) {
        String query = "";
        try {
            if (object instanceof LocalUser) {
                query = "INSERT INTO " + table + " (discord_id, is_opt_in) VALUES (?, ?)";
                ps = conn.prepareStatement(query);
                LocalUser user = (LocalUser) object;
                ps.setString(1, user.getDiscordId());
                ps.setBoolean(2, user.isOptIn());
                ps.executeUpdate();
            } else if (object instanceof LocalMessage) {
                query = "INSERT INTO " + table + " (user_id, message, discord_message_id) VALUES (?, ?, ?)";
                ps = conn.prepareStatement(query);
                LocalMessage message = (LocalMessage) object;
                ps.setInt(1, message.getUserId());
                ps.setString(2, message.getMessage());
                ps.setString(3, message.getDiscordMessageId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            printException(e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    printException(e);
                }
            }
        }
    }

    /**
     * 
     */
    public <T, U> void updateByField(String table, String findField, T findValue, String updateField, U updateValue) {
        String query = "";
        try {
            query = "UPDATE " + table + " SET " + updateField + " = ? "
                    + "WHERE " + findField + " = ?";
            ps = conn.prepareStatement(query);
            ps.setObject(1, updateValue);
            ps.setObject(2, findValue);
            ps.executeUpdate();
        } catch (SQLException e) {
            printException(e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    printException(e);
                }
            }
        }
    }

    /**
     * 
     */
    public void removeByField(String table, String field, Object object) {
        String query = "";
        try {
            query = "DELETE FROM " + table + " WHERE " + field + " = ?";
            ps = conn.prepareStatement(query);
            ps.setObject(1, object);
            ps.executeUpdate();
        } catch (SQLException e) {
            printException(e);
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    printException(e);
                }
            }
        }
    }

    /**
     * Closes {@link ResultSet} and {@link Statement}
     */
    public void closeQuery() {
        if (r != null) {
            try {
                r.close();
            } catch (SQLException e) {
                printException(e);
            }
            r = null;
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

    /**
     * Prints the SQL exception details, useful for debugging
     * 
     * @param e the exception to print
     */
    private void printException(SQLException e) {
        LOGGER.warn("SQL Exception: " + e.getMessage());
        LOGGER.warn("SQL State: " + e.getSQLState());
        LOGGER.warn("Error Code: " + e.getErrorCode());
        e.printStackTrace();
    }
}

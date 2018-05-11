package io.tobylarone.markov.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.*;

import io.tobylarone.markov.Config;
import io.tobylarone.markov.model.LocalMessage;
import io.tobylarone.markov.model.LocalUser;

/**
 * DatabaseHandler class
 */
public class DatabaseHandler {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseHandler.class);
    private Connection conn;

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
                                            + "&useUnicode=yes"
                                            + "&characterEncoding=UTF-8"
                                            + "&autoReconnect=true");
        } catch (SQLException e) {
            printException(e);
        }
    }

    /**
     * Select row by column equality
     * 
     * @param table the table to select from
     * @param field the field in the where clause
     * @param value the equality value of the where clause
     */
    public Result<Record> selectBy(String table, String field, Object value) {
        DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
        Result<Record> result = create.select().from(table(table)).where(field(field).eq(value)).fetch();
        return result;
    }

    /**
     * Select all rows from table
     * 
     * @param table the table to select from 
     */
    public Result<Record> select(String table) {
        DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
        Result<Record> result = create.select().from(table(table)).fetch();
        return result;
    }

    /**
     * Insert a list of objects
     * 
     * @param table the table to insert into
     * @param list the list of objects to insert
     */
    public <T> void insertBatch(String table, List<T> list) {
        // DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
        list = list.subList(1, list.size());
        for (T item : list) {
            insert(table, item);
        }
    }

    /**
     * Insert new row into table
     * 
     * @param table the table to insert into
     * @param object the object to insert
     */
    public <T> void insert(String table, T object) {
        DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
        try {
            if (object instanceof LocalUser) {
                LocalUser u = (LocalUser) object;
                create.insertInto(table(table), field("discord_id"), field("is_opt_in"))
                    .values(u.getDiscordId(), u.isOptIn())
                    .execute();
            } else if (object instanceof LocalMessage) {
                LocalMessage m = (LocalMessage) object;
                create.insertInto(table(table), field("user_id"), field("message"), field("discord_message_id"))
                    .values(m.getUserId(), m.getMessage(), m.getDiscordMessageId())
                    .execute();
            }
        } catch (DataAccessException e) {
            LOGGER.warn("Error Inserting:" + e.getMessage());
            LOGGER.warn(e.getStackTrace());
        }
    }

    /**
     * Update rows by a specified field
     * 
     * @param table the table to update
     * @param findField the field in the where clause
     * @param findValue the where clause equality
     * @param updateField the field to update
     * @param updateValue the new value
     */
    public <T, U> void updateByField(String table, String findField, T findValue, String updateField, U updateValue) {
        DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
        try {
            create.update(table(table))
                .set(field(updateField), updateValue)
                .where(field(findField).eq(findValue))
                .execute();
        } catch (DataAccessException e) {
            LOGGER.warn("Error Updating:" + e.getMessage());
            LOGGER.warn(e.getStackTrace());
        }
    }

    /**
     * 
     */
    public void removeByField(String table, String field, Object object) {
        DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
        try {
            create.delete(table(table))
                .where(field(field).eq(object))
                .execute();
        } catch (DataAccessException e) {
            LOGGER.warn("Error Removing:" + e.getMessage());
            LOGGER.warn(e.getStackTrace());
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

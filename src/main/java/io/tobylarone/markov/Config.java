package io.tobylarone.markov;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Config
 */
public class Config {
    private static final Logger LOGGER = LogManager.getLogger(Config.class);
    public Properties config;
    public Properties messages;

    /**
     * Loads configuration and message files from resource folder
     * If loading fails program will exit.
     */
    public Config() {
        config = new Properties();
        messages = new Properties();

        LOGGER.debug("Attempting to load config.properties");
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
            } else {
                LOGGER.error("Failed to load config.properties");
                System.exit(1);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load config.properties");
            e.printStackTrace();
            System.exit(1);
        }
        LOGGER.debug("config.properties loaded.");

        LOGGER.debug("Attempting to load messages.properties");
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("messages.properties")) {
            if (input != null) {
                messages.load(input);
            } else {
                LOGGER.error("Failed to load messages.properties");
                System.exit(1);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load messages.properties");
            e.printStackTrace();
            System.exit(1);
        }
        LOGGER.debug("messages.properties loaded.");
    }

    /**
     * Gets a config option from config.properties file
     * 
     * @param key String to search for
     * @return config data as {@link String}
     */
    public String getProperty(String key) {
        return config.getProperty(key);
    }

    /**
     * Gets a message from the messages.properties file
     * 
     * @param key String to search for
     * @return message as {@link String}
     */
    public String getMessage(String key) {
        return messages.getProperty(key);
    }
}

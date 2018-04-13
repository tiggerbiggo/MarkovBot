package io.tobylarone;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Config
 */
public class Config {

    public Properties config;
    public Properties messages;

    /**
     * Loads configuration and message files from resource folder
     * If loading fails program will exit.
     */
    public Config() {
        config = new Properties();
        messages = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input != null) {
                config.load(input);
            } else {
                System.out.println("Cannot load config.properties");
                System.exit(1);
            }
        } catch (IOException e) {
            System.out.println("Cannot load config.properties");
            e.printStackTrace();
            System.exit(1);
        }

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("messages.properties")) {
            if (input != null) {
                messages.load(input);
            } else {
                System.out.println("Cannot load messages.properties");
                System.exit(1);
            }
        } catch (IOException e) {
            System.out.println("Cannot load messages.properties");
            e.printStackTrace();
            System.exit(1);
        }
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

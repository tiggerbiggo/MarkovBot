package io.tobylarone.markov;

import javax.security.auth.login.LoginException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * App class 
 */
public class App extends ListenerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(App.class);

    private Util util;
    private CommandParser parser;

    /**
     * Main method
     * 
     * @throws LoginException if authentication to discordapp fails
     * @throws InterruptedException if JDABuilder is interrupted during initialisation
     */
    public static void main(String[] args) throws LoginException, InterruptedException {
        LOGGER.info("Initialising");
        App app = new App();
        Config config = new Config();
        JDA j = new JDABuilder(AccountType.BOT).setToken(config.getProperty("token")).buildBlocking();
        j.addEventListener(app);
    }
    
    /**
     * Constructor for app. Then initialises the {@link CommandParser}
     */
    public App() {
        util = new Util();
        parser = new CommandParser();
    }

    /**
     * Ensures the message starts with the prefix !markov
     * Passes information on to the {@link CommandParser} to
     * parse and execute the command. 
     * 
     * @param e The message received from discord
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        boolean bot = e.getAuthor().isBot();
        if(!bot) {
            Message message = e.getMessage();
            MessageChannel channel = e.getChannel();
            String messageContent = message.getContentRaw();
            if (messageContent.startsWith("!markov")) {
                String[] messageSplit = messageContent.split(" ");
                switch (messageSplit.length) {
                    case 1:
                        parser.parseSingle(e);
                        break;
                    case 2:
                        parser.parseMultiArg(e, messageSplit);
                        break;
                    case 3:
                        parser.parseTripleArg(e, messageSplit);
                        break;
                    default:
                        util.sendHelp(channel);
                        break;
                }
            } else {
                parser.converse(e);
                parser.saveMessage(message);
            }
        }
    }
}

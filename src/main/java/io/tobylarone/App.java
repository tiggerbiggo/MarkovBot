package io.tobylarone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private final int MAX_FILE_NUM = 6;
    private List<String> uniqueUsers;
    private Util util;
    private String chatlog;
    private List<String> userChats;
    private Markov markov;
    private List<Markov> userMarkov;
    private CommandParser parser;

    /**
     * Main method
     * 
     * @throws LoginException if authentication to discordapp fails
     * @throws InterruptedException if JDABuilder is interrupted during initialisation
     */
    public static void main(String[] args) throws LoginException, InterruptedException {
        App app = new App();
        Config config = new Config();
        JDA j = new JDABuilder(AccountType.BOT).setToken(config.getProperty("token")).buildBlocking();
        j.addEventListener(app);
    }
    
    /**
     * Constructor for app. Loads the known chat history
     * and prepares {@link Markov} chains for all users and
     * individual users. Then initialises the {@link CommandParser}
     */
    public App() {
        util = new Util();
        uniqueUsers = new ArrayList<>();
        userChats = new ArrayList<>();
        try {
            chatlog = loadChat();
		} catch (IOException e) {
            e.printStackTrace();
		}
        markov = new Markov(chatlog);
        userMarkov = new ArrayList<>();
        for(String s : userChats) {
            userMarkov.add(new Markov(s));
        }
        chatlog = "";
        userChats.clear();
        parser = new CommandParser(markov, userMarkov, uniqueUsers);
    }

    /**
     * Loads from exported files. Will be removed in due course
     * 
     * @return chat history as a {@link String}
     */
    public String loadChat() throws IOException {
        List<String> chatList = new ArrayList<>();
        for (int i = 0; i <=  MAX_FILE_NUM; i++) {
            File input = new File(getClass().getClassLoader().getResource("lobby" + i + ".html").getFile());
            Document doc = Jsoup.parse(input, "UTF-8", "");
            Element log = doc.getElementById("log");
            Elements messages = log.getElementsByClass("msg");
            for (Element message : messages) {
                Elements userElement = message.getElementsByClass("msg-user");
                String user = userElement.get(0).text();
                if (!uniqueUsers.contains(user)) {
                    uniqueUsers.add(user);
                }

                Elements userMessage = message.getElementsByClass("msg-content");
                String messageContent = "";
                if (userMessage.size() > 0) {
                    for (int j = 0; j < userMessage.size(); j++) {
                        messageContent = userMessage.get(j).text();
                        chatList.add(messageContent);
                        int index = uniqueUsers.indexOf(user);
                        if (userChats.isEmpty() || userChats.size() < uniqueUsers.size()) {
                            userChats.add(messageContent);
                        } else {
                            userChats.set(index, userChats.get(index).concat(" " + messageContent));
                        }
                    }
                }
            }
        }
        return String.join(" ", chatList);
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
                        String markovSentence = markov.generateSentence();
                        util.sendWithTag(channel, e.getAuthor(), markovSentence);
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
                parser.saveMessage(message);
            }
        }
    }
}

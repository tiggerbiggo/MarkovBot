package io.tobylarone.markov;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.tobylarone.markov.database.LocalMessageRepo;
import io.tobylarone.markov.database.LocalUserRepo;
import io.tobylarone.markov.model.LocalMessage;
import io.tobylarone.markov.model.LocalUser;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * CommandParser
 *
 */
public class CommandParser {

    private static final Logger LOGGER = LogManager.getLogger(CommandParser.class);
    
    private static int saveMessageCounter;

    private Markov markov;
    private List<Markov> userMarkov;
    private List<String> uniqueUsers;
    private Util util;
    private Config config;
    private LocalDateTime startTime;
    private LocalUserRepo userRepo;
    private LocalMessageRepo messageRepo;
    private CommandHelper cmdHelper;

    /**
     * CommandParser constructor
     */
    public CommandParser() {
        saveMessageCounter = 0;
        cmdHelper = new CommandHelper();
        userRepo = new LocalUserRepo();
        messageRepo = new LocalMessageRepo();        
        util = new Util();
        config = new Config();
        startTime = LocalDateTime.now();
        loadChat();
    }

    /**
     * Loads the known chat history from database
     * and prepares {@link Markov} chains for all users and
     * individual users.
     */
    private void loadChat() {
        long loadTimeStart = System.nanoTime();
        List<String> chatList = new ArrayList<>();
        List<String> userChats = new ArrayList<>();
        List<LocalUser> knownUsers = userRepo.findAll();
        List<LocalMessage> messages = messageRepo.findAll();
        uniqueUsers = new ArrayList<>();

        for (LocalUser u : knownUsers) {
            if (!uniqueUsers.contains(u.getDiscordId())) {
                uniqueUsers.add(u.getDiscordId());
            }
        }

        LOGGER.info("Unique user count: " + uniqueUsers.size());

        for (LocalMessage m : messages) {
            chatList.add(m.getMessage());
            LocalUser user = userRepo.findById(m.getUserId());
            int index = uniqueUsers.indexOf(user.getDiscordId());
            if (userChats.isEmpty() || userChats.size() < uniqueUsers.size()) {
                userChats.add(m.getMessage());
            } else {
                userChats.set(index, userChats.get(index).concat(" " + m.getMessage()));
            }
        }
        String chatlog =  String.join(" ", chatList);

        markov = new Markov(chatlog);
        userMarkov = new ArrayList<>();
        for(String s : userChats) {
            userMarkov.add(new Markov(s));
        }
        long loadTimeEnd = System.nanoTime();
        LOGGER.info("Markov load time: " + (loadTimeEnd - loadTimeStart) / 1000000 + "ms");
    }

    /**
     * 
     */
    public void parseSingle(MessageReceivedEvent e) {
        String markovSentence = markov.generateSentence();
        LOGGER.info("Bot owner: " + isBotOwner(e));
        LOGGER.info("Server owner: " + isServerOwner(e));
        util.sendWithTag(e.getChannel(), e.getAuthor(), markovSentence);
    }

    /**
     * Method to parse commands, split from arguments of length three for nicer
     * readability
     * 
     * @param e Received message from discord
     * @param args String array of arguments from the discord message
     */
    public void parseMultiArg(MessageReceivedEvent e, String[] args) {
        MessageChannel channel = e.getChannel();
        User user = e.getAuthor();
        Message message = e.getMessage();
        String output = "";
        switch (args[1]) {
            case "ping":
                long time = cmdHelper.calculatePing(message);
                time = e.getJDA().getPing();
                util.sendWithTag(channel, user, "Ping: " + time + "ms");
                return;
            case "status":
                output = cmdHelper.getUserStatus(user);
                util.sendWithTag(channel, user, output);
                return;
            case "?":
            case "help":
                util.sendHelp(channel);
                return;
            case "about":
                util.sendAbout(channel);
                return;
            case "history":
                if (isServerOwner(e) || isBotOwner(e)) {
                    setIdle(e, true);
                    e.getJDA().getPresence().setGame(Game.playing("Loading history..."));
                    cmdHelper.history(channel, user, true);
                    rebuildIndex();
                    e.getJDA().getPresence().setGame(null);
                    setIdle(e, false);
                    return;
                }
                util.sendWithTag(channel, user, config.getMessage("request.no-permission"));
                return;
            case "rebuild":
                if (isServerOwner(e) || isBotOwner(e)) {
                    setIdle(e, true);
                    rebuildIndex();
                    setIdle(e, false);
                    return;
                }
                util.sendWithTag(channel, user, config.getMessage("request.no-premission"));
                return;
            case "add":
                output = cmdHelper.add(channel, user);
                util.sendWithTag(channel, user, output);
                rebuildIndex();
                return;
            case "remove":
                output = cmdHelper.remove(user);
                util.sendWithTag(channel, user, output);
                rebuildIndex();
                return;
            case "opt-in":
                output = cmdHelper.optIn(user);
                util.sendWithTag(channel, user, output);
                return;
            case "opt-out":
                output = cmdHelper.optOut(user);
                util.sendWithTag(channel, user, output);
                return;
            case "stats":
                e.getJDA().getPresence().setGame(Game.playing("Ping: " + e.getJDA().getPing() + "ms"));
                util.sendEmbed(channel, cmdHelper.prepStatsMessage(startTime, markov.getUniqueWordCount(), uniqueUsers.size(), markov.getTopWords()));
                return;
            case "mad":
                util.sendWithTag(channel, user, markov.generateSentence(80).toUpperCase() + "!");
                return;
            case "oof":
                contains(channel, user, "oof");
                return;
            case "thicc":
                output = thicc();
                util.sendWithTag(channel, user, output);
                return;
        }
        if (message.getMentionedUsers().size() == 1) {
            tagUser(channel, user, message, args, 140);
            return;
        }
        if (args[1].matches("[0-9]+")) {
            generateWithLimit(channel, user, args);
            return;
        }
        return;
    }

    private boolean isBotOwner(MessageReceivedEvent e) {
        String ownerId = config.getProperty("bot.owner.id");
        return (ownerId.equals(e.getAuthor().getId())) ? true : false;
    }

    private boolean isServerOwner(MessageReceivedEvent e) {
        // String serverOwnerId = e.getGuild().getOwner().getUser();
        return (e.getGuild().getOwner().getUser() == e.getAuthor()) ? true : false;
    }

    /**
     * Sets the bot presence to idle
     * 
     * @param e the message event
     * @param b if true, set to idle, if false, set to online
     */
    private void setIdle(MessageReceivedEvent e, boolean b) {
        if (b) {
            e.getJDA().getPresence().setStatus(OnlineStatus.IDLE);
        } else {
            e.getJDA().getPresence().setStatus(OnlineStatus.ONLINE);
        }
	}

    /**
     * Force rebuilding of markov chains
     */
	private void rebuildIndex() {
        loadChat();
    }

	/**
     * Method to parse commands, split from arguements of length two for nicer
     * readability
     * 
     * @param e message recieved from discord
     * @param args command arguments
     */
    public void parseTripleArg(MessageReceivedEvent e, String[] args) {
        MessageChannel channel = e.getChannel();
        User user = e.getAuthor();
        Message message = e.getMessage();
        if (args[1].equals("contains")) {
            contains(channel, user, args[2]);
            return;
        }
    }

    /**
     * Translates a generated sentence to `thicc` chars
     * 
     * @return translated string
     */
    private String thicc() {
        String sentence = markov.generateSentence();
        Thicc thicc = new Thicc(sentence);
        return thicc.parse();
	}

    /**
     * Method to return messages that contain a specific word
     * <p>
     * Q: Why does it use a loop to generate words, surely that is ineffecient?
     * <p>
     * A: It would be possible to ensure the sentence always started or ended with
     *  the search term. However, these negates the idea of the sentences being random
     *  It is also possible to randomly inject the word into the middle of the
     *  after generation. However, this defeats the purpose of using markov chains.
     *  This generate is not meant to be a perfect solution. 
     *  It's primary use is fun.
     *  <p>
     *  Sentence generation is a very fast operation, and "searching" is limited to
     *  a maximum of 50,000 iterations. More testing is required to determine a
     *  sweet spot of successful return probability and return time.
     * 
     * @param channel the channel to send the message to
     * @param user the user to tag the message to
     * @param searchTerm the word to search fo
     */
    private void contains(MessageChannel channel, User user, String searchTerm) {
        if(markov.getUniqueWords().contains(searchTerm)) {
            String sentence = "";
            int counter = 0;
            int limit = markov.getUniqueWordCount() < 50000 ? markov.getUniqueWordCount() : 50000;
            String term = " " + searchTerm;
            long startTime = System.nanoTime();
            while (!sentence.contains(term) && counter < limit) {
                sentence = markov.generateSentence();
                counter++;
            }
            long endTime = System.nanoTime();
            LOGGER.info("Took " + counter + " iterations to generate sentence with word " + searchTerm + " (" + (endTime - startTime) / 1000 + "μs)");
            if (sentence.contains(term)) {
                util.sendWithTag(channel, user, sentence);
                return;
            } else {
                util.sendWithTag(channel, user, config.getMessage("request.search.not-found"));
                return;
            }
        } else {
            util.sendWithTag(channel, user, config.getMessage("request.search.not-in-dictionary"));
        }
    }

    /**
     * Method to allow users to tag another user and generate a sentence only based
     * on the tagged users chain.
     * <p>
     * If the message author is self tagging, continue otherwise, check if the target
     * user has opted-in to 3rd party tagging
     * 
     * @param channel the channel to send the message to
     * @param user the user to tag the message to
     * @param message the message received from discord
     * @param args the split arguments received from discord
     * @param length length of the message to generate
     */
    private void tagUser(MessageChannel channel, User user, Message message, String[] args, int length) {
        String targetName = message.getMentionedUsers().get(0).getId();
        String id = message.getAuthor().getId();
        String argId = args[1].replace("<@", "");
        argId = argId.replace(">", "");
        if (uniqueUsers.contains(targetName)) {
            int index = uniqueUsers.indexOf(targetName);
            if (argId.equals(id)) {
                String output = userMarkov.get(index).generateSentence(length);
                util.sendWithTag(channel, user, output);
                return;
            } else {
                LocalUser searchUser = userRepo.findByStringId(argId);
                if (searchUser != null && searchUser.isOptIn()) {
                    String output = userMarkov.get(index).generateSentence(length);
                    util.sendWithTag(channel, user, output);
                    return;
                }
                LOGGER.debug("Target user was not opted in.");
                util.sendWithTag(channel, user, config.getMessage("tag.user.out"));
                return;
            }
        }
        LOGGER.debug("Target user was not found.");
        util.sendWithTag(channel, user, config.getMessage("tag.user.out"));
        return;
    }

    /**
     * Generate a sentence with a user specified length
     * Ensure the length is > 10 and < 1500
     * 
     * @param channel the channel to send the message to
     * @param user the user to tag the message to
     * @param args the split arguments received from discord
     */
    private void generateWithLimit(MessageChannel channel, User user, String[] args) {
        int limit = Integer.valueOf(args[1]);
        if (limit > 1500) {
            util.sendWithTag(channel, user, config.getMessage("request.limit.big"));
            return;
        } else if (limit < 10) {
            util.sendWithTag(channel, user, config.getMessage("request.limit.small"));
            return;
        }
        LOGGER.debug("Generating message with limit " + limit);
        util.sendWithTag(channel, user, markov.generateSentence(limit));
        return;
    }

    /**
     * Wrapper for saving a message to the database
     * 
     * @param message the message to save
     */
	public void saveMessage(Message message) {
        if (cmdHelper.saveMessage(message)) {
            saveMessageCounter++;
            if (saveMessageCounter % 50 == 0) {
                saveMessageCounter = 0;
                rebuildIndex();
            }
        }
	}
}

package io.tobylarone;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

import io.tobylarone.database.LocalMessageRepo;
import io.tobylarone.database.LocalUserRepo;
import io.tobylarone.model.LocalMessage;
import io.tobylarone.model.LocalUser;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * CommandParser
 *
 */
public class CommandParser {

    private Markov markov;
    private List<Markov> userMarkov;
    private List<String> users;
    private Util util;
    private Config config;
    private LocalDateTime startTime;
    private LocalUserRepo userRepo;
    private LocalMessageRepo messageRepo;
    private CommandHelper cmdHelper;

    /**
     * CommandParser constructor
     * 
     * @param markov full chat chain
     * @param userMarkov list of markov chains for individual users
     * @param users list of unique users
     */
    public CommandParser(Markov markov, List<Markov> userMarkov, List<String> users) {
        cmdHelper = new CommandHelper();
        userRepo = new LocalUserRepo();
        messageRepo = new LocalMessageRepo();
        startTime = LocalDateTime.now();
        this.markov = markov;
        this.userMarkov = userMarkov;
        this.users = users;
        util = new Util();
        config = new Config();
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
                history(channel, user);
                return;
            case "add":
                output = cmdHelper.add(user);
                util.sendWithTag(channel, user, output);
                return;
            case "remove":
                output = cmdHelper.remove(user);
                util.sendWithTag(channel, user, output);
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
                util.send(channel, cmdHelper.prepStatsMessage(startTime, markov.getUniqueWordCount(), users.size()));
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
            while (!sentence.contains(term) && counter < limit) {
                sentence = markov.generateSentence();
                counter++;
            }
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
     * Current behaviour only allows for self tagging, in future must check DB for
     * opt-in users
     * 
     * @param channel the channel to send the message to
     * @param user the user to tag the message to
     * @param message the message received from discord
     * @param args the split arguments received from discord
     * @param length length of the message to generate
     */
    private void tagUser(MessageChannel channel, User user, Message message, String[] args, int length) {
        String targetName = message.getMentionedUsers().get(0).getName();
        String id = message.getAuthor().getId();
        String argId = args[1].replace("<@", "");
        argId = argId.replace(">", "");
        int index = users.indexOf(targetName);
        String output = userMarkov.get(index).generateSentence(length);
        System.out.println(targetName);
        System.out.println(id);
        System.out.println(argId);
        if (argId.equals(id)) {
            util.sendWithTag(channel, user, output);
            return;
        } else {
            util.sendWithTag(channel, user, config.getMessage("tag.user.out"));
            return;
        }
    }

    /**
     * In progress - pulling history via JDA
     */
    private void history(MessageChannel channel, User user) {
        if (user.getName().equals("Toby Łarone")) {
            util.sendWithTag(channel, user, config.getMessage("history.collection.started"));
            List<LocalUser> users = userRepo.findAll();
            List<LocalMessage> messages = new ArrayList<>();
            int historicalMessageLimit = 10000;
            List<LocalUser> uniqueUsers = new ArrayList<>();
            for (Message aMessage : channel.getIterableHistory().cache(false)) {
                if (!aMessage.getContentRaw().startsWith("!markov")) {
                    boolean isFound = false;
                    LocalUser u = null;
                    for (LocalUser lu : users) {
                        if(lu.getDiscordId().equals(aMessage.getAuthor().getId())) {
                            isFound = true;
                            if(!uniqueUsers.contains(lu)) {
                                uniqueUsers.add(lu);
                            }
                            u = lu;
                            break;
                        }
                    }
                    if (isFound) {
                        LocalMessage m = new LocalMessage(u.getId(), aMessage.getId(), aMessage.getContentRaw());
                        m.removeInvalidWords();
                        messages.add(m);
                    }
                }
                if (--historicalMessageLimit <= 0) {
                    break;
                }
            }
            messageRepo.insertBulk(messages);
            // if (messages.size() > 2) {
            //     int id = messages.size() - 2;
            //     String a = "Latest: " + messages.get(0).getContentRaw() + "  Date: " + messages.get(0).getCreationTime();
            //     String b = "Oldest(" + id + "): " + messages.get(id).getContentRaw() + "  Date: " + messages.get(id).getCreationTime();
            //     System.out.println(messages.get(0).getId());
                
            //     util.sendWithTag(channel, user, a);
            //     util.sendWithTag(channel, user, b);
            // }
            util.sendWithTag(channel, user, config.getMessage("history.collection.complete") + " (Users: " + uniqueUsers.size() + ")");
        } else {
            util.sendWithTag(channel, user, config.getMessage("request.no-permission"));
        }
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
        util.sendWithTag(channel, user, markov.generateSentence(limit));
        return;
    }

	public void saveMessage(Message message) {
        cmdHelper.saveMessage(message);
	}
}

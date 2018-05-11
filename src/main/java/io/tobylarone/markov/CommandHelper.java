package io.tobylarone.markov;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.tobylarone.markov.database.LocalMessageRepo;
import io.tobylarone.markov.database.LocalUserRepo;
import io.tobylarone.markov.model.LocalMessage;
import io.tobylarone.markov.model.LocalUser;
import io.tobylarone.markov.util.WordStat;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;

/**
 * CommandHelper Class
 */
public class CommandHelper {

    private static final Logger LOGGER = LogManager.getLogger(CommandHelper.class);

    private Config config;
    private LocalUserRepo userRepo;
    private LocalMessageRepo messageRepo;
    private Util util;
    private static final Color BLUE = new Color(0x007acc);

    /**
     * CommandHelper Constructor
     */
    public CommandHelper() {
        config = new Config();
        userRepo = new LocalUserRepo();
        messageRepo = new LocalMessageRepo();
        util = new Util();
    }

    /**
     * Pulls historic messages from the provided discord channel
     * 
     * @param channel the channel to retreive history from
     * @param user the calling user, used to send status update messages to
     * @param isBackground true will suppress status messages in the chat
     * only bot Precense will be used for status
     */
    public void history(MessageChannel channel, User user, boolean isBackground) {
        LOGGER.info("Starting history collection");
        long startTime = System.nanoTime();
        // TODO get current db message count
        if (!isBackground) {
            util.sendWithTag(channel, user, config.getMessage("history.collection.started"));
        }
        List<LocalUser> users = userRepo.findAll();
        List<LocalMessage> messages = new ArrayList<>();
        int historicalMessageLimit = 25000;
        List<LocalUser> uniqueUsers = new ArrayList<>();
        for (Message aMessage : channel.getIterableHistory().cache(false)) {
            if (--historicalMessageLimit <= 0) {
                break;
            }
            if (!aMessage.getContentRaw().startsWith("!markov")) {
                boolean isFound = false;
                LocalUser u = null;
                for (LocalUser lu : users) {
                    if (lu.getDiscordId().equals(aMessage.getAuthor().getId())) {
                        isFound = true;
                        if (!uniqueUsers.contains(lu)) {
                            LOGGER.debug("Adding a new user");
                            uniqueUsers.add(lu);
                        }
                        u = lu;
                        break;
                    }
                }
                if (isFound) {
                    if (aMessage.getAttachments().size() == 0 && aMessage.getEmbeds().size() == 0) {
                        LocalMessage m = new LocalMessage(u.getId(), aMessage.getId(), aMessage.getContentRaw());
                        if (!aMessage.getMentionedUsers().isEmpty()) {
                            for (User mentionedUser : aMessage.getMentionedUsers()) {
                                m.setMessage(m.getMessage() + " " + mentionedUser.getName());
                            }
                        }
                        m.removeInvalidWords();
                        if (m.getMessage().equals("")) {
                            continue;
                        }
                        messages.add(m);
                        LOGGER.trace("Added new message.");
                    } else {
                        continue;
                    }
                }
            }
        }
        messageRepo.insertBulk(messages);
        if (!isBackground) {
            util.sendWithTag(channel, user, config.getMessage("history.collection.complete") + " (Users: " + uniqueUsers.size() + ")");
        }
        long endTime = System.nanoTime();
        // TODO log new message count
        LOGGER.info("History collection finished in " + (endTime - startTime) / 1000000 + "ms");
    }

    /**
     * Removes a user from the database
     * 
     * @param user the user to remove
     * @return string of success or failure
     */
    public String remove(User user) {
        List<LocalUser> users = userRepo.findAll();
        boolean userFound = false;
        LocalUser foundUser = null;
        for (LocalUser u : users) {
            if (u.getDiscordId().equals(user.getId())) {
                userFound = true;
                foundUser = u;
                break;
            }
        }
        if (userFound) {
            userRepo.removeById(foundUser.getId());
            messageRepo.removeByUserId(foundUser.getId());
            LOGGER.info("User has been removed");
            return config.getMessage("user.removed.success");
        } else {
            return config.getMessage("user.removed.failure");
        }
    }

    /**
     * Adds a user into the database
     * 
     * @param user the user to add
     * @return string of success or failure
     */
    public String add(MessageChannel channel, User user) {
        List<LocalUser> users = userRepo.findAll();
        boolean alreadyExists = false;
        for (LocalUser u : users) {
            if (u.getDiscordId().equals(user.getId())) {
                alreadyExists = true;
                break;
            }
        }
        if (!alreadyExists) {
            util.sendWithTag(channel, user, config.getMessage("history.collection.started"));
            LocalUser u = new LocalUser(user.getId());
            userRepo.insert(u);
            LOGGER.info("New user added");
            boolean isBackground = true;
            history(channel, user, isBackground);
            return config.getMessage("user.added.success");
        } else {
            return config.getMessage("user.added.failure");
        }
    }

    /**
     * Allow a user to optIn to being tagged with the markov
     * command
     * 
     * @param user the target user
     * @return success or failure message
     */
    public String optIn(User user) {
        List<LocalUser> users = userRepo.findAll();
        boolean alreadyIn = false;
        boolean userFound = false;
        for (LocalUser u : users) {
            if (u.getDiscordId().equals(user.getId())) {
                userFound = true;
                if(u.isOptIn()) {
                    alreadyIn = true;
                    break;
                }
            }
        }
        if(userFound) {
            if (!alreadyIn) {
                LocalUser u = new LocalUser(user.getId());
                userRepo.updateOptIn(u.getDiscordId(), true);
                return config.getMessage("user.optin.success");
            } else {
                return config.getMessage("user.optin.failure");
            }
        } else {
            return config.getMessage("user.notfound");
        }
    }

    /**
     * Allow a user to optOut of being tagged with the markov
     * command
     * 
     * @param user the target user
     * @return success or failure message
     */
    public String optOut(User user) {
        List<LocalUser> users = userRepo.findAll();
        boolean alreadyOut = false;
        for (LocalUser u : users) {
            if (u.getDiscordId().equals(user.getId())) {
                if(!u.isOptIn()) {
                    alreadyOut = true;
                    break;
                }
            }
        }
        if (!alreadyOut) {
            LocalUser u = new LocalUser(user.getId());
            userRepo.updateOptIn(u.getDiscordId(), false);
            return config.getMessage("user.optout.success");
        } else {
            return config.getMessage("user.optout.failure");
        }
    }

    /**
     * Discovered the users opt-in status which is returned
     * as a chat message to the user
     * 
     * @param inpUser the user to check the status of
     * @return string status of user opt-in levels
     */
    public String getUserStatus(User inpUser) {
        LocalUser user = userRepo.findByStringId(inpUser.getId());
        String message = "";
        if (user == null) {
            message = config.getMessage("status.global.exc") + "\n"
                    + config.getMessage("status.individual.exc") + "\n"
                    + config.getMessage("status.3rdparty.exc");
        } else {
            if (user.isOptIn()) {
                message = config.getMessage("status.global.inc") + "\n"
                        + config.getMessage("status.individual.inc") + "\n"
                        + config.getMessage("status.3rdparty.inc");
            } else {
                message = config.getMessage("status.global.inc") + "\n"
                        + config.getMessage("status.individual.inc") + "\n"
                        + config.getMessage("status.3rdparty.exc");
            }
        }
        return message;
    }

    /**
     * Calculate ping using the creation time of the !markov ping message received
     * 
     * @param message the message used to calculate ping
     * @return time in milliseconds
     */
    public long calculatePing(Message message) {
        long messageSecond = message.getCreationTime().toEpochSecond();
        long messageMilli = message.getCreationTime().get(ChronoField.MILLI_OF_SECOND);
        long messageTime = Long.valueOf(String.valueOf(messageSecond) + String.format("%03d", messageMilli));
        long now = System.currentTimeMillis();
        return now - messageTime;
    }

    /**
     * Generates the bot statistics message
     * 
     * @param startTime the startup time of the bot
     * @param wordCount number of globally unique words
     * @param userCount total number of unique users
     * @return built message as {@link MessageEmbed}
     */
    public MessageEmbed prepStatsMessage(LocalDateTime startTime, int wordCount, int userCount, List<WordStat> topWords) {
        LocalDateTime now = LocalDateTime.now();
        Duration d = Duration.between(startTime, now);
        String uptime = d.toDays() + " days " + (d.toHours() % 60) + " hours " + (d.toMinutes() % 60) + " min";
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(BLUE);
        eb.setTitle("Statistics");
        eb.addField("Uptime", uptime, false);
        eb.addField("Total users", String.valueOf(userCount), false);
        eb.addField("Unique word count", String.valueOf(wordCount), false);
        
        String topWordsText = "";
        for (int i = 0; i < 5; i++) {
            topWordsText += "**" + topWords.get(i).getWord() + "** (" + topWords.get(i).getCount() + ")\n";
        }
        
        eb.addField("Top Five Words", topWordsText, false);
        return eb.build();
    }

    /**
     * Saves a message to the database
     * 
     * @param message the message to be saved
     * @return true if message was saved, false if not saved
     */
	public boolean saveMessage(Message message) {
        LocalUser user = userRepo.findByStringId(message.getAuthor().getId());
        if (user != null) {
            if (message.getAttachments().size() == 0 && message.getEmbeds().size() == 0) {
                LocalMessage m = new LocalMessage(user.getId(), message.getId(), message.getContentRaw());
                m.removeInvalidWords();
                m.setMessage(m.getMessage().trim());
                if (m.getMessage() != "") {
                    LOGGER.info("Saving message: " + message.getContentRaw());
                    messageRepo.insert(m);
                    return true;
                }
            }
        }
        return false;
	}
}

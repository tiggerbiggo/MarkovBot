package io.tobylarone;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.List;

import io.tobylarone.database.LocalMessageRepo;
import io.tobylarone.database.LocalUserRepo;
import io.tobylarone.model.LocalMessage;
import io.tobylarone.model.LocalUser;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;

/**
 * CommandHelper Class
 */
public class CommandHelper {

    private Config config;
    private LocalUserRepo userRepo;
    private LocalMessageRepo messageRepo;

    /**
     * CommandHelper Constructor
     */
    public CommandHelper() {
        config = new Config();
        userRepo = new LocalUserRepo();
        messageRepo = new LocalMessageRepo();
    }

    /**
     * Removes a user from the database
     * TODO remove all messages associated with user
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
    public String add(User user) {
        List<LocalUser> users = userRepo.findAll();
        boolean alreadyExists = false;
        for (LocalUser u : users) {
            if (u.getDiscordId().equals(user.getId())) {
                alreadyExists = true;
                break;
            }
        }
        if (!alreadyExists) {
            LocalUser u = new LocalUser(user.getId());
            userRepo.insert(u);
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
     * @return message as {@link String}
     */
    public String prepStatsMessage(LocalDateTime startTime, int wordCount, int userCount) {
        LocalDateTime now = LocalDateTime.now();
        Duration d = Duration.between(startTime, now);
        String message = "\n"
            + "STATISTICS: \n"
            + "Total unique word count: " + wordCount + "\n"
            + "Total users: " + userCount + "\n"
            + "Uptime: " + d.toDays() + "days " + (d.toHours() % 60) + "hours " + (d.toMinutes() % 60) + "min\n";
        return message;
    }

	public void saveMessage(Message message) {
        LocalUser user = userRepo.findByStringId(message.getAuthor().getId());
        if (user != null) {
            LocalMessage m = new LocalMessage(user.getId(), message.getId(), message.getContentRaw());
            m.removeInvalidWords();
            messageRepo.insert(m);
        }
	}
}
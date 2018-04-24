package io.tobylarone;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * Util class for sending various message types to discord channels
 *
 */
public class Util {

    public Util() {
    }

    /**
     * Builds the help message and sends it to the channel
     * <p>
     * Q: Why not use an EmbedBuilder?
     * <p>
     * A: Using an EmbedBuilder would make the help message
     * inaccessible to discord users who disable embeds
     * 
     * @param channel channel to send message to
     */
    public void sendHelp(MessageChannel channel) {
        String helpMessage = "```Marko Rov Help!\n"
            + "\n"
            + "Example Usage: !markov mad\n"
            + "!markov help          --  Show help message\n"
            + "!markov about         --  Show about message\n"
            + "\n"
            + "!markov               --  Return phrase (max: 140)\n"
            + "!markov 120           --  Return phrase (max: 1500)\n"
            + "!markov @User         --  Return phrase based on target user (max: 140)\n"
            + "!markov @User 120     --  Return phrase based on target user (max: 1500)\n"
            + "!markov mad           --  MARKO ROV GETS MAD (max: 80)\n"
            + "!markov contains term --  Returns a phrase that contains 'term' (max: 140)\n"
            + "\n"
            + "!markov status        --  View your opt-in status\n"
            + "!markov add           --  Add your chat history (since 2018/01/01)\n"
            + "!markov remove        --  Remove all your chat history (since 2018/01/01)\n"
            + "!markov opt-in        --  Allow other users to generate phrase based on your chat\n"
            + "!markov opt-out       --  Disallow other users to generate phrase based on your chat\n"
            + "\n"
            + "!markov stats         --  Returns some statistics\n"
            + "!markov ping          --  Returns bot ping\n"
            + "```";
        channel.sendMessage(helpMessage).queue();
    }

    /**
     * Builds the help message and sends it to the channel
     * <p>
     * Q: Why not use an EmbedBuilder? 
     * <p>
     * A: Using an EmbedBuilder would make the help message
     * inaccessible to discord users who disable embeds
     * 
     * @param channel channel to send message to
     */
    public void sendAbout(MessageChannel channel) {
        String message = "```\n"
            + "Marko Rov Bot\n"
            + "\n"
            + "Written & maintained by Toby Łarone\n"
            + "\n"
            + "Source: https://github.com/TobyLarone85/MarkovBot\n"
            + "```\n";
            channel.sendMessage(message).queue();
    }

    /**
     * Sends a message to the specified discord channel
     * 
     * @param channel target channel
     * @param message message to send
     */
    public void send(MessageChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    /**
     * Sends a message to the specified discord channel and mentions
     * a user
     * 
     * @param channel target channel
     * @param user user to mention in the message
     * @param message message to send
     */
    public void sendWithTag(MessageChannel channel, User user, String message) {
        channel.sendMessage(user.getAsMention() + " " + message).queue();
    }
}
package io.tobylarone;

import java.awt.Color;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

/**
 * Util class for sending various message types to discord channels
 *
 */
public class Util {

    private static final Color BLUE = new Color(0x007acc);

    public Util() {
    }

    /**
     * Builds the help message and sends it to the channel
     * 
     * @param channel channel to send message to
     */
    public void sendHelp(MessageChannel channel) {

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Markov Help", null);
        eb.setColor(BLUE);
        eb.setDescription("Usage information for Markov Bot");
        eb.addField("!markov help", "Displays the help \nmessage", true);
        eb.addField("!markov about", "Displays the about message", true);
        eb.addBlankField(true);

        eb.addField("!markov", "Return generated \nphrase (140 chars)", true);
        eb.addField("!markov 120", "Return phrase (max: 1500)", true);
        eb.addBlankField(true);

        eb.addField("!markov @User", "Return phrase based \non target user \n(140 chars)", true);
        eb.addField("!markov @user 120", "Return phrase based \non target user (max: 1500)", true);
        eb.addBlankField(true);

        eb.addField("!markov mad", "MARKO ROV GETS \nMAD (80 chars)", true);
        eb.addField("!markov contains term", "Returns a phrase \nthat contains 'term' \n(max: 140)", true);
        eb.addBlankField(true);

        eb.addField("!markov status", "View your opt-in status", true);
        eb.addBlankField(true);
        eb.addBlankField(true);

        eb.addField("!markov add", "Add your chat history ", true);
        eb.addField("!markov remove", "Remove all your chat \nhistory", true);
        eb.addBlankField(true);

        eb.addField("!markov opt-in", "Allow other users to \ngenerate phrase based \non your chat", true);
        eb.addField("!markov opt-out", "Disallow other users to \ngenerate phrase based \non your chat", true);
        eb.addBlankField(true);

        eb.addField("!markov stats", "Returns some statistics", true);
        eb.addField("!markov ping", "Returns bot ping", true);
        eb.addBlankField(true);
        channel.sendMessage(eb.build()).queue();
    }

    /**
     * Builds the about message and sends it to the channel
     * 
     * @param channel channel to send message to
     */
    public void sendAbout(MessageChannel channel) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(BLUE);
        eb.setTitle("Marko Rov Bot", null);
        eb.setDescription("Written & maintained by Toby Łarone");
        eb.addField("Source", "https://github.comS̨̥̫͎̭ͯ̿̔̀ͅ", false);
        channel.sendMessage(eb.build()).queue();
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
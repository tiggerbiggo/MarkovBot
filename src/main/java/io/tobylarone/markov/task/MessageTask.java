package io.tobylarone.markov.task;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import io.tobylarone.markov.Markov;
import io.tobylarone.markov.Util;
import io.tobylarone.markov.database.LocalMessageRepo;
import io.tobylarone.markov.model.LocalMessage;
import net.dv8tion.jda.core.JDA;

/**
 * MessageTask class
 */
public class MessageTask extends TimerTask {

    private JDA jda;
    private Util util;
    private LocalMessageRepo messageRepo;

    /**
     * MessageTask constructor
     */
    public MessageTask(JDA jda) {
        this.jda = jda;
        util = new Util();
        messageRepo = new LocalMessageRepo();
    }

	@Override
	public void run() {

        Markov markov = reloadMarkov();
        // TODO read channel from db
		util.send(jda.getTextChannelById("433946564218847235"), markov.generateSentence());
    }
    
    private Markov reloadMarkov() {
        List<String> chatList = new ArrayList<>();
        List<LocalMessage> messages = messageRepo.findAll();

        for (LocalMessage m : messages) {
            chatList.add(m.getMessage());
        }
        String chatlog =  String.join(" ", chatList);

        return new Markov(chatlog);
    }
}

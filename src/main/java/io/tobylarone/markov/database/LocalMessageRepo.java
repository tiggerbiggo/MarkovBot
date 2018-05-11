package io.tobylarone.markov.database;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;

import io.tobylarone.markov.model.LocalMessage;

/**
 * LocalMessageRepo Class
 */
public class LocalMessageRepo extends DatabaseWrapper<LocalMessage> {

	/**
	 * LocalMessageRepo Constructor
	 */
	public LocalMessageRepo() {
	}

	/**
	 * Find message by message id
	 * 
	 * @param id the message id to search for
	 * @return the message, or if not found, null
	 */
	@Override
	public LocalMessage findByStringId(String id) {
		LocalMessage message = null;
		Result<Record> result = getDb().selectBy("messages", "discord_message_id", id);

		if (result.size() != 1) {
			return null;
		}
		for (Record r : result) {
			int resid = r.getValue("id", Integer.class);
			int userId = r.getValue("user_id", Integer.class);
			String messageText = r.getValue("message", String.class);
			String discordMessageId = r.getValue("discord_message_id", String.class);

			message = new LocalMessage(resid, userId, messageText, discordMessageId);
		}

		return message;
	}

	/**
	 * Retrieve all messages
	 * 
	 * @return the list of messages
	 */
	@Override
	public List<LocalMessage> findAll() {
		List<LocalMessage> messages = new ArrayList<>();
		Result<Record> results = getDb().select("messages");

		for (Record r : results) {
			int id = r.getValue("id", Integer.class);
			int userId = r.getValue("user_id", Integer.class);
			String discordMessageId = r.getValue("discord_message_id", String.class);
			String messageValue = r.getValue("message", String.class);
			LocalMessage message = new LocalMessage(id, userId, discordMessageId, messageValue);
			messages.add(message);
		}
		return messages;
	}

	/**
	 * Insert a new message into the database
	 * @param t the message to insert
	 */
	@Override
	public void insert(LocalMessage t) {
		getDb().insert("messages", t);
	}

	/**
	 * Insert a list of messages into the database
	 * @param the list of messages
	 */
	public void insertBulk(List<LocalMessage> messages) {
		getDb().insertBatch("messages", messages);
	}

	/**
	 * Remove messages filtered by user_id
	 * 
	 * @param id the id of the target user
	 */
	public void removeByUserId(int id) {
		getDb().removeByField("messages", "user_id", id);
	}

	/**
	 * Find message by id
	 * 
	 * @param id the message id to retreive
	 * @return the message object
	 */
	@Override
	public LocalMessage findById(int id) {
		LocalMessage user = null;
        Result<Record> result = getDb().selectBy("messages", "id", id);

        if (result.size() != 1) {
            return null;
        }

        for (Record r : result) {
            int resid = r.getValue("id", Integer.class);
            int userId = r.getValue("user_id", Integer.class);
            String discordMessageId = r.getValue("discord_message_id", String.class);
			String messageValue = r.getValue("message", String.class);
            user = new LocalMessage(resid, userId, discordMessageId, messageValue);
        }

        return user;
	}
}

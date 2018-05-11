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

	@Override
	public void insert(LocalMessage t) {
		getDb().insert("messages", t);
	}

	public void insertBulk(List<LocalMessage> messages) {
		getDb().insertBatch("messages", messages);
	}

	public void removeByUserId(int id) {
		getDb().removeByField("messages", "user_id", id);
	}

	@Override
	public LocalMessage findById(int id) {
		return null;
	}
}

package io.tobylarone.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.tobylarone.model.LocalMessage;

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
		LocalMessage user = null;
		String[] fields = {"*"};
		ResultSet result = getDb().selectId("users", fields, "discord_message_id", id);
		try {
			while (result.next()) {
				int resid = result.getInt("id");
				int userId = result.getInt("user_id");
				String message = result.getString("message");
				String discordMessageId = result.getString("discord_message_id");
				user = new LocalMessage(resid, userId, message, discordMessageId);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		getDb().closeQuery();
		return user;
	}

	@Override
	public List<LocalMessage> findAll() {
		List<LocalMessage> messages = new ArrayList<>();
		String[] fields = {"*"};
		ResultSet results = getDb().select("messages", fields);
		try {
			while (results.next()) {
				int id = results.getInt("id");
				int userId = results.getInt("user_id");
				String discordMessageId = results.getString("discord_message_id");
				String messageValue = results.getString("message");
				LocalMessage message = new LocalMessage(id, userId, discordMessageId, messageValue);
				messages.add(message);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		getDb().closeQuery();
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

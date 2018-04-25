package io.tobylarone.markov.model;

import java.util.ArrayList;
import java.util.List;

/**
 * LocalMessage Class
 */
public class LocalMessage {

    private int id;
    private int userId;
	private String discordMessageId;
	private String message;

    /**
     * LocalMessage Constructor
     */
    public LocalMessage(int id, int userId, String discordMessageId, String message) {
        this.id = id;
		this.userId = userId;
		this.discordMessageId = discordMessageId;
		this.message = message;
	}

	public LocalMessage(int userId, String discordMessageId, String message) {
		this.userId = userId;
		this.discordMessageId = discordMessageId;
		this.message = message;
	}
	
	public void removeInvalidWords() {
		String[] words = message.split(" ");
		List<String> validWords = new ArrayList<>();
		for (int i = 0; i < words.length; i++) {
			if (words[i].startsWith("http://")) {
				continue;
			}
			if (words[i].startsWith("https://")) {
				continue;
			}
			switch (words[i]) {
				case "":
				case "#":
				case "-":
				case "@":
				case "(edited)":
					continue;
			}
			String w = words[i].replaceAll("\\(edited\\)", "");
			w = w.replaceAll("@", "");
			w = w.replaceAll("::", "");
			w = w.replaceAll("`", "");
			if (w.matches("<[0-9]+>")) {
				continue;
			}
			if (w.equals("")) {
				continue;
			}
			if (w.equals(" ")) {
				continue;
			}
			if (w.equals("\r\n") || w.equals("\n")) {
				continue;
			}
			validWords.add(w);
		}
		message = String.join(" ", validWords);
	}

    @Override
    public String toString() {
        String object = id + ", " + userId + ", " + discordMessageId + ", " + message;
        return object;
    }

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the discordMessageId
	 */
	public String getDiscordMessageId() {
		return discordMessageId;
	}

	/**
	 * @param discordMessageId the discordMessageId to set
	 */
	public void setDiscordMessageId(String discordMessageId) {
		this.discordMessageId = discordMessageId;
	}
}

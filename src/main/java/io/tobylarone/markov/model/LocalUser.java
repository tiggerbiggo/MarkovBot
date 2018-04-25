package io.tobylarone.markov.model;

/**
 * LocalUser class
 */
public class LocalUser {

    private int id;
    private String discordId;
    private boolean isOptIn;

    /**
     * LocalUser Constructor
     */
    public LocalUser(int id, String discordId, boolean isOptIn) {
        this.id = id;
        this.discordId = discordId;
        this.isOptIn = isOptIn;
	}
	
	public LocalUser(String discordId) {
		this.discordId = discordId;
		this.isOptIn = false;
	}

    @Override
    public String toString() {
        String object = id + ", " + discordId + ", " + isOptIn;
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
	 * @return the discordId
	 */
	public String getDiscordId() {
		return discordId;
	}

	/**
	 * @param discordId the discordId to set
	 */
	public void setDiscordId(String discordId) {
		this.discordId = discordId;
	}

	/**
	 * @return the isOptIn
	 */
	public boolean isOptIn() {
		return isOptIn;
	}

	/**
	 * @param isOptIn the isOptIn to set
	 */
	public void setIsOptIn(boolean isOptIn) {
		this.isOptIn = isOptIn;
	}
}

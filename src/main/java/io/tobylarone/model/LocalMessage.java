package io.tobylarone.model;

/**
 * LocalMessage Class
 */
public class LocalMessage {

    private int id;
    private int userId;
    private String message;

    /**
     * LocalMessage Constructor
     */
    public LocalMessage(int id, int userId, String message) {
        this.id = id;
        this.userId = userId;
        this.message = message;
    }

    @Override
    public String toString() {
        String object = id + ", " + userId + ", " + message;
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
}

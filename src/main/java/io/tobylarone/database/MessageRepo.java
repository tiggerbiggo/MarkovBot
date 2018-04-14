package io.tobylarone.database;

import java.util.List;

import io.tobylarone.model.LocalMessage;

/**
 * MessageRepo Class
 */
public class MessageRepo extends DatabaseWrapper<LocalMessage> {

    /**
     * MessageRepo Constructor
     */
    public MessageRepo() {
    
    }

	@Override
	public LocalMessage findById(int id) {
		return null;
	}

	@Override
	public List<LocalMessage> findAll() {
		return null;
	}
}

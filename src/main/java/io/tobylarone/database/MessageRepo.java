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
	public LocalMessage findByStringId(String id) {
		return null;
	}

	@Override
	public List<LocalMessage> findAll() {
		return null;
	}

	@Override
	public void insert(LocalMessage t) {
		
	}

	@Override
	public void removeById(int id) {
		
	}
}

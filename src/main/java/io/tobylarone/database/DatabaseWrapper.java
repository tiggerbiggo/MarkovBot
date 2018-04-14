package io.tobylarone.database;

import java.util.List;


/**
 * DatabaseWrapper class
 */
public abstract class DatabaseWrapper<T> {

    private DatabaseHandler db;

    /**
     * DatabaseWrapper Constructor
     */
    public DatabaseWrapper() {
        db = new DatabaseHandler();
    }

    public abstract T findById(int id);

    public abstract List<T> findAll();

    public abstract void insert(T t);

	/**
	 * @return the db
	 */
	public DatabaseHandler getDb() {
		return db;
	}

	/**
	 * @param db the db to set
	 */
	public void setDb(DatabaseHandler db) {
		this.db = db;
	}
}

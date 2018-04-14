package io.tobylarone.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.tobylarone.model.LocalUser;

/**
 * LocalUserRepo class
 */
public class LocalUserRepo extends DatabaseWrapper<LocalUser>{

    /**
     * LocalUserRepo Constructor
     */
    public LocalUserRepo() {
    
    }

	@Override
	public List<LocalUser> findAll() {
		List<LocalUser> users = new ArrayList<>();
        String[] fields = {"*"};
        ResultSet results = getDb().select("users", fields);
        try {
			while(results.next()) {
                int id = results.getInt("id");
                String discordId = results.getString("discord_id");
                boolean isOptIn = results.getBoolean("is_opt_in");
                LocalUser user = new LocalUser(id, discordId, isOptIn);
                users.add(user);
            }
		} catch (SQLException e) {
			e.printStackTrace();
        }
        
        getDb().closeQuery();

        return users;
	}

	@Override
	public LocalUser findById(int id) {
		return null;
	}

	@Override
	public void insert(LocalUser t) {
		
	}
}

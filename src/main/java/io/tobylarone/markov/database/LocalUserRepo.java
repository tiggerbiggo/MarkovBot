package io.tobylarone.markov.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import io.tobylarone.markov.model.LocalUser;

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
	public LocalUser findByStringId(String id) {
        LocalUser user = null;
        String[] fields = {"*"};
        ResultSet result = getDb().selectId("users", fields, "discord_id", id);
        try {
            while(result.next()) {
                int resid = result.getInt("id");
                String discordId = result.getString("discord_id");
                boolean isOptIn = result.getBoolean("is_opt_in");
                user = new LocalUser(resid, discordId, isOptIn);
            }
        } catch (NullPointerException e) {
            getDb().closeQuery();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getDb().closeQuery();

        return user;
    }

	@Override
	public void insert(LocalUser t) {
		getDb().insert("users", t);
    }
    
    public void removeById(int id) {
        getDb().removeByField("users", "id", id);
    }

	public void updateOptIn(String discordId, boolean isOptIn) {
		getDb().updateByField("users", "discord_id", discordId, "is_opt_in", isOptIn);
	}

	@Override
	public LocalUser findById(int id) {
        LocalUser user = null;
        String[] fields = {"*"};
        ResultSet result = getDb().selectId("users", fields, "id", id);
        try {
            while (result.next()) {
                int resid = result.getInt("id");
                String discordId = result.getString("discord_id");
                boolean isOptIn = result.getBoolean("is_opt_in");
                user = new LocalUser(resid, discordId, isOptIn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        getDb().closeQuery();

        return user;
	}
}

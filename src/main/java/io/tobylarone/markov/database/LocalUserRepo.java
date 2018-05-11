package io.tobylarone.markov.database;

import java.util.ArrayList;
import java.util.List;

import org.jooq.Record;
import org.jooq.Result;

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
        Result<Record> results = getDb().select("users");

		for (Record r : results) {
			int id = r.getValue("id", Integer.class);
			String discordId = r.getValue("discord_id", String.class);
			boolean isOptIn = r.getValue("is_opt_in", Boolean.class);
			LocalUser user = new LocalUser(id, discordId, isOptIn);
			users.add(user);
		}

        return users;
	}

	@Override
	public LocalUser findByStringId(String id) {
        LocalUser user = null;
        Result<Record> result = getDb().selectBy("users", "discord_id", id);

        if (result.size() != 1) {
            return null;
        }

        for (Record r : result) {
            int resid = r.getValue("id", Integer.class);
            String discordId = r.getValue("discord_id", String.class);
            boolean isOptIn = r.getValue("is_opt_in", Boolean.class);
            user = new LocalUser(resid, discordId, isOptIn);
        }

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
        Result<Record> result = getDb().selectBy("users", "id", id);

        if (result.size() != 1) {
            return null;
        }

        for (Record r : result) {
            int resid = r.getValue("id", Integer.class);
            String discordId = r.getValue("discord_id", String.class);
            boolean isOptIn = r.getValue("is_opt_in", Boolean.class);
            user = new LocalUser(resid, discordId, isOptIn);
        }

        return user;
	}
}

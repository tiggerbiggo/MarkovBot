# Markov Rov

Discord bot to generate sentences using markov chains based on chat history

## Features & Roadmap

#### Key
| Marker | Description |
|--------| ----------- |
| [ ] | Todo |
|  🚶  | In progress |
| [x] | Complete |

- [x] Move to storing chat history in database. Allow updating and re-indexing
- [x] Move to jooq
- [ ] 🚶 Reduce memory usage
- [ ] Implement some usage rate limiting to reduce spam
- [ ] Voting system for generated sentences based on reaction count
- [ ] Generate sentences based on music artists

## About

* Requires maven and mysql (make sure to set char sets to utf8mb4 and collation to utf8mb4_unicode_ci)
* Uses [JDA](https://github.com/DV8FromTheWorld/JDA)
* Requires a `config.properties` file in resources
```
token=DISCORDTOKEN
host=HOST
dbname=DATABASE_NAME
dbuser=DATABASE_USERNAME
dbpass=DATABASE_PASSWORD
dbSSL=false  -- set to true/false
bot.owner.id=DISCORD_USER_ID
```
* Then import db.sql into your database to build structure
* Run: `mvn exec:java`
* Test: `mvn test`
* Build: `mvn package`
* Generate docs: `mvn javadoc:javadoc` output in `/target/site/apidocs/` open `index.html` in a browser.

## Contributing
See [contribution guidelines](CONTRIBUTING.md)
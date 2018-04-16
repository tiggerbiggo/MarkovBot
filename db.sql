CREATE DATABASE IF NOT EXISTS `markov`
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

SET default_storage_engine = INNODB;

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
    `id` int(11) UNIQUE NOT NULL,
    `discord_id` varchar(50) NOT NULL,
    `is_opt_in` BIT(1) NOT NULL DEFAULT 0
);

DROP TABLE IF EXISTS `messages`;

CREATE TABLE `messages` (
    `id` int(11) UNIQUE NOT NULL,
    `user_id` int (11) NOT NULL,
    `message` varchar(2000) NOT NULL,
    `discord_message_id` varchar(50) NOT NULL UNIQUE
);

ALTER TABLE `users`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `id` (`id`);

ALTER TABLE `messages`
    ADD PRIMARY KEY (`id`),
    ADD UNIQUE KEY `id` (`id`),
    ADD KEY `user_fk` (`user_id`);

ALTER TABLE `users`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=0;

ALTER TABLE `messages`
    MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=0;

ALTER TABLE `messages`
    ADD CONSTRAINT `user_fk` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

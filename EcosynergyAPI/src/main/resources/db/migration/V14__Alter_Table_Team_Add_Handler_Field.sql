ALTER TABLE `teams`
ADD COLUMN `handle` VARCHAR(255) NOT NULL UNIQUE AFTER `id`;

ALTER TABLE `teams`
DROP INDEX `name`;

ALTER TABLE `teams`
MODIFY COLUMN `name` VARCHAR(255) NOT NULL;
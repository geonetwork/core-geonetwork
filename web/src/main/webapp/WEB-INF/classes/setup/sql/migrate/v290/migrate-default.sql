ALTER TABLE Users ADD security varchar(128);

UPDATE Users SET security='update_hash_required';

ALTER TABLE Users ALTER COLUMN password varchar(120) not null;

UPDATE Settings SET value='2.9.0' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';
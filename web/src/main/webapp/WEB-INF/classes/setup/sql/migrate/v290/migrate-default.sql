ALTER TABLE Users ADD security varchar(128);
ALTER TABLE Users ADD authtype varchar(32);

UPDATE Users SET security='update_hash_required';

ALTER TABLE Users ALTER COLUMN password varchar(120) not null;

-- Delete LDAP settings
DELETE FROM Settings WHERE parentid=86;
DELETE FROM Settings WHERE parentid=87;
DELETE FROM Settings WHERE parentid=89;
DELETE FROM Settings WHERE parentid=80;
DELETE FROM Settings WHERE id=80;


ALTER TABLE HarvestHistory ADD elapsedTime int;



UPDATE Settings SET value='2.9.0' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';

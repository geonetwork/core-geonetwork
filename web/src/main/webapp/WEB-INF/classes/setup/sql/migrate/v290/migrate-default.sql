INSERT INTO Settings VALUES (89,80,'bind',NULL);
INSERT INTO Settings VALUES (102,86,'subtree','false');
INSERT INTO Settings VALUES (140,89,'bindDn','cn=fake.name,ou=people,dc=fao,dc=org');
INSERT INTO Settings VALUES (141,89,'bindPw','fake_password');
INSERT INTO Settings VALUES (150,80,'anonBind','true');

ALTER TABLE Users ADD security varchar(128);
ALTER TABLE Users ADD authtype varchar(32);

UPDATE Users SET security='update_hash_required';

ALTER TABLE Users ALTER COLUMN password varchar(120) not null;

-- Add current user profile to all its groups
UPDATE usergroups SET profile = (SELECT profile from users WHERE id = userid);

UPDATE Settings SET value='2.9.0' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';

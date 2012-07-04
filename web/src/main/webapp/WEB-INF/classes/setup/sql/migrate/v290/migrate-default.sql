
-- Add current user profile to all its groups
UPDATE usergroups SET profile = (SELECT profile from users WHERE id = userid);

UPDATE Settings SET value='2.9.0' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';

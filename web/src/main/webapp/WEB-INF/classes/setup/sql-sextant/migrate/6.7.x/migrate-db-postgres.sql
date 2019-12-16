UPDATE Sources SET type = 'portal' WHERE type IS null AND uuid = (SELECT value FROM settings WHERE name = 'system/site/siteId');
UPDATE Sources SET type = 'harvester' WHERE type IS null AND uuid != (SELECT value FROM settings WHERE name = 'system/site/siteId');

-- Copy the current UI setting
INSERT INTO Settings_ui (id, configuration) (SELECT 'srv', value FROM Settings WHERE name = 'ui/config');
DELETE FROM Settings WHERE name = 'ui/config';

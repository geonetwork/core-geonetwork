-- 3.8.2
UPDATE Sources SET type = 'portal' WHERE type IS null AND uuid = (SELECT value FROM settings WHERE name = 'system/site/siteId');
UPDATE Sources SET type = 'harvester' WHERE type IS null AND uuid != (SELECT value FROM settings WHERE name = 'system/site/siteId');



UPDATE Settings SET value='3.9.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

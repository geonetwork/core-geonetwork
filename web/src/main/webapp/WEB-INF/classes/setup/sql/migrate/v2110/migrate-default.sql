INSERT INTO HarvesterSettings VALUES  (1,NULL,'harvesting',NULL);
-- Copy all harvester's root nodes config
INSERT INTO HarvesterSettings SELECT id, 1, name, value FROM Settings WHERE parentId = 2;
-- Copy all harvester's properties (Greater than last 2.10.0 settings ie. keepMarkedElement)
INSERT INTO HarvesterSettings SELECT * FROM Settings WHERE id > (SELECT max(id) FROM HarvesterSettings);
-- Drop harvester config from Settings table
DELETE FROM Settings WHERE id > 958;
DELETE FROM Settings WHERE id=2;

-- Version update
UPDATE Settings SET value='2.11.0' WHERE name='version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='subVersion';

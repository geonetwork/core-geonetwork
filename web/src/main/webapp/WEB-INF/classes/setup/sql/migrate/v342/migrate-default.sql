INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/url', '', 0, 7211, 'n');
UPDATE Settings SET internal='n' WHERE name='system/inspire/enable';

UPDATE Settings SET name = 'system/localrating/enable', datatype = 0, value = 'off' WHERE position = 2110 and value = 'n';
UPDATE Settings SET name = 'system/localrating/enable', datatype = 0, value = 'basic' WHERE position = 2110 and value = 'y';

UPDATE Settings SET value='3.4.2' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvester/disabledHarvesterTypes', '', 0, 9011, 'n');

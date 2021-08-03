INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/nodeid', '', 0, 7212, 'n');

UPDATE Settings SET value='4.0.6' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

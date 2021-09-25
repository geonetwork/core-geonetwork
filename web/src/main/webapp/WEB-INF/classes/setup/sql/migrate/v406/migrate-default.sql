INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/nodeid', '', 0, 7212, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/apikey', '', 0, 7213, 'y');

DELETE FROM Settings WHERE name = 'system/server/securePort';

UPDATE Settings SET value='4.0.6' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

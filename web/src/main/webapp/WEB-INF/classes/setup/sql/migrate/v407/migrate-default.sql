INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/csvReport/csvName', 'metadata_{datetime}.csv', 0, 12607, 'n');

UPDATE Settings SET value='4.0.7' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

UPDATE Settings set position = 7213 WHERE name = 'system/inspire/remotevalidation/nodeid';
UPDATE Settings set position = 7214 WHERE name = 'system/inspire/remotevalidation/apikey';
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/urlquery', '', 0, 7212, 'n');

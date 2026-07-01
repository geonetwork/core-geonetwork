INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/getRecordsIgnoreMetadataNotSupported', 'true', 2, 1321, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/publication/managepublicationdate', 'false', 2, 9182, 'n');
UPDATE Settings SET position=9183 WHERE name='system/metadataprivs/publication/notificationLevel';
UPDATE Settings SET position=9184 WHERE name='system/metadataprivs/publication/notificationGroups';

UPDATE Settings SET value='4.4.12' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

DROP SEQUENCE IF EXISTS files_id_seq;
DROP TABLE IF EXISTS files;

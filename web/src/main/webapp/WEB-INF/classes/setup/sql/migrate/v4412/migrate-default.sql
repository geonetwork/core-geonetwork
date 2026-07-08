INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/getRecordsIgnoreMetadataNotSupported', 'true', 2, 1321, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/oai/enable', 'true', 2, 7000, 'n'  from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/oai/enable');

UPDATE Settings SET value='4.4.12' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

DROP SEQUENCE IF EXISTS files_id_seq;
DROP TABLE IF EXISTS files;

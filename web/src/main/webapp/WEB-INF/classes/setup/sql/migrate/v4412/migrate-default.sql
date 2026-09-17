INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/getRecordsIgnoreMetadataNotSupported', 'true', 2, 1321, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/oai/enable', 'true', 2, 7000, 'n'  from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/oai/enable');

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadata/thesaurusUrlAllowlist', '', 0, 9162, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadata/thesaurusUrlAllowlist');

-- Move metadata/vcs/enable to a free position so it no longer shares position 9161 with system/metadata/thesaurusNamespace.
UPDATE Settings SET position = '9164' WHERE name = 'metadata/vcs/enable' AND position = '9161';

UPDATE Settings SET value='4.4.12' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

DROP SEQUENCE IF EXISTS files_id_seq;
DROP TABLE IF EXISTS files;

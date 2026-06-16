UPDATE Settings SET value='4.2.17' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

DROP SEQUENCE IF EXISTS files_id_seq;
DROP TABLE IF EXISTS files;

UPDATE Settings SET value='3.0.4' WHERE name='system/platform/version';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/lock', '-1', 1, 100003, 'n');
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';
ALTER TABLE HarvesterData RENAME COLUMN "key" TO keyvalue;
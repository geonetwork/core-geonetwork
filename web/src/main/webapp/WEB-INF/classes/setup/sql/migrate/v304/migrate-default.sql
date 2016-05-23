UPDATE Settings SET value='3.0.4' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';
ALTER TABLE HarvesterData RENAME COLUMN "key" TO keyvalue;
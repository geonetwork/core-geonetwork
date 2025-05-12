UPDATE Settings SET value='4.2.13' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/delete/backupOptions', 'UseAPIParameter', 0, 12012, 'n');

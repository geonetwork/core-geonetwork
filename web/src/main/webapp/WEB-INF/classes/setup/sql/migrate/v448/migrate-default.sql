UPDATE Settings SET value='4.4.8' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/delete/backupOptions', 'NoPreference', 0, 12012, 'n');

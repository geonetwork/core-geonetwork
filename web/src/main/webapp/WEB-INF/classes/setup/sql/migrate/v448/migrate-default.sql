UPDATE Settings SET value='4.4.8' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'metadata/delete/backupOptions', 'UseAPIParameter', 0, 12012, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'metadata/delete/backupOptions');

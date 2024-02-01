UPDATE Settings SET value='4.4.3' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

UPDATE settings SET name='metadata/history/enabled' WHERE name='system/metadata/history/enabled'
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'metadata/history/accesslevel', 'Editor', 0, 12021, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'metadata/history/accesslevel');

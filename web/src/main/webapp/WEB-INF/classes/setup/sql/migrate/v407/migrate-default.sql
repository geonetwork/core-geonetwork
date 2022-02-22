INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/csvReport/csvName', 'metadata_{datetime}.csv', 0, 12607, 'n');

UPDATE Settings SET value='4.0.7' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

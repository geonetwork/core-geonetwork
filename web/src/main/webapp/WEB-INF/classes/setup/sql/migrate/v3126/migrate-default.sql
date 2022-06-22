INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/delete/profilePublishedMetadata', 'Editor', 0, 12011, 'n');

UPDATE Settings SET value='3.12.6' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/publication/notificationLevel', '', 0, 9182, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/publication/notificationGroups', '', 0, 9183, 'n');

UPDATE Settings SET value='3.12.8' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

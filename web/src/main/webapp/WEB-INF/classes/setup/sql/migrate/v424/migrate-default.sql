INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/publication/profilePublishMetadata', 'Reviewer', 0, 12021, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/publication/profileUnpublishMetadata', 'Reviewer', 0, 12022, 'n');

UPDATE Settings SET value='4.2.4' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

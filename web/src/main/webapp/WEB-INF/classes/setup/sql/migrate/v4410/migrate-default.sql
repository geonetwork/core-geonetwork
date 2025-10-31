INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadatacreate/publishForGroupEditors', 'false', 2, 9101, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadatacreate/publishForGroupEditors');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadatacreate/copyAttachments', 'true', 2, 9102, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadatacreate/copyAttachments');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadatacreate/skipMetadataCreationPage', 'false', 2, 9103, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadatacreate/skipMetadataCreationPage');

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadata/edit/supportedFileMimetypes', 'image/png|image/gif|image/jpeg|text/plain|application/xml|application/pdf', 0, 9107, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadata/edit/supportedFileMimetypes');

UPDATE Settings SET value='4.4.10' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

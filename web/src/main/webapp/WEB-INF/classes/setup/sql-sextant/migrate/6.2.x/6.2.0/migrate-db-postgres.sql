UPDATE Users SET enabled = true;
UPDATE Mapservers set pushstyleinworkspace = 'n';

UPDATE metadata SET schemaid = 'iso19139' WHERE schemaid = 'iso19139.sextant';


-- TODO : resource link change cf. MetadataResourceDatabaseMigration


UPDATE Settings SET value='3.1.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';
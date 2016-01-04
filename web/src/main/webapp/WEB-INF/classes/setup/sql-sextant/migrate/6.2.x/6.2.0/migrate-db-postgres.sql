UPDATE Users SET enabled = true;
UPDATE Mapservers set pushstyleinworkspace = 'n';

UPDATE metadata SET schemaid = 'iso19139' WHERE schemaid = 'iso19139.sextant';

UPDATE Settings SET value='3.0.4' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadataprivs/publication/managepublicationdate', 'false', 2, 9182, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadataprivs/publication/managepublicationdate');
UPDATE Settings SET position=9183 WHERE name='system/metadataprivs/publication/notificationLevel';
UPDATE Settings SET position=9184 WHERE name='system/metadataprivs/publication/notificationGroups';

UPDATE Settings SET value='4.4.13' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

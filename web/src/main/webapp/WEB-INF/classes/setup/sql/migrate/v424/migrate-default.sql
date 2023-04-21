INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/publication/profilePublishMetadata', 'Reviewer', 0, 12021, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/publication/profileUnpublishMetadata', 'Reviewer', 0, 12022, 'n');

UPDATE Settings SET value='4.2.4' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- Migrate setting names
UPDATE Settings SET name = replace(name, 'system/', 'catalog/') WHERE name LIKE 'system/site/*';
UPDATE Settings SET name = replace(name, 'system/', 'catalog/') WHERE name LIKE 'system/platform/*';
UPDATE Settings SET name = replace(name, 'system/', 'catalog/') WHERE name LIKE 'system/server/*';
UPDATE Settings SET name = replace(name, 'system/', 'network/') WHERE name LIKE 'system/intranet/*';
UPDATE Settings SET name = replace(name, 'system/', 'network/') WHERE name LIKE 'system/proxy/*';
UPDATE Settings SET name = replace(name, 'system/', 'network/') WHERE name LIKE 'system/cors/*';
UPDATE Settings SET name = replace(name, 'system/', 'catalog/') WHERE name LIKE 'system/feedback/*';
UPDATE Settings SET name = replace(name, 'system/', 'services/') WHERE name LIKE 'system/csw/*';
UPDATE Settings SET name = replace(name, 'system/', 'usersgroups/') WHERE name LIKE 'system/userSelfRegistration/*';
UPDATE Settings SET name = replace(name, 'system/', 'metadata/') WHERE name LIKE 'system/clickablehyperlinks/*';
UPDATE Settings SET name = replace(name, 'system/', 'metadata/') WHERE name LIKE 'system/localrating/*';
UPDATE Settings SET name = replace(name, 'system/', 'metadata/') WHERE name LIKE 'system/xlinkResolver/*';
UPDATE Settings SET name = replace(name, 'system/', 'metadata/') WHERE name LIKE 'system/hidewithheldelements/*';
UPDATE Settings SET name = replace(name, 'system/', 'metadata/') WHERE name LIKE 'system/autofixing/*';
UPDATE Settings SET name = replace(name, 'system/', 'catalog/') WHERE name LIKE 'system/searchStats/*';
UPDATE Settings SET name = replace(name, 'system/', 'services/') WHERE name LIKE 'system/oai/*';
UPDATE Settings SET name = replace(name, 'system/', '') WHERE name LIKE 'system/inspire/*';
UPDATE Settings SET name = replace(name, 'system/', '') WHERE name LIKE 'system/harvester/*';
UPDATE Settings SET name = replace(name, 'system/harvesting/', 'harvester/') WHERE name LIKE 'system/harvesting/*';
UPDATE Settings SET name = replace(name, 'system/', 'usersgroups/') WHERE name LIKE 'system/users/*';
UPDATE Settings SET name = replace(name, 'system/', 'metadata/') WHERE name LIKE 'system/metadata/*';
UPDATE Settings SET name = replace(name, 'system/', 'metadata/') WHERE name LIKE 'system/metadatacreate/*';
UPDATE Settings SET name = replace(name, 'system/', 'metadata/') WHERE name LIKE 'system/metadataprivs/*';
UPDATE Settings SET name = replace(name, 'system/', 'catalog/') WHERE name LIKE 'system/index/*';
UPDATE Settings SET name = replace(name, 'system/threadedindexing/', 'catalog/') WHERE name LIKE 'system/index/*';
UPDATE Settings SET name = replace(name, 'system/', 'inspire/') WHERE name LIKE 'system/inspire/*';
UPDATE Settings SET name = replace(name, 'system/', 'catalog/') WHERE name LIKE 'system/ui/*';
UPDATE Settings SET name = replace(name, 'system/', 'usersgroups/') WHERE name LIKE 'system/userSelfRegistration/*';
UPDATE Settings SET name = replace(name, 'system/', 'metadata/') WHERE name LIKE 'system/publication/*';
UPDATE Settings SET name = replace(name, 'system/', 'usersgroups/') WHERE name LIKE 'system/security/*';

UPDATE Settings SET name = replace(name, 'catalog/feedback/', 'catalog/mail/') WHERE name LIKE 'catalog/feedback/*';

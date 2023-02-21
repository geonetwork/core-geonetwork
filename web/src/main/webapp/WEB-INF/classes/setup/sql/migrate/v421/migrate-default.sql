
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/publicationbyrevieweringroupowneronly', 'true', 2, 9181, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doipattern', '{{uuid}}', 0, 100197, 'n');

-- Changes were back ported to version 3.12.x so they are no longer required unless upgrading from previous v42x which did not have 3.12.x  migrations steps.
-- So lets try to only add the records if they don't already exists.
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'metadata/delete/profilePublishedMetadata', 'Editor', 0, 12011, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'metadata/delete/profilePublishedMetadata');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadataprivs/publication/notificationLevel', '', 0, 9182, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadataprivs/publication/notificationLevel');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadataprivs/publication/notificationGroups', '', 0, 9183, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadataprivs/publication/notificationGroups');

-- cf. https://www.un.org/en/about-us/member-states/turkiye (run this manually if it applies to your catalogue)
-- UPDATE metadata SET data = replace(data, 'Turkey', 'Türkiye') WHERE data LIKE '%Turkey%';
UPDATE Settings SET value='log4j2.xml' WHERE name='system/server/log';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/localrating/notificationGroups', '', 0, 2112, 'n');

UPDATE Settings SET value='4.2.1' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

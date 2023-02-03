
DELETE FROM Schematrondes WHERE iddes IN (SELECT id FROM schematron WHERE filename LIKE 'schematron-rules-inspire%');
DELETE FROM Schematroncriteria WHERE group_name || group_schematronid IN (SELECT name || schematronid FROM schematroncriteriagroup WHERE schematronid IN (SELECT id FROM schematron WHERE filename LIKE 'schematron-rules-inspire%'));
DELETE FROM Schematroncriteriagroup WHERE schematronid IN (SELECT id FROM schematron WHERE filename LIKE 'schematron-rules-inspire%');
DELETE FROM Schematron WHERE filename LIKE 'schematron-rules-inspire%';



UPDATE Settings SET internal = 'n' WHERE name = 'system/metadata/prefergrouplogo';

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/security/passwordEnforcement/minLength', '6', 1, 12000, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/security/passwordEnforcement/minLength');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/security/passwordEnforcement/maxLength', '20', 1, 12001, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/security/passwordEnforcement/maxLength');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/security/passwordEnforcement/usePattern', 'true', 2, 12002, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/security/passwordEnforcement/usePattern');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/security/passwordEnforcement/pattern', '^((?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*(_|[^\w])).*)$', 0, 12003, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/security/passwordEnforcement/pattern');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/nodeid', '', 0, 7212, 'n');

-- Changes were back ported to version 3.12.x so they are no longer required unless upgrading from previous v40x which did not have 3.12.x  migrations steps.
-- So lets try to only add the records if they don't already exists.
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/inspire/remotevalidation/apikey', '', 0, 7213, 'y' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/inspire/remotevalidation/apikey');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/publication/doi/doipublicurl', '', 0, 100196, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/publication/doi/doipublicurl');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadatacreate/preferredGroup', '', 1, 9105, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadatacreate/preferredTemplate', '', 0, 9106, 'n');

DELETE FROM Settings WHERE name = 'system/server/securePort';

UPDATE Settings SET value = '0 0 0 * * ?' WHERE name = 'system/inspire/atomSchedule' and value = '0 0 0/24 ? * *';

UPDATE Settings SET value='4.0.6' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';


UPDATE Settings set position = 7213 WHERE name = 'system/inspire/remotevalidation/nodeid';
UPDATE Settings set position = 7214 WHERE name = 'system/inspire/remotevalidation/apikey';

-- Changes were back ported to version 3.12.x so they are no longer required unless upgrading from previous v40x which did not have 3.12.x  migrations steps.
-- So lets try to only add the records if they don't already exists.
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/inspire/remotevalidation/urlquery', '', 0, 7212, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/inspire/remotevalidation/urlquery');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'metadata/import/userprofile', 'Editor', 0, 12001, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'metadata/import/userprofile');

alter table settings
  add editable char default 'y' not null;

UPDATE Settings SET editable = 'n' WHERE name = 'system/userFeedback/lastNotificationDate';
UPDATE Settings SET editable = 'n' WHERE name = 'system/security/passwordEnforcement/pattern';

UPDATE Settings SET value='4.0.7' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';



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

DELETE FROM Settings WHERE name = 'system/downloadservice/leave';
DELETE FROM Settings WHERE name = 'system/downloadservice/simple';
DELETE FROM Settings WHERE name = 'system/downloadservice/withdisclaimer';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/url/sitemapLinkUrl', NULL, 0, 9165, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/url/sitemapDoiFirst', 'false', 2, 9166, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/url/dynamicAppLinkUrl', NULL, 0, 9167, 'y');




UPDATE Settings SET value='4.2.2' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

UPDATE Settings SET value='4.2.3' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/csvReport/csvName', 'metadata_{datetime}.csv', 0, 12607, 'n');

UPDATE Settings set position = 7213 WHERE name = 'system/inspire/remotevalidation/nodeid';
UPDATE Settings set position = 7214 WHERE name = 'system/inspire/remotevalidation/apikey';

-- Changes were back ported to version 3.12.x so they are no longer required unless upgrading from previous v40x which did not have 3.12.x  migrations steps.
-- So lets try to only add the records if they don't already exists.
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/inspire/remotevalidation/urlquery', '', 0, 7212, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/inspire/remotevalidation/urlquery');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'metadata/import/userprofile', 'Editor', 0, 12001, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'metadata/import/userprofile');

INSERT INTO Users (id, username, password, name, surname, profile, kind, organisation, security, authtype, isenabled) VALUES  (0,'nobody','','nobody','nobody',4,'','','','', 'n');
INSERT INTO Address (id, address, city, country, state, zip) VALUES  (0, '', '', '', '', '');
INSERT INTO UserAddress (userid, addressid) VALUES  (0, 0);

-- WARNING: Security / Add this settings only if you need to allow admin
-- users to be able to reset user password. If you have mail server configured
-- user can reset password directly. If not, then you may want to add that settings
-- if you don't have access to the database.
-- INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/password/allowAdminReset', 'false', 2, 12004, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/link/excludedUrlPattern', '', 0, 12010, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/thesaurusNamespace', 'https://registry.geonetwork-opensource.org/{{type}}/{{filename}}', 0, 9161, 'n');

UPDATE Settings SET editable = 'n' WHERE name = 'system/userFeedback/lastNotificationDate';
UPDATE Settings SET editable = 'n' WHERE name = 'system/security/passwordEnforcement/pattern';

UPDATE Settings SET value='4.0.7' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

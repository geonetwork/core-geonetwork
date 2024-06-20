UPDATE Settings SET value='4.2.11' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/domainsAllowed', '', 0, 1911, 'y');

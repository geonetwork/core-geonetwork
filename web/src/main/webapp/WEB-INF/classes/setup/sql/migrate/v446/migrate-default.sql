UPDATE Settings SET value='4.4.6' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/userSelfRegistration/domainsAllowed', '', 0, 1911, 'y' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/userSelfRegistration/domainsAllowed');

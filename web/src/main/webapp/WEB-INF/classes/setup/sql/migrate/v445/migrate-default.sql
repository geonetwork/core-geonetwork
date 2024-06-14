UPDATE Settings SET value='4.4.5' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/translation/provider', '', 0, 7301, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/translation/serviceUrl', '', 0, 7302, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/translation/apiKey', '', 0, 7303, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/domainsAllowed', '', 0, 1911, 'y');

UPDATE Settings SET value='4.4.6' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/domainsAllowed', '', 0, 1911, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/banner/enable', 'false', 2, 1920, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/banner/message', '', 0, 1921, 'n');

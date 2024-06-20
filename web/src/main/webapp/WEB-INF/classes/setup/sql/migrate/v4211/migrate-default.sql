UPDATE Settings SET value='4.2.11' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/domainsAllowed', '', 0, 1911, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/feedback/languages', '', 0, 646, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/feedback/languages');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/feedback/translationFollowsText', '', 0, 647, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/feedback/translationFollowsText');

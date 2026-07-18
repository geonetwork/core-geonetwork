UPDATE Settings SET value='4.4.5' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/translation/provider', '', 0, 7301, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/translation/provider');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/translation/serviceUrl', '', 0, 7302, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/translation/serviceUrl');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/translation/apiKey', '', 0, 7303, 'y' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/translation/apiKey');

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/feedback/languages', '', 0, 646, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/feedback/languages');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/feedback/translationFollowsText', '', 0, 647, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/feedback/translationFollowsText');


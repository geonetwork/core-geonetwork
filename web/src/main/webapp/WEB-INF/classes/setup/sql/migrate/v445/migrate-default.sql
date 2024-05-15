UPDATE Settings SET value='4.4.5' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/languages', '', 0, 646, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/translationFollowsText', '', 0, 647, 'n');

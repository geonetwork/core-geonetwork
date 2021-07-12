UPDATE Settings SET value='4.0.6' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/proxyUrl', '', 0, 1221, 'y');

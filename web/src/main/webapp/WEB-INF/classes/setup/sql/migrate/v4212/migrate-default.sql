UPDATE Settings SET value='4.2.12' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/banner/enable', 'false', 2, 1920, 'n');

UPDATE Settings SET value='4.4.7' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/banner/enable', 'false', 2, 1920, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/banner/enable');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/auditable/enable', 'false', 2, 12010, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/auditable/enable');

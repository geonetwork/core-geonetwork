UPDATE Settings SET value='4.0.7' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/templatesToOperateOnAtInsert', '', 0, 2314, 'n');


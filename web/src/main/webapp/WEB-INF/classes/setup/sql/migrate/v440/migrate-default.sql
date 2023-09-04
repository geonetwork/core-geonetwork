UPDATE Settings SET value='4.4.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/analytics/type', '', 0, 12010, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/analytics/jscode', '', 0, 12011, 'n');

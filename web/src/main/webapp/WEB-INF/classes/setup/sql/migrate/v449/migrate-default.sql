INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doimailnotification', 'false', 2, 100192, 'n');

UPDATE Settings SET value='4.4.9' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

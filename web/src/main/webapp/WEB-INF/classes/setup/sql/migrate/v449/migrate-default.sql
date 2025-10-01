INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doimailnotification', 'false', 2, 100192, 'n');

-- Migration to 4.4.5 only removed the old DOI settings, when the DOI server was defined.
-- Related to https://github.com/geonetwork/core-geonetwork/pull/8098
DELETE FROM Settings WHERE name LIKE 'system/publication/doi%' and name != 'system/publication/doi/doienabled';

UPDATE Settings SET value='4.4.9' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

UPDATE groups SET type='RecordPrivilege' WHERE id<2 AND type IS NULL;
UPDATE groups SET type='Workspace' WHERE id>=2 AND type IS NULL;

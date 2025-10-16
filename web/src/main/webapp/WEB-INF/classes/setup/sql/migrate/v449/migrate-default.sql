INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/publication/doi/doimailnotification', 'false', 2, 100192, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/publication/doi/doimailnotification');

-- Migration to 4.4.5 only removed the old DOI settings, when the DOI server was defined.
-- Related to https://github.com/geonetwork/core-geonetwork/pull/8098
DELETE FROM Settings WHERE name LIKE 'system/publication/doi%' and name != 'system/publication/doi/doienabled';

UPDATE Settings SET value='4.4.9' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

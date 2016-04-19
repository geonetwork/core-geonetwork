DELETE FROM Settings WHERE name = 'system/autodetect/enable';
DELETE FROM Settings WHERE name LIKE 'system/requestedLanguage/*';
DELETE FROM Settings WHERE name LIKE 'system/searchStats/*';
DELETE FROM Settings WHERE name LIKE 'system/indexoptimizer/*';

DROP TABLE Params;
DROP TABLE Requests;

UPDATE Settings SET value='4.0.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

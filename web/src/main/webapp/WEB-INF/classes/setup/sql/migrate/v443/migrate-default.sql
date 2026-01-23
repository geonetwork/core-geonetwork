INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct'system/documentation/url', 'https://docs.geonetwork-opensource.org/{{version}}/{{lang}}', 0, 570, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/documentation/url');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct'system/userFeedback/metadata/enable', 'false', 2, 1913, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/userFeedback/metadata/enable');


UPDATE Settings SET value='4.4.3' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

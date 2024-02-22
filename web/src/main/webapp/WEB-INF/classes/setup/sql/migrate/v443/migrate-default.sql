UPDATE Settings SET value='4.4.3' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/documentation/url', 'https://docs.geonetwork-opensource.org/{{version}}/{{lang}}', 0, 570, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userFeedback/metadata/enable', 'false', 2, 1913, 'n');


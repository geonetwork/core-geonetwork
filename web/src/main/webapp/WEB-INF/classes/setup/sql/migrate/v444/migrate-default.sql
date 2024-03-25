UPDATE Metadata SET data = replace(data, 'http://standards.iso.org/iso/19115/-3/srv/2.1', 'http://standards.iso.org/iso/19115/-3/srv/2.0') WHERE data LIKE '%http://standards.iso.org/iso/19115/-3/srv/2.1%' AND schemaId = 'iso19115-3.2018';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/edit/supportedFileMimetypes', 'image/png|image/gif|image/jpeg|text/plain|application/xml|application/pdf', 0, 9107, 'n');

UPDATE Settings SET value='4.4.4' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

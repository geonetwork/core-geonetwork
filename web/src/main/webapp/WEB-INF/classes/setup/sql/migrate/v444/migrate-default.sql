UPDATE Metadata SET data = replace(data, 'http://standards.iso.org/iso/19115/-3/srv/2.1', 'http://standards.iso.org/iso/19115/-3/srv/2.0') WHERE data LIKE '%http://standards.iso.org/iso/19115/-3/srv/2.1%' AND schemaId = 'iso19115-3.2018';

UPDATE Settings SET value='4.4.4' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

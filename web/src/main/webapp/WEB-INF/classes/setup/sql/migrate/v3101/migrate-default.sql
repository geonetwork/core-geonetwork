DELETE FROM cswservercapabilitiesinfo;
DELETE FROM Settings WHERE name = 'system/csw/contactId';
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/capabilityRecordUuid', '-1', 0, 1220, 'y');

-- Increase the length of Validation type (where the schematron file name is stored)
ALTER TABLE Validation ALTER COLUMN valType TYPE varchar(128);

UPDATE Settings SET value='3.10.1' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

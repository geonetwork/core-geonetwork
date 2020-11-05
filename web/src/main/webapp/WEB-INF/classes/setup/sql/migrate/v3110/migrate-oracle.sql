
UPDATE Settings SET value='3.11.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- Increase the length of Validation type (where the schematron file name is stored)
ALTER TABLE Validation ALTER COLUMN valType TYPE varchar(128);

ALTER TABLE usersearch ADD (tempurl clob);
ALTER TABLE usersearch SET tempurl = url, url = null;
ALTER TABLE usersearch DROP COLUMN url;
ALTER TABLE usersearch RENAME COLUMN tempurl to url;


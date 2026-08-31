UPDATE Settings SET value='3.10.3' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

ALTER TABLE groupsdes MODIFY label varchar(255);
ALTER TABLE sourcesdes MODIFY label varchar(255);
ALTER TABLE schematrondes MODIFY label varchar(255);

-- New setting for server timezone
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/timeZone', '', 0, 260, 'n');

-- keep these at the bottom of the file!
DROP INDEX idx_metadatafiledownloads_metadataid;
DROP INDEX idx_metadatafileuploads_metadataid;
DROP INDEX idx_operationallowed_metadataid;

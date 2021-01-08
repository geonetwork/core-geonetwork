UPDATE Settings SET value='3.10.3' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

ALTER TABLE groupsdes ALTER COLUMN label TYPE varchar(255);
ALTER TABLE sourcesdes ALTER COLUMN label TYPE varchar(255);
ALTER TABLE schematrondes ALTER COLUMN label TYPE varchar(255);

-- New setting for server timezone
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/timeZone', '', 0, 260, 'n');

DROP INDEX idx_metadatafiledownloads_metadataid ON MetadataFileDownloads;
DROP INDEX idx_metadatafileuploads_metadataid ON MetadataFileUploads;
DROP INDEX idx_operationallowed_metadataid ON OperationAllowed;

UPDATE Settings SET value='4.4.2' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/useGeodesicExtents', 'false', 2, 9591, 'n');

DELETE FROM Settings WHERE name = 'system/index/indexingTimeRecordLink';

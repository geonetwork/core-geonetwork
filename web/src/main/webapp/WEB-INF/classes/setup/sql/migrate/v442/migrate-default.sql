UPDATE Settings SET value='4.4.2' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/useGeodesicExtents', 'false', 2, 9591, 'n');

ALTER TABLE public.spg_page ADD icon varchar NULL;

UPDATE settings SET name='metadata/history/enabled' WHERE name='system/metadata/history/enabled'
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'metadata/history/accesslevel', 'Editor', 0, 12021, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'metadata/history/accesslevel');

UPDATE Settings SET value='3.0.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/background', 'osm', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/width', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/summaryWidth', '500', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('region/getmap/mapproj', 'EPSG:3857', 0, 9590, 'n');


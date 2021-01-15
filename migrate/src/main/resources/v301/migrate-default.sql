
INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/is3DModeAllowed', 'false', 2, 9593, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/isSaveMapInCatalogAllowed', 'true', 2, 9594, 'n');

UPDATE Settings SET value='3.0.1' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

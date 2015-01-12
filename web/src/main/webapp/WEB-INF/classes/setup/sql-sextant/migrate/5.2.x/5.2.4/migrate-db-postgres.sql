UPDATE Settings SET value =
  '{"useOSM":true,"context":"","layer":{"url":"http://www2.demis.nl/mapserver/wms.asp?","layers":"Countries","version":"1.1.1"},"projection":"EPSG:3857","projectionList":[{"code":"EPSG:4326","label":"WGS84 (EPSG:4326)"},{"code":"EPSG:3857","label":"Google mercator (EPSG:3857)"}]}', 0, 9590, 'n')
  WHERE name = 'map/config';


-- See https://github.com/geonetwork/core-geonetwork/commit/8ec082bc48c613b53acb06963d29c9a734482ba9
-- withheld element filter is now configured as a per schema basis
DELETE FROM Settings WHERE name = 'system/hidewithheldelements/enable';
DELETE FROM Settings WHERE name = 'system/hidewithheldelements/keepMarkedElement';
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/hidewithheldelements/enableLogging', 'false', 2, 2320, 'y');

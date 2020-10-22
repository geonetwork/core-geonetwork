
UPDATE Metadata SET data = replace(data, 'WGS 84 (EPSG:4326)', 'WGS 84 (EPSG:4326) - Equirectangular projection') WHERE data LIKE '%WGS 84 (EPSG:4326)%' AND schemaId = 'iso19115-3.2018';
UPDATE Metadata SET data = replace(data, 'ETRS89 (EPSG:4258)', 'ETRS89 (EPSG:4258) - Equirectangular projection') WHERE data LIKE '%ETRS89 (EPSG:4258)%' AND schemaId = 'iso19115-3.2018';
UPDATE Metadata SET data = replace(data, 'ETRS89 / LAEA Europe (EPSG:3035)', 'ETRS89 / LAEA Europe (EPSG:3035) - Lambert azimuthal equal-area projection') WHERE data LIKE '%ETRS89 / LAEA Europe (EPSG:3035)%' AND schemaId = 'iso19115-3.2018';

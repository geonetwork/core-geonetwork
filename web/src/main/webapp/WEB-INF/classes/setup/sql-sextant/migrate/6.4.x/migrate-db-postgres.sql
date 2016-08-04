INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/ignoreSslCertificateErrors', 'false', 2, 645, 'y');

DELETE FROM Settings WHERE name = 'system/z3950/enable';
DELETE FROM Settings WHERE name = 'system/z3950/port';

DELETE FROM Settings WHERE name = 'system/removedMetadata/dir';

UPDATE Settings SET datatype = 3 WHERE name = 'map/config';
UPDATE Settings SET datatype = 3 WHERE name = 'map/proj4js';
UPDATE Settings SET datatype = 3 WHERE name = 'metadata/editor/schemaConfig';

UPDATE OperationsDes SET label = 'Traitement' WHERE iddes = 7 AND langid = 'fre';

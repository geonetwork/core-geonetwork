UPDATE Settings SET value='3.1.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

DELETE FROM Settings WHERE name = 'system/metadata/enableSimpleView';
DELETE FROM Settings WHERE name = 'system/metadata/enableIsoView';
DELETE FROM Settings WHERE name = 'system/metadata/enableInspireView';
DELETE FROM Settings WHERE name = 'system/metadata/enableXmlView';
DELETE FROM Settings WHERE name = 'system/metadata/defaultView';


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/tls', 'false', 2, 644, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/ignoreSslCertificateErrors', 'false', 2, 645, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/ignore', 'operatesOn,featureCatalogueCitation,Anchor,source', 0, 2312, 'n');

DELETE FROM Settings WHERE name = 'system/z3950/enable';
DELETE FROM Settings WHERE name = 'system/z3950/port';
DELETE FROM Settings WHERE name = 'system/removedMetadata/dir';


UPDATE Settings SET datatype = 3 WHERE name = 'map/config';
UPDATE Settings SET datatype = 3 WHERE name = 'map/proj4js';
UPDATE Settings SET datatype = 3 WHERE name = 'metadata/editor/schemaConfig';

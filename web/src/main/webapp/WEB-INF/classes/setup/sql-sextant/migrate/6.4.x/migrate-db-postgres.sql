INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/ignoreSslCertificateErrors', 'false', 2, 645, 'y');

DELETE FROM Settings WHERE name = 'system/z3950/enable';
DELETE FROM Settings WHERE name = 'system/z3950/port';

DELETE FROM Settings WHERE name = 'system/removedMetadata/dir';

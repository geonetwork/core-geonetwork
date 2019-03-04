DELETE FROM Settings WHERE  name = 'metadata/editor/schemaConfig';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/validation/removeSchemaLocation', 'false', 2, 9170, 'n');

UPDATE Settings SET value='3.4.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

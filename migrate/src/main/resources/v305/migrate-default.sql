INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/allowPublishInvalidMd', 'true', 2, 100003, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/automaticUnpublishInvalidMd', 'false', 2, 100004, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/forceValidationOnMdSave', 'false', 2, 100005, 'n');

UPDATE Settings SET value='3.0.5' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

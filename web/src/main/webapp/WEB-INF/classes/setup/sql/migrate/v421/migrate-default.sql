
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/publicationbyrevieweringroupowneronly', 'true', 2, 9181, 'n');

UPDATE Settings SET value='4.2.1' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

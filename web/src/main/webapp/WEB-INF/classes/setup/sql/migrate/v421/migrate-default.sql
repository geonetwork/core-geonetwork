
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadataprivs/publicationbyrevieweringroupowneronly', 'true', 2, 9181, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doipattern', '{{uuid}}', 0, 100197, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/delete/profilePublishedMetadata', 'Editor', 0, 12011, 'n');

-- cf. https://www.un.org/en/about-us/member-states/turkiye (run this manually if it applies to your catalogue)
-- UPDATE metadata SET data = replace(data, 'Turkey', 'Türkiye') WHERE data LIKE '%Turkey%';

UPDATE Settings SET value='4.2.1' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

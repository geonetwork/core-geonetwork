UPDATE Settings SET value='3.12.3' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

-- New setting for DOI public URL
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doipublicurl', '', 0, 100196, 'n');

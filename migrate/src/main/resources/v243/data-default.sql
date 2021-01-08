-- TODO if needed to migrate 2.4.2 and previous version

UPDATE Settings SET value='2.4.3' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';
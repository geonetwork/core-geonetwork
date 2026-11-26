ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS target varchar(255);
ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS windowWidth integer;
ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS windowHeight integer;

UPDATE Settings SET value='4.4.14' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

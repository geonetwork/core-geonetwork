UPDATE Settings SET value='4.4.3' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


ALTER TABLE spg_page ADD COLUMN access_expression VARCHAR(2048);

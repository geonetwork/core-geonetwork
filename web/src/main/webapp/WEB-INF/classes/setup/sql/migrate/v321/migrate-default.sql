UPDATE Settings SET value='3.2.1' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

ALTER TABLE Settings ADD COLUMN encrypted VARCHAR(1) DEFAULT 'n';

UPDATE Settings SET encrypted='y' WHERE name='system/proxy/password';
UPDATE Settings SET encrypted='y' WHERE name='system/feedback/mailServer/password';
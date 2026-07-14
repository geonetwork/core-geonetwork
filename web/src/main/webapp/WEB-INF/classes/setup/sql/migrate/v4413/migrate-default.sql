ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS showOnNonApproved boolean DEFAULT true NOT NULL;
ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS showOnApproved boolean DEFAULT true NOT NULL;
ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS showWhenWorkflowDisabled boolean DEFAULT true NOT NULL;

UPDATE Settings SET value='4.4.13' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

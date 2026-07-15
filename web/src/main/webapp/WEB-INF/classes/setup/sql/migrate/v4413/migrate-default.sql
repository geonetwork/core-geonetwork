UPDATE Settings SET value='4.4.13' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- Move the force validation on save setting out of the workflow section so it
-- can also be used when the metadata approval workflow is disabled.
UPDATE Settings SET name='metadata/save/forceValidationOnMdSave', position = 12005 WHERE name='metadata/workflow/forceValidationOnMdSave';

-- Move the allow publication of invalid metadata setting out of the workflow
-- section as it is a publication concern, not linked to the approval workflow.
UPDATE Settings SET name='metadata/publication/allowPublishInvalidMd', position = 12023 WHERE name='metadata/workflow/allowPublishInvalidMd';

-- Move the automatic unpublication of invalid metadata setting out of the
-- workflow section as it is a publication concern, not linked to the approval workflow.
UPDATE Settings SET name='metadata/publication/automaticUnpublishInvalidMd', position = 12024 WHERE name='metadata/workflow/automaticUnpublishInvalidMd';

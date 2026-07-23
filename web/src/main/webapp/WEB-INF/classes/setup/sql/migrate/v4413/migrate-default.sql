ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS showOnNonApproved boolean DEFAULT true NOT NULL;
ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS showOnApproved boolean DEFAULT true NOT NULL;
ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS showWhenWorkflowDisabled boolean DEFAULT true NOT NULL;

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

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/publication/enableScheduledPublication', 'false', 2, 12023, 'n');

INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (101,'scheduledPublicationTask','n', 101, 'task', 'statusUserOwner');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'ara','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'arm','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'aze','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'cat','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'chi','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'dan','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'dut','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'eng','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'fin','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'fre','Publication programmée');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'geo','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'ger','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'ita','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'nor','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'pol','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'por','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'rum','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'rus','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'slo','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'spa','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'swe','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'tur','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'ukr','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'vie','Scheduled publication');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (101,'wel','Scheduled publication');

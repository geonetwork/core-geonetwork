INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadataprivs/publication/managepublicationdate', 'false', 2, 9182, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadataprivs/publication/managepublicationdate');
UPDATE Settings SET position=9183 WHERE name='system/metadataprivs/publication/notificationLevel';
UPDATE Settings SET position=9184 WHERE name='system/metadataprivs/publication/notificationGroups';

ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS showOnNonApproved boolean DEFAULT true NOT NULL;
ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS showOnApproved boolean DEFAULT true NOT NULL;
ALTER TABLE spg_page ADD COLUMN IF NOT EXISTS showWhenWorkflowDisabled boolean DEFAULT true NOT NULL;

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

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadata/recordCreationDateFromXml', 'false', 2, 9190, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadata/recordCreationDateFromXml');

-- Rename the default saved selection to favorite records
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'ara';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'arm';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'aze';
UPDATE SelectionsDes SET label = 'Registres preferits' WHERE iddes = 0 AND langid = 'cat';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'chi';
UPDATE SelectionsDes SET label = 'Favoritposter' WHERE iddes = 0 AND langid = 'dan';
UPDATE SelectionsDes SET label = 'Favoriete records' WHERE iddes = 0 AND langid = 'dut';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'eng';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'fin';
UPDATE SelectionsDes SET label = 'Fiches favorites' WHERE iddes = 0 AND langid = 'fre';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'geo';
UPDATE SelectionsDes SET label = 'Favorisierte Datensätze' WHERE iddes = 0 AND langid = 'ger';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'ita';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'nor';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'pol';
UPDATE SelectionsDes SET label = 'Registros favoritos' WHERE iddes = 0 AND langid = 'por';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'rum';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'rus';
UPDATE SelectionsDes SET label = 'Favorite records [SK]' WHERE iddes = 0 AND langid = 'slo';
UPDATE SelectionsDes SET label = 'Registros favoritos' WHERE iddes = 0 AND langid = 'spa';
UPDATE SelectionsDes SET label = 'Favoritposter' WHERE iddes = 0 AND langid = 'swe';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'tur';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'ukr';
UPDATE SelectionsDes SET label = 'Favorite records' WHERE iddes = 0 AND langid = 'vie';
UPDATE SelectionsDes SET label = 'Hoff gofnodion' WHERE iddes = 0 AND langid = 'wel';

UPDATE Settings SET value='4.4.13' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

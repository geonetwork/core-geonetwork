-- MyOcean
UPDATE metadata SET schemaid = 'iso19139' WHERE schemaid = 'iso19139.myocean';
UPDATE metadata SET schemaid = 'iso19139' WHERE schemaid = 'iso19139.myocean.short';



INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doienabled', 'false', 2, 191, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doiurl', '', 0, 192, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doiusername', '', 0, 193, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doipassword', '', 0, 194, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doikey', '', 0, 195, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doilandingpagetemplate', 'http://localhost:8080/geonetwork/srv/resources/records/{{uuid}}', 0, 195, 'n');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/history/enabled', 'false', 2, 9171, 'n');



ALTER TABLE StatusValues ADD type varchar(255);
ALTER TABLE StatusValues ADD notificationLevel varchar(255);


UPDATE StatusValues SET type = 'workflow';

UPDATE StatusValues SET notificationLevel = 'recordUserAuthor' WHERE name = 'approved';
UPDATE StatusValues SET notificationLevel = 'recordUserAuthor' WHERE name = 'retired';
UPDATE StatusValues SET notificationLevel = 'recordProfileReviewer' WHERE name = 'submitted';
UPDATE StatusValues SET notificationLevel = 'recordUserAuthor' WHERE name = 'rejected';


INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (100,'doiCreationTask','n', 100, 'task', 'statusUserOwner');


INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (50,'recordcreated','y', 50, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (51,'recordupdated','y', 51, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (52,'attachmentadded','y', 52, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (53,'attachmentdeleted','y', 53, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (54,'recordownerchange','y', 54, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (55,'recordgroupownerchange','y', 55, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (56,'recordprivilegeschange','y', 56, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (57,'recordcategorychange','y', 57, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (58,'recordvalidationtriggered','y', 58, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (59,'recordstatuschange','y', 59, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (60,'recordprocessingchange','y', 60, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (61,'recorddeleted','y', 61, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (62,'recordimported','y', 62, 'event', null);



INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (50,'eng','Record created.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (51,'eng','Record updated.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (52,'eng','Attachment {{h.currentStatus}} added.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (53,'eng','Attachment {{h.previousStatus}} deleted.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (54,'eng','Owner changed from {{h.previousStatus}} to {{h.currentStatus}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (55,'eng','Group owner changed from {{h.previousStatus}} to {{h.currentStatus}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (56,'eng','Privileges updated.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (57,'eng','Category changed. Now categories are {{h.currentStatus}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (58,'eng','Validation triggered. Exit status is now {{h.currentStatus}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (59,'eng','Status changed from {{h.previousStatus}} to {{h.currentStatus}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (60,'eng','Record updated by process {{h.currentStatus}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (61,'eng','Record deleted.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (62,'eng','Record imported.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'eng','DOI creation request');

INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (50,'fre','Fiche créée.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (51,'fre','Fiche mise à jour.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (52,'fre','Document {{h.item1}} ajouté.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (53,'fre','Document {{h.item1}} supprimé.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (54,'fre','Auteur {{h.item1}} remplacé par {{h.item2}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (55,'fre','Groupe {{h.item1}} remplacé par {{h.item2}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (56,'fre','Accès mis à jour.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (57,'fre','Changement de catégorie. Les catégories sont {{h.item1}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (58,'fre','Fiche validée. La validation est maintenant {{h.item1}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (59,'fre','Changement de status de {{h.item1}} à {{h.item2}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (60,'fre','Fiche mise à jour par le processus {{h.item1}}.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (61,'fre','Fiche supprimée.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (62,'fre','Fiche importée.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'fre','Demande de création de DOI');


DELETE FROM schematroncriteria
 WHERE concat(group_name, group_schematronid) IN (
   SElECT concat(name, schematronid) FROM schematroncriteriagroup sg
     WHERE schematronid IN (
       SElECT id FROM Schematron WHERE filename = 'schematron-rules-inspire.disabled.xsl'));

DELETE FROM schematroncriteriagroup
 WHERE schematronid IN (SElECT id FROM Schematron WHERE filename = 'schematron-rules-inspire.disabled.xsl');

DELETE FROM schematrondes
 WHERE iddes IN (SElECT id FROM Schematron WHERE filename = 'schematron-rules-inspire.disabled.xsl');

DELETE FROM Schematron WHERE filename = 'schematron-rules-inspire.disabled.xsl';



DELETE FROM schematroncriteria
 WHERE concat(group_name, group_schematronid) IN (
   SElECT concat(name, schematronid) FROM schematroncriteriagroup sg
     WHERE schematronid IN (
       SElECT id FROM Schematron WHERE schemaname IN ('iso19139.sextant', 'iso19139.emodnet.chemistry', 'iso19139.emodnet.hydrography', 'iso19139.myocean')));

DELETE FROM schematroncriteriagroup
 WHERE schematronid IN (SElECT id FROM Schematron WHERE schemaname IN ('iso19139.sextant', 'iso19139.emodnet.chemistry', 'iso19139.emodnet.hydrography', 'iso19139.myocean'));

DELETE FROM schematrondes
 WHERE iddes IN (SElECT id FROM Schematron WHERE schemaname IN ('iso19139.sextant', 'iso19139.emodnet.chemistry', 'iso19139.emodnet.hydrography', 'iso19139.myocean'));

DELETE FROM schematron
  WHERE schemaname IN ('iso19139.sextant', 'iso19139.emodnet.chemistry', 'iso19139.emodnet.hydrography', 'iso19139.myocean');


UPDATE Schematron SET filename = 'schematron-rules-url-check.xsl' WHERE filename = 'schematron-rules-url-check.report_only.xsl';
UPDATE Schematron SET filename = 'schematron-rules-inspire-sds.xsl' WHERE filename = 'schematron-rules-inspire-sds.disabled.xsl';
UPDATE Schematron SET filename = 'schematron-rules-inspire-strict.xsl' WHERE filename = 'schematron-rules-inspire-strict.disabled.xsl';
UPDATE Schematron SET filename = 'schematron-rules-inspire.xsl' WHERE filename = 'schematron-rules-inspire-disabled.xsl';



INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/validation/removeSchemaLocation', 'false', 2, 9170, 'n');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/vcs/enable', 'false', 2, 9161, 'n');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/import/restrict', '', 0, 11000, 'y');


UPDATE Settings SET value='3.5.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- TODO setting ui/config is it used or not in Sextant. Merge with current.

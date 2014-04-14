-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('fre','Français', 'y', 'n');

UPDATE CategoriesDes             SET langid='fre' WHERE langid='fr';
UPDATE IsoLanguagesDes           SET langid='fre' WHERE langid='fr';
UPDATE RegionsDes                SET langid='fre' WHERE langid='fr';
UPDATE GroupsDes                 SET langid='fre' WHERE langid='fr';
UPDATE OperationsDes             SET langid='fre' WHERE langid='fr';
UPDATE StatusValuesDes           SET langid='fre' WHERE langid='fr';
UPDATE CswServerCapabilitiesInfo SET langid='fre' WHERE langid='fr';
DELETE FROM Languages WHERE id='fr';

-- Take care to table ID (related to other loc files)
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (11,'fre','Serveurs Z3950');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (12,'fre','Annuaires');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (13,'fre','Echantillons physiques');

INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'fre','Inconnu');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'fre','Brouillon');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'fre','Validé');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'fre','Retiré');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'fre','A valider');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'fre','Rejeté');


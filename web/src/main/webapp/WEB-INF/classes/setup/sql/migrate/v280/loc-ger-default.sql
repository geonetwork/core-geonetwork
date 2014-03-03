-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('ger','Deutsch', 'y', 'n');

UPDATE CategoriesDes             SET langid='ger' WHERE langid='de';
UPDATE IsoLanguagesDes           SET langid='ger' WHERE langid='de';
UPDATE RegionsDes                SET langid='ger' WHERE langid='de';
UPDATE GroupsDes                 SET langid='ger' WHERE langid='de';
UPDATE OperationsDes             SET langid='ger' WHERE langid='de';
UPDATE StatusValuesDes           SET langid='ger' WHERE langid='de';
UPDATE CswServerCapabilitiesInfo SET langid='ger' WHERE langid='de';
DELETE FROM Languages WHERE id='de';

-- Take care to table ID (related to other loc files)
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (11,'ger','Z3950 Servers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (12,'ger','Registers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (13,'ger','Physischen Proben');

INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'ger','Unknown');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'ger','Draft');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'ger','Approved');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'ger','Retired');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'ger','Submitted');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'ger','Rejected');


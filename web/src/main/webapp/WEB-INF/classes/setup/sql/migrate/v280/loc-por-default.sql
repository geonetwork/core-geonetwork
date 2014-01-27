-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('por','Português', 'y', 'n');

UPDATE CategoriesDes             SET langid='por' WHERE langid='pt';
UPDATE IsoLanguagesDes           SET langid='por' WHERE langid='pt';
UPDATE RegionsDes                SET langid='por' WHERE langid='pt';
UPDATE GroupsDes                 SET langid='por' WHERE langid='pt';
UPDATE OperationsDes             SET langid='por' WHERE langid='pt';
UPDATE StatusValuesDes           SET langid='por' WHERE langid='pt';
UPDATE CswServerCapabilitiesInfo SET langid='por' WHERE langid='pt';
DELETE FROM Languages WHERE id='pt';

-- Take care to table ID (related to other loc files)
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (11,'por','Z3950 Servers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (12,'por','Registers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (13,'por','Amostras físicas');

INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'por','Unknown');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'por','Draft');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'por','Approved');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'por','Retired');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'por','Submitted');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'por','Rejected');


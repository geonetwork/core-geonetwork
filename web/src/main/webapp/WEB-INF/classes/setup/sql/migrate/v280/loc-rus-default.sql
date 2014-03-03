-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('rus','русский язык', 'n', 'n');

UPDATE CategoriesDes             SET langid='rus' WHERE langid='ru';
UPDATE IsoLanguagesDes           SET langid='rus' WHERE langid='ru';
UPDATE RegionsDes                SET langid='rus' WHERE langid='ru';
UPDATE GroupsDes                 SET langid='rus' WHERE langid='ru';
UPDATE OperationsDes             SET langid='rus' WHERE langid='ru';
UPDATE StatusValuesDes           SET langid='rus' WHERE langid='ru';
UPDATE CswServerCapabilitiesInfo SET langid='rus' WHERE langid='ru';
DELETE FROM Languages WHERE id='ru';

-- Take care to table ID (related to other loc files)
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (11,'rus','Z3950 Servers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (12,'rus','Registers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (13,'rus','Физические образцы');

INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'rus','Unknown');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'rus','Draft');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'rus','Approved');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'rus','Retired');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'rus','Submitted');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'rus','Rejected');

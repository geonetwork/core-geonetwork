-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('eng','English', 'y', 'y');

UPDATE CategoriesDes             SET langid='eng' WHERE langid='en';
UPDATE IsoLanguagesDes           SET langid='eng' WHERE langid='en';
UPDATE RegionsDes                SET langid='eng' WHERE langid='en';
UPDATE GroupsDes                 SET langid='eng' WHERE langid='en';
UPDATE OperationsDes             SET langid='eng' WHERE langid='en';
UPDATE StatusValuesDes           SET langid='eng' WHERE langid='en';
UPDATE CswServerCapabilitiesInfo SET langid='eng' WHERE langid='en';
DELETE FROM Languages WHERE id='en';

INSERT INTO CategoriesDes (iddes, langid, label) VALUES (11,'eng','Z3950 Servers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (12,'eng','Registers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (13,'eng','Physical Samples');


INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'eng','Unknown');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'eng','Draft');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'eng','Approved');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'eng','Retired');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'eng','Submitted');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'eng','Rejected');

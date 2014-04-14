-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('tur','Turkish', 'n', 'n');

UPDATE CategoriesDes             SET langid='tur' WHERE langid='tr';
UPDATE IsoLanguagesDes           SET langid='tur' WHERE langid='tr';
UPDATE RegionsDes                SET langid='tur' WHERE langid='tr';
UPDATE GroupsDes                 SET langid='tur' WHERE langid='tr';
UPDATE OperationsDes             SET langid='tur' WHERE langid='tr';
UPDATE StatusValuesDes           SET langid='tur' WHERE langid='tr';
UPDATE CswServerCapabilitiesInfo SET langid='tur' WHERE langid='tr';
DELETE FROM Languages WHERE id='tr';

-- Take care to table ID (related to other loc files)
INSERT INTO CswServerCapabilitiesInfo VALUES (57, 'tur', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (58, 'tur', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (59, 'tur', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (60, 'tur', 'accessConstraints', '');


INSERT INTO CategoriesDes (iddes, langid, label) VALUES (11,'tur','Z3950 Sunucular');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (12,'tur','Kayıtlar');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (13,'tur','Fiziksel Örnekleri');

INSERT INTO OperationsDes VALUES (0,'tur','Publish');
INSERT INTO OperationsDes VALUES (1,'tur','Download');
INSERT INTO OperationsDes VALUES (2,'tur','Editing');
INSERT INTO OperationsDes VALUES (3,'tur','Notify');
INSERT INTO OperationsDes VALUES (5,'tur','Interactive Map');
INSERT INTO OperationsDes VALUES (6,'tur','Featured');

INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'tur','Unknown');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'tur','Draft');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'tur','Approved');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'tur','Retired');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'tur','Submitted');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'tur','Rejected');

-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('cat','Català', 'n', 'n');

UPDATE CategoriesDes             SET langid='cat' WHERE langid='ca';
UPDATE IsoLanguagesDes           SET langid='cat' WHERE langid='ca';
UPDATE RegionsDes                SET langid='cat' WHERE langid='ca';
UPDATE GroupsDes                 SET langid='cat' WHERE langid='ca';
UPDATE OperationsDes             SET langid='cat' WHERE langid='ca';
UPDATE StatusValuesDes           SET langid='cat' WHERE langid='ca';
UPDATE CswServerCapabilitiesInfo SET langid='cat' WHERE langid='ca';
DELETE FROM Languages WHERE id='ca';

-- Take care to table ID (related to other loc files)
INSERT INTO CswServerCapabilitiesInfo VALUES (53, 'cat', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (54, 'cat', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (55, 'cat', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (56, 'cat', 'accessConstraints', '');

INSERT INTO CategoriesDes (iddes, langid, label) VALUES (11,'cat','Z3950 Servers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (12,'cat','Registers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (13,'cat','Physical Samples');

INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'cat','Unknown');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'cat','Draft');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'cat','Approved');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'cat','Retired');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'cat','Submitted');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'cat','Rejected');


-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('chi','??', 'n', 'n');

UPDATE CategoriesDes             SET langid='chi' WHERE langid='cn';
UPDATE IsoLanguagesDes           SET langid='chi' WHERE langid='cn';
UPDATE RegionsDes                SET langid='chi' WHERE langid='cn';
UPDATE GroupsDes                 SET langid='chi' WHERE langid='cn';
UPDATE OperationsDes             SET langid='chi' WHERE langid='cn';
UPDATE StatusValuesDes           SET langid='chi' WHERE langid='cn';
UPDATE CswServerCapabilitiesInfo SET langid='chi' WHERE langid='cn';
DELETE FROM Languages WHERE id='cn';

-- Take care to table ID (related to other loc files)
INSERT INTO CategoriesDes VALUES (11,'chi','Z3950 Servers');
INSERT INTO CategoriesDes VALUES (12,'chi','Registers');
INSERT INTO CategoriesDes VALUES (13,'chi','Physical Samples');

INSERT INTO StatusValuesDes VALUES (0,'chi','Unknown');
INSERT INTO StatusValuesDes VALUES (1,'chi','Draft');
INSERT INTO StatusValuesDes VALUES (2,'chi','Approved');
INSERT INTO StatusValuesDes VALUES (3,'chi','Retired');
INSERT INTO StatusValuesDes VALUES (4,'chi','Submitted');
INSERT INTO StatusValuesDes VALUES (5,'chi','Rejected');


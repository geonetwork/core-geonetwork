-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('spa','Español', 'y', 'n');

UPDATE CategoriesDes             SET langid='spa' WHERE langid='es';
UPDATE IsoLanguagesDes           SET langid='spa' WHERE langid='es';
UPDATE RegionsDes                SET langid='spa' WHERE langid='es';
UPDATE GroupsDes                 SET langid='spa' WHERE langid='es';
UPDATE OperationsDes             SET langid='spa' WHERE langid='es';
UPDATE StatusValuesDes           SET langid='spa' WHERE langid='es';
UPDATE CswServerCapabilitiesInfo SET langid='spa' WHERE langid='es';
DELETE FROM Languages WHERE id='es';

-- Take care to table ID (related to other loc files)
INSERT INTO CategoriesDes VALUES (11,'spa','Z3950 Servers');
INSERT INTO CategoriesDes VALUES (12,'spa','Registers');
INSERT INTO CategoriesDes VALUES (13,'spa','Muestras físicas');

INSERT INTO StatusValuesDes VALUES (0,'spa','Unknown');
INSERT INTO StatusValuesDes VALUES (1,'spa','Draft');
INSERT INTO StatusValuesDes VALUES (2,'spa','Approved');
INSERT INTO StatusValuesDes VALUES (3,'spa','Retired');
INSERT INTO StatusValuesDes VALUES (4,'spa','Submitted');
INSERT INTO StatusValuesDes VALUES (5,'spa','Rejected');

-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('dut','Nederlands', 'y', 'n');

UPDATE CategoriesDes             SET langid='dut' WHERE langid='nl';
UPDATE IsoLanguagesDes           SET langid='dut' WHERE langid='nl';
UPDATE RegionsDes                SET langid='dut' WHERE langid='nl';
UPDATE GroupsDes                 SET langid='dut' WHERE langid='nl';
UPDATE OperationsDes             SET langid='dut' WHERE langid='nl';
UPDATE StatusValuesDes           SET langid='dut' WHERE langid='nl';
UPDATE CswServerCapabilitiesInfo SET langid='dut' WHERE langid='nl';
DELETE FROM Languages WHERE id='nl';

-- Take care to table ID (related to other loc files)
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (11,'dut','Z3950 Servers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (12,'dut','Registers');
INSERT INTO CategoriesDes (iddes, langid, label) VALUES (13,'dut','Physical Samples');

INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (0,'dut','Unknown');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (1,'dut','Draft');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (2,'dut','Approved');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (3,'dut','Retired');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (4,'dut','Submitted');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (5,'dut','Rejected');
						

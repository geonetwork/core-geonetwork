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
INSERT INTO CategoriesDes VALUES (11,'dut','Z3950 Servers');
INSERT INTO CategoriesDes VALUES (12,'dut','Registers');
INSERT INTO CategoriesDes VALUES (13,'dut','Physical Samples');

INSERT INTO StatusValuesDes VALUES (0,'dut','Unknown');
INSERT INTO StatusValuesDes VALUES (1,'dut','Draft');
INSERT INTO StatusValuesDes VALUES (2,'dut','Approved');
INSERT INTO StatusValuesDes VALUES (3,'dut','Retired');
INSERT INTO StatusValuesDes VALUES (4,'dut','Submitted');
INSERT INTO StatusValuesDes VALUES (5,'dut','Rejected');
						

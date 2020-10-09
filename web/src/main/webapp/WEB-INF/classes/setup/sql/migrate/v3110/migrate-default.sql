
ALTER TABLE groupsdes ALTER COLUMN label TYPE varchar(255);
ALTER TABLE sourcesdes ALTER COLUMN label TYPE varchar(255);
ALTER TABLE schematrondes ALTER COLUMN label TYPE varchar(255);

UPDATE Settings SET value='3.11.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- Increase the length of Validation type (where the schematron file name is stored)
ALTER TABLE Validation ALTER COLUMN valType TYPE varchar(128);

-- New setting for server timezone
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/server/timeZone', '', 0, 260, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/users/identicon', 'gravatar:mp', 0, 9110, 'n');

ALTER TABLE usersearch ALTER COLUMN url TYPE text;

INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (63,'recordrestored','y', 63, 'event', null);
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'ara','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'cat','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'chi','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'dut','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'eng','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'fre','Fiche restauré.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'fin','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'ger','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'ita','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'nor','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'pol','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'por','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'rus','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'slo','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'spa','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'tur','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'vie','Record restored.');

-- keep these at the bottom of the file!
DROP INDEX idx_metadatafiledownloads_metadataid;
DROP INDEX idx_metadatafileuploads_metadataid;
DROP INDEX idx_operationallowed_metadataid;

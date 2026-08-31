
UPDATE Settings SET value='3.11.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- Increase the length of Validation type (where the schematron file name is stored)
ALTER TABLE Validation MODIFY valType varchar(128);

ALTER TABLE usersearch ADD (tempurl clob);
UPDATE usersearch SET tempurl = url, url = null;
ALTER TABLE usersearch DROP COLUMN url;
ALTER TABLE usersearch RENAME COLUMN tempurl to url;

INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (63,'recordrestored','y', 63, 'event', null);
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'ara','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'cat','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'chi','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'dut','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'eng','Record restored.');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (63,'fre','Fiche restaurée.');
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

DELETE FROM Settings WHERE name = 'system/server/securePort';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/passwordEnforcement/minLength', '6', 1, 12000, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/passwordEnforcement/maxLength', '20', 1, 12001, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/passwordEnforcement/usePattern', 'true', 2, 12002, 'n');
INSERT INTO Settings (name, value, datatype, position, internal, editable) VALUES ('system/security/passwordEnforcement/pattern', '^((?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*(_|[^\w])).*)$', 0, 12003, 'n', 'n');

UPDATE Settings SET encrypted='y' WHERE name='system/proxy/password';
UPDATE Settings SET encrypted='y' WHERE name='system/feedback/mailServer/password';
UPDATE Settings SET encrypted='y' WHERE name='system/publication/doi/doipassword';

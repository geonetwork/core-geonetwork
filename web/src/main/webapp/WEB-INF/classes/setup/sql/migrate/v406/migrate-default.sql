INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/nodeid', '', 0, 7212, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/apikey', '', 0, 7213, 'y');

DELETE FROM Settings WHERE name = 'system/server/securePort';

UPDATE Settings SET value='4.0.6' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- Add missing translation for StatusValue 100 - doiCreationTask
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'tur','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'ara','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'ger','DOI-Erstellungsanfrage');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'pol','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'rus','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'nor','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'swe','Begäran att skapa DOI');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'fre','Demande de création de DOI');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'fin','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'vie','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'eng','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'chi','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'dut','DOI aanvragen');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'ita','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'slo','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'cat','DOI creation request');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'spa','Solicitud de creación de DOI');
INSERT INTO StatusValuesDes  (iddes, langid, label) VALUES (100,'por','DOI creation request');
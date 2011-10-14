-- 2.6.4 changes
CREATE TABLE CustomElementSet
  (
    xpath  varchar(1000) not null
  );

UPDATE Languages SET isocode = 'spa' where id ='es';

ALTER TABLE Languages ADD isInspire char(1);
ALTER TABLE Languages ADD isDefault char(1);

UPDATE Languages SET isInspire = 'y', isDefault = 'y' where id ='en';
UPDATE Languages SET isInspire = 'y', isDefault = 'n' where id ='fr';
UPDATE Languages SET isInspire = 'y', isDefault = 'n' where id ='es';
UPDATE Languages SET isInspire = 'n', isDefault = 'n' where id ='ru';
UPDATE Languages SET isInspire = 'n', isDefault = 'n' where id ='cn';
UPDATE Languages SET isInspire = 'y', isDefault = 'n' where id ='de';
UPDATE Languages SET isInspire = 'y', isDefault = 'n' where id ='nl';
UPDATE Languages SET isInspire = 'y', isDefault = 'n' where id ='pt';
UPDATE Languages SET isInspire = 'n', isDefault = 'n' where id ='ca';
UPDATE Languages SET isInspire = 'n', isDefault = 'n' where id ='tr';

-- 2.6.5 changes

INSERT INTO Settings VALUES (23,20,'protocol','http');

INSERT INTO Settings VALUES (88,80,'defaultGroup', NULL);
INSERT INTO Settings VALUES (113,87,'group',NULL);
INSERT INTO Settings VALUES (178,173,'group',NULL);
INSERT INTO Settings VALUES (179,170,'defaultGroup', NULL);

INSERT INTO Languages VALUES ('fi','Finnish', 'fin', 'y', 'n');

INSERT INTO CswServerCapabilitiesInfo VALUES (41, 'fi', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (42, 'fi', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (43, 'fi', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (44, 'fi', 'accessConstraints', '');

INSERT INTO CategoriesDes VALUES (1,'fi','Kartat & kuvat');
INSERT INTO CategoriesDes VALUES (2,'fi','Tietoaineistot');
INSERT INTO CategoriesDes VALUES (3,'fi','Vuorovaikutteiset resurssit');
INSERT INTO CategoriesDes VALUES (4,'fi','Sovellukset');
INSERT INTO CategoriesDes VALUES (5,'fi','Esimerkkitapaukset, parhaat käytännöt');
INSERT INTO CategoriesDes VALUES (6,'fi','Konferenssijulkaisut');
INSERT INTO CategoriesDes VALUES (7,'fi','Valokuvat');
INSERT INTO CategoriesDes VALUES (8,'fi','Äänitteet / Videot');
INSERT INTO CategoriesDes VALUES (9,'fi','Hakemistot');
INSERT INTO CategoriesDes VALUES (10,'fi','Other information resources');

INSERT INTO GroupsDes VALUES (-1,'fi','Vierailija');
INSERT INTO GroupsDes VALUES (0,'fi','Intranet');
INSERT INTO GroupsDes VALUES (1,'fi','Kaikki');
INSERT INTO GroupsDes VALUES (2,'fi','Ryhmä');

INSERT INTO Settings VALUES (722,720,'enableSearchPanel','false');

INSERT INTO Settings VALUES (910,1,'metadata',NULL);
INSERT INTO Settings VALUES (911,910,'enableSimpleView','true');
INSERT INTO Settings VALUES (912,910,'enableIsoView','true');
INSERT INTO Settings VALUES (913,910,'enableInspireView','false');
INSERT INTO Settings VALUES (914,910,'enableXmlView','true');
INSERT INTO Settings VALUES (915,910,'defaultView','simple');

UPDATE Settings SET value='2.6.5' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';
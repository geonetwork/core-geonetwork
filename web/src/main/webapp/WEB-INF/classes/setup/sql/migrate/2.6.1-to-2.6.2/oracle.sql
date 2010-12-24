CREATE TABLE CswServerCapabilitiesInfo
  (
    idField   int,
    langId    varchar(5)    not null,
    field     varchar(32)   not null,
    label     varchar(96),

    primary key(idField)
  );

ALTER TABLE CswServerCapabilitiesInfo ADD FOREIGN KEY (langId) REFERENCES Languages (id);

CREATE TABLE IndexLanguages
  (
    id            int,
    languageName  varchar(32)   not null,
    selected      char(1)       default 'n' not null,

    primary key(id, languageName)

  );

ALTER TABLE Languages ADD isocode varchar(3);

UPDATE Languages SET isocode = 'eng' where id ='en';
UPDATE Languages SET isocode = 'fre' where id ='fr';
UPDATE Languages SET isocode = 'esp' where id ='es';
UPDATE Languages SET isocode = 'rus' where id ='ru';
UPDATE Languages SET isocode = 'chi' where id ='cn';
UPDATE Languages SET isocode = 'ger' where id ='de';
UPDATE Languages SET isocode = 'dut' where id ='nl';
UPDATE Languages SET isocode = 'por' where id ='pt';

INSERT INTO CswServerCapabilitiesInfo VALUES (1, 'en', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (2, 'en', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (3, 'en', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (4, 'en', 'accessConstraints', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (5, 'es', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (6, 'es', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (7, 'es', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (8, 'es', 'accessConstraints', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (9, 'nl', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (10, 'nl', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (11, 'nl', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (12, 'nl', 'accessConstraints', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (13, 'cn', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (14, 'cn', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (15, 'cn', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (16, 'cn', 'accessConstraints', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (17, 'de', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (18, 'de', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (19, 'de', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (20, 'de', 'accessConstraints', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (21, 'fr', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (22, 'fr', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (23, 'fr', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (24, 'fr', 'accessConstraints', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (25, 'pt', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (26, 'pt', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (27, 'pt', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (28, 'pt', 'accessConstraints', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (29, 'ru', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (30, 'ru', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (31, 'ru', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (32, 'ru', 'accessConstraints', '');

INSERT INTO IndexLanguages VALUES (1, 'danish', 'n');
INSERT INTO IndexLanguages VALUES (2, 'dutch', 'n');
INSERT INTO IndexLanguages VALUES (3, 'english', 'y');
INSERT INTO IndexLanguages VALUES (4, 'finnish', 'n');
INSERT INTO IndexLanguages VALUES (5, 'french', 'n');
INSERT INTO IndexLanguages VALUES (6, 'german', 'n');
INSERT INTO IndexLanguages VALUES (7, 'hungarian', 'n');
INSERT INTO IndexLanguages VALUES (8, 'italian', 'n');
INSERT INTO IndexLanguages VALUES (9, 'norwegian', 'n');
INSERT INTO IndexLanguages VALUES (10, 'portuguese', 'n');
INSERT INTO IndexLanguages VALUES (11, 'russian', 'n');
INSERT INTO IndexLanguages VALUES (12, 'spanish', 'n');
INSERT INTO IndexLanguages VALUES (13, 'swedish', 'n');

UPDATE Settings SET value='2.6.2' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';

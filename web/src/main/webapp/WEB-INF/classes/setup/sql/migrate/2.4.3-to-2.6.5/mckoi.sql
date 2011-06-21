CREATE TABLE MetadataNotifiers
  (
    id         int,
    name       varchar(32)    not null,
    url        varchar(255)   not null,
    enabled    char(1)        default 'n' not null,
    username       varchar(32),
    password       varchar(32),

    primary key(id)
  );

-- ======================================================================

CREATE TABLE MetadataNotifications
  (
    metadataId         int,
    notifierId         int,
    notified           char(1)        default 'n' not null,
    metadataUuid       varchar(250)   not null,
    action             char(1)        not null,
    errormsg           text,

    primary key(metadataId,notifierId),

    foreign key(notifierId) references MetadataNotifiers(id)
  );

-- ALTER TABLE Settings ALTER COLUMN value longvarchar;
-- Mckoi only
ALTER CREATE TABLE Settings
	(  id        int,
    parentId  int,
    name      varchar(32)    not null,
    value     longvarchar);
    
ALTER TABLE Metadata ADD displayorder int;

INSERT INTO Settings VALUES (85,80,'uidAttr','uid');
INSERT INTO Settings VALUES (90,1,'selectionmanager',NULL);
INSERT INTO Settings VALUES (91,90,'maxrecords','1000');
INSERT INTO Settings VALUES (210,1,'localrating',NULL);
INSERT INTO Settings VALUES (211,210,'enable','false');
INSERT INTO Settings VALUES (220,1,'downloadservice',NULL);
INSERT INTO Settings VALUES (221,220,'leave','false');
INSERT INTO Settings VALUES (222,220,'simple','true');
INSERT INTO Settings VALUES (223,220,'withdisclaimer','false');
INSERT INTO Settings VALUES (230,1,'xlinkResolver',NULL);
INSERT INTO Settings VALUES (231,230,'enable','false');
INSERT INTO Settings VALUES (240,1,'autofixing',NULL);
INSERT INTO Settings VALUES (241,240,'enable','true');
INSERT INTO Settings VALUES (600,1,'indexoptimizer',NULL);
INSERT INTO Settings VALUES (601,600,'enable','true');
INSERT INTO Settings VALUES (602,600,'at',NULL);
INSERT INTO Settings VALUES (603,602,'hour','0');
INSERT INTO Settings VALUES (604,602,'min','0');
INSERT INTO Settings VALUES (605,602,'sec','0');
INSERT INTO Settings VALUES (606,600,'interval',NULL);
INSERT INTO Settings VALUES (607,606,'day','0');
INSERT INTO Settings VALUES (608,606,'hour','24');
INSERT INTO Settings VALUES (609,606,'min','0');
INSERT INTO Settings VALUES (700,1,'oai',NULL);
INSERT INTO Settings VALUES (701,700,'mdmode',1);
INSERT INTO Settings VALUES (702,700,'tokentimeout',3600);
INSERT INTO Settings VALUES (703,700,'cachesize',60);
INSERT INTO Settings VALUES (720,1,'inspire',NULL);
INSERT INTO Settings VALUES (721,720,'enable','false');

-- 2.6.2 changes

CREATE TABLE CswServerCapabilitiesInfo
  (
    idField   int,
    langId    varchar(5)    not null,
    field     varchar(32)   not null,
    label     varchar(96),

    primary key(idField),

    foreign key(langId) references Languages(id)
  );

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
UPDATE Languages SET isocode = 'spa' where id ='es';
UPDATE Languages SET isocode = 'rus' where id ='ru';
UPDATE Languages SET isocode = 'chi' where id ='cn';
UPDATE Languages SET isocode = 'ger' where id ='de';
UPDATE Languages SET isocode = 'dut' where id ='nl';
UPDATE Languages SET isocode = 'por' where id ='pt';
UPDATE Languages SET isocode = 'cat' where id ='ca';
UPDATE Languages SET isocode = 'tur' where id ='tr';

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
INSERT INTO CswServerCapabilitiesInfo VALUES (33, 'ca', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (34, 'ca', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (35, 'ca', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (36, 'ca', 'accessConstraints', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (37, 'tr', 'title', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (38, 'tr', 'abstract', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (39, 'tr', 'fees', '');
INSERT INTO CswServerCapabilitiesInfo VALUES (40, 'tr', 'accessConstraints', '');

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
INSERT INTO IndexLanguages VALUES (14, 'catalan', 'n');
INSERT INTO IndexLanguages VALUES (15, 'turkish', 'n');

-- 2.6.4 changes
CREATE TABLE CustomElementSet
  (
    xpath  varchar(1000) not null
  );

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

UPDATE Settings SET value='2.6.5' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';

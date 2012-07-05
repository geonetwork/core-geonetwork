--
-- DB migration script for Sextant
--	Source : Sextant dump from 2012/04
-- 
-- Author: François Prunayre
-- Date: 2012/06/10

-- Load dump
-- geonetwork_03_04_2012.backup

-- TODO : take care of table owner
-- TODO - Complete configuration: 
--  * From system administration > CSW configuration to set Sextant CSW capabilities info (contact, title, abstract, ...)
--  * INSPIRE checks in the metadata records (eg. LanguageCode, fre/fra)
--  * Add INSPIRE CSW capabilities configuration in overrides 
--  * Ifremer ISO profiles

-- !!! WARNING !!!: Remove harvesting configuration (ie ID >= 722 in Settings table) 
-- before update. After migration, start the catalog and add the harvesting configuration
-- back.
-- DELETE FROM SETTINGS WHERE ID >= 722


--SET search_path = geonetwork;


-- Sextant specific
--  * For the time being, move all records to iso19139 schema (no Ifremer profiles support)
UPDATE metadata SET schemaid = 'iso19139' WHERE schemaid like 'iso19139.%';
--  * Drop unused columns
ALTER TABLE groups DROP COLUMN extent;
ALTER TABLE groups DROP COLUMN public_group;


-- TODO : to remove
--  * Add a GeoNetwork default admin/admin account
INSERT INTO Users VALUES  (2,'admin','d033e22ae348aeb566fc214aec3585c4da997','admin','admin','Administrator','','','','','','','','');



-- 2.6.x series
INSERT INTO Settings VALUES (85,80,'uidAttr','uid');


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

CREATE TABLE CswServerCapabilitiesInfo
  (
    idField   int,
    langId    varchar(5)    not null,
    field     varchar(32)   not null,
    label     varchar(96),

    primary key(idField),

    foreign key(langId) references Languages(id)
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


INSERT INTO Settings VALUES (240,1,'autofixing',NULL);
INSERT INTO Settings VALUES (241,240,'enable','true');



ALTER TABLE CswServerCapabilitiesInfo ALTER COLUMN label TYPE text;

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


INSERT INTO Settings VALUES (23,20,'protocol','http');

INSERT INTO Settings VALUES (88,80,'defaultGroup', NULL);
INSERT INTO Settings VALUES (113,87,'group',NULL);
INSERT INTO Settings VALUES (178,173,'group',NULL);
INSERT INTO Settings VALUES (179,170,'defaultGroup', NULL);

-- 2.7.x series

CREATE TABLE HarvestHistory
  (
    id             int not null,
    harvestDate    varchar(30),
        harvesterUuid  varchar(250),
        harvesterName  varchar(128),
        harvesterType  varchar(128),
    deleted        char(1) default 'n' not null,
    info           text,
    params         text,

    primary key(id)

  );

CREATE INDEX HarvestHistoryNDX1 ON HarvestHistory(harvestDate);

CREATE TABLE StatusValues
  (
    id        int not null,
    name      varchar(32)   not null,
    reserved  char(1)       default 'n' not null,
    primary key(id)
  );


CREATE TABLE StatusValuesDes
  (
    idDes   int not null,
    langId  varchar(5) not null,
    label   varchar(96)   not null,
    primary key(idDes,langId)
  );


CREATE TABLE MetadataStatus
  (
    metadataId  int not null,
    statusId    int default 0 not null,
    userId      int not null,
    changeDate   varchar(30)    not null,
    changeMessage   varchar(2048) not null,
    primary key(metadataId,statusId,userId,changeDate),
    foreign key(metadataId) references Metadata(id),
    foreign key(statusId)   references StatusValues(id),
    foreign key(userId)     references Users(id)
  );
CREATE INDEX MetadataNDX3 ON Metadata(owner);


CREATE TABLE Validation
  (
    metadataId   int,
    valType      varchar(40),
    status       int,
    tested       int,
    failed       int,
    valDate      varchar(30),
    
    primary key(metadataId, valType),
    foreign key(metadataId) references Metadata(id)
);

CREATE TABLE Thesaurus (
    id   varchar(250) not null,
    activated    varchar(1),
    primary key(id)
  );

ALTER TABLE Users ALTER COLUMN username TYPE varchar(256);

ALTER TABLE Metadata ALTER COLUMN createDate TYPE varchar(30);
ALTER TABLE Metadata ALTER COLUMN changeDate TYPE varchar(30);
ALTER TABLE Metadata ADD doctype varchar(255);


INSERT INTO Settings VALUES (250,1,'searchStats',NULL);
INSERT INTO Settings VALUES (251,250,'enable','false');

INSERT INTO Settings VALUES (900,1,'harvester',NULL);
INSERT INTO Settings VALUES (901,900,'enableEditing','false');

INSERT INTO Settings VALUES (722,720,'enableSearchPanel','false');


INSERT INTO Settings VALUES (910,1,'metadata',NULL);
INSERT INTO Settings VALUES (911,910,'enableSimpleView','true');
INSERT INTO Settings VALUES (912,910,'enableIsoView','true');
INSERT INTO Settings VALUES (913,910,'enableInspireView','false');
INSERT INTO Settings VALUES (914,910,'enableXmlView','true');
INSERT INTO Settings VALUES (915,910,'defaultView','simple');

INSERT INTO Settings VALUES (917,1,'metadataprivs',NULL);
INSERT INTO Settings VALUES (918,917,'usergrouponly','false');

INSERT INTO Settings VALUES (920,1,'threadedindexing',NULL);
INSERT INTO Settings VALUES (921,920,'maxthreads','1');
INSERT INTO Settings VALUES (17,10,'svnUuid','');

-- add extra placeholders for shibboleth attributes

INSERT INTO Settings VALUES (180,173,'organizationName',NULL);
INSERT INTO Settings VALUES (181,173,'postalAddress',NULL);
INSERT INTO Settings VALUES (182,173,'phone',NULL);
INSERT INTO Settings VALUES (183,173,'email',NULL);
INSERT INTO Settings VALUES (184,173,'fullName',NULL);

-- add requestedlanguage and autodetect settings

INSERT INTO Settings VALUES (950,1,'autodetect',NULL);
INSERT INTO Settings VALUES (951,950,'enable','true');
INSERT INTO Settings VALUES (952,1,'requestedLanguage',NULL);
INSERT INTO Settings VALUES (953,952,'only','false');
INSERT INTO Settings VALUES (954,952,'sorted','true');
INSERT INTO Settings VALUES (955,952,'ignored','false');


-- ISO 3 letter code migration
INSERT INTO Languages VALUES ('ara','العربية', 'ara','n', 'n');
INSERT INTO Languages VALUES ('cat','Català', 'cat','n', 'n');
INSERT INTO Languages VALUES ('chi','中文', 'chi','n', 'n');
INSERT INTO Languages VALUES ('dut','Nederlands', 'dut','y', 'n');
INSERT INTO Languages VALUES ('eng','English', 'eng','y', 'y');
INSERT INTO Languages VALUES ('fin','Suomi', 'fin','y', 'n');
INSERT INTO Languages VALUES ('fre','Français', 'fre','y', 'n');
INSERT INTO Languages VALUES ('ger','Deutsch', 'ger','y', 'n');
INSERT INTO Languages VALUES ('nor','Norsk', 'nor','n', 'n');
INSERT INTO Languages VALUES ('por','Português', 'por','y', 'n');
INSERT INTO Languages VALUES ('rus','русский язык', 'rus','n', 'n');
INSERT INTO Languages VALUES ('spa','Español', 'spa','y', 'n');
INSERT INTO Languages VALUES ('vie','Tiếng Việt', 'vie','n', 'n');

UPDATE CategoriesDes SET langid='ara' WHERE langid='ar';
UPDATE CategoriesDes SET langid='cat' WHERE langid='ca';
UPDATE CategoriesDes SET langid='chi' WHERE langid='cn';
UPDATE CategoriesDes SET langid='dut' WHERE langid='nl';
UPDATE CategoriesDes SET langid='eng' WHERE langid='en';
UPDATE CategoriesDes SET langid='fin' WHERE langid='fi';
UPDATE CategoriesDes SET langid='fre' WHERE langid='fr';
UPDATE CategoriesDes SET langid='ger' WHERE langid='de';
UPDATE CategoriesDes SET langid='nor' WHERE langid='no';
UPDATE CategoriesDes SET langid='por' WHERE langid='pt';
UPDATE CategoriesDes SET langid='rus' WHERE langid='ru';
UPDATE CategoriesDes SET langid='spa' WHERE langid='es';
UPDATE CategoriesDes SET langid='vie' WHERE langid='vi';

UPDATE IsoLanguagesDes SET langid='ara' WHERE langid='ar';
UPDATE IsoLanguagesDes SET langid='cat' WHERE langid='ca';
UPDATE IsoLanguagesDes SET langid='chi' WHERE langid='cn';
UPDATE IsoLanguagesDes SET langid='dut' WHERE langid='nl';
UPDATE IsoLanguagesDes SET langid='eng' WHERE langid='en';
UPDATE IsoLanguagesDes SET langid='fin' WHERE langid='fi';
UPDATE IsoLanguagesDes SET langid='fre' WHERE langid='fr';
UPDATE IsoLanguagesDes SET langid='ger' WHERE langid='de';
UPDATE IsoLanguagesDes SET langid='nor' WHERE langid='no';
UPDATE IsoLanguagesDes SET langid='por' WHERE langid='pt';
UPDATE IsoLanguagesDes SET langid='rus' WHERE langid='ru';
UPDATE IsoLanguagesDes SET langid='spa' WHERE langid='es';
UPDATE IsoLanguagesDes SET langid='vie' WHERE langid='vi';

UPDATE RegionsDes SET langid='ara' WHERE langid='ar';
UPDATE RegionsDes SET langid='cat' WHERE langid='ca';
UPDATE RegionsDes SET langid='chi' WHERE langid='cn';
UPDATE RegionsDes SET langid='dut' WHERE langid='nl';
UPDATE RegionsDes SET langid='eng' WHERE langid='en';
UPDATE RegionsDes SET langid='fin' WHERE langid='fi';
UPDATE RegionsDes SET langid='fre' WHERE langid='fr';
UPDATE RegionsDes SET langid='ger' WHERE langid='de';
UPDATE RegionsDes SET langid='nor' WHERE langid='no';
UPDATE RegionsDes SET langid='por' WHERE langid='pt';
UPDATE RegionsDes SET langid='rus' WHERE langid='ru';
UPDATE RegionsDes SET langid='spa' WHERE langid='es';
UPDATE RegionsDes SET langid='vie' WHERE langid='vi';


UPDATE GroupsDes SET langid='ara' WHERE langid='ar';
UPDATE GroupsDes SET langid='cat' WHERE langid='ca';
UPDATE GroupsDes SET langid='chi' WHERE langid='cn';
UPDATE GroupsDes SET langid='dut' WHERE langid='nl';
UPDATE GroupsDes SET langid='eng' WHERE langid='en';
UPDATE GroupsDes SET langid='fin' WHERE langid='fi';
UPDATE GroupsDes SET langid='fre' WHERE langid='fr';
UPDATE GroupsDes SET langid='ger' WHERE langid='de';
UPDATE GroupsDes SET langid='nor' WHERE langid='no';
UPDATE GroupsDes SET langid='por' WHERE langid='pt';
UPDATE GroupsDes SET langid='rus' WHERE langid='ru';
UPDATE GroupsDes SET langid='spa' WHERE langid='es';
UPDATE GroupsDes SET langid='vie' WHERE langid='vi';


UPDATE OperationsDes SET langid='ara' WHERE langid='ar';
UPDATE OperationsDes SET langid='cat' WHERE langid='ca';
UPDATE OperationsDes SET langid='chi' WHERE langid='cn';
UPDATE OperationsDes SET langid='dut' WHERE langid='nl';
UPDATE OperationsDes SET langid='eng' WHERE langid='en';
UPDATE OperationsDes SET langid='fin' WHERE langid='fi';
UPDATE OperationsDes SET langid='fre' WHERE langid='fr';
UPDATE OperationsDes SET langid='ger' WHERE langid='de';
UPDATE OperationsDes SET langid='nor' WHERE langid='no';
UPDATE OperationsDes SET langid='por' WHERE langid='pt';
UPDATE OperationsDes SET langid='rus' WHERE langid='ru';
UPDATE OperationsDes SET langid='spa' WHERE langid='es';
UPDATE OperationsDes SET langid='vie' WHERE langid='vi';


UPDATE StatusValuesDes SET langid='ara' WHERE langid='ar';
UPDATE StatusValuesDes SET langid='cat' WHERE langid='ca';
UPDATE StatusValuesDes SET langid='chi' WHERE langid='cn';
UPDATE StatusValuesDes SET langid='dut' WHERE langid='nl';
UPDATE StatusValuesDes SET langid='eng' WHERE langid='en';
UPDATE StatusValuesDes SET langid='fin' WHERE langid='fi';
UPDATE StatusValuesDes SET langid='fre' WHERE langid='fr';
UPDATE StatusValuesDes SET langid='ger' WHERE langid='de';
UPDATE StatusValuesDes SET langid='nor' WHERE langid='no';
UPDATE StatusValuesDes SET langid='por' WHERE langid='pt';
UPDATE StatusValuesDes SET langid='rus' WHERE langid='ru';
UPDATE StatusValuesDes SET langid='spa' WHERE langid='es';
UPDATE StatusValuesDes SET langid='vie' WHERE langid='vi';


UPDATE CswServerCapabilitiesInfo SET langid='ara' WHERE langid='ar';
UPDATE CswServerCapabilitiesInfo SET langid='cat' WHERE langid='ca';
UPDATE CswServerCapabilitiesInfo SET langid='chi' WHERE langid='cn';
UPDATE CswServerCapabilitiesInfo SET langid='dut' WHERE langid='nl';
UPDATE CswServerCapabilitiesInfo SET langid='eng' WHERE langid='en';
UPDATE CswServerCapabilitiesInfo SET langid='fin' WHERE langid='fi';
UPDATE CswServerCapabilitiesInfo SET langid='fre' WHERE langid='fr';
UPDATE CswServerCapabilitiesInfo SET langid='ger' WHERE langid='de';
UPDATE CswServerCapabilitiesInfo SET langid='nor' WHERE langid='no';
UPDATE CswServerCapabilitiesInfo SET langid='por' WHERE langid='pt';
UPDATE CswServerCapabilitiesInfo SET langid='rus' WHERE langid='ru';
UPDATE CswServerCapabilitiesInfo SET langid='spa' WHERE langid='es';
UPDATE CswServerCapabilitiesInfo SET langid='vie' WHERE langid='vi';


DELETE FROM Languages WHERE id='ar';
DELETE FROM Languages WHERE id='cn';
DELETE FROM Languages WHERE id='de';
DELETE FROM Languages WHERE id='en';
DELETE FROM Languages WHERE id='es';
DELETE FROM Languages WHERE id='fr';
DELETE FROM Languages WHERE id='nl';
DELETE FROM Languages WHERE id='no';
DELETE FROM Languages WHERE id='pt';
DELETE FROM Languages WHERE id='ru';

ALTER TABLE Languages DROP COLUMN isocode;

ALTER TABLE IsoLanguages ADD shortcode varchar(2);

UPDATE IsoLanguages SET shortcode='ar' WHERE code='ara';
UPDATE IsoLanguages SET shortcode='ca' WHERE code='cat';
UPDATE IsoLanguages SET shortcode='ch' WHERE code='chi';
UPDATE IsoLanguages SET shortcode='nl' WHERE code='dut';
UPDATE IsoLanguages SET shortcode='en' WHERE code='eng';
UPDATE IsoLanguages SET shortcode='fi' WHERE code='fin';
UPDATE IsoLanguages SET shortcode='fr' WHERE code='fre';
UPDATE IsoLanguages SET shortcode='de' WHERE code='ger';
UPDATE IsoLanguages SET shortcode='no' WHERE code='nor';
UPDATE IsoLanguages SET shortcode='pt' WHERE code='por';
UPDATE IsoLanguages SET shortcode='ru' WHERE code='rus';
UPDATE IsoLanguages SET shortcode='es' WHERE code='spa';
UPDATE IsoLanguages SET shortcode='vi' WHERE code='vie';



UPDATE Settings SET value='2.9.0' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';

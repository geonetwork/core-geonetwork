
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

INSERT INTO Settings VALUES (800,1,'indexlanguages',NULL);
INSERT INTO Settings VALUES (801,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (802,801,'name','danish');
INSERT INTO Settings VALUES (803,801,'selected','false');
INSERT INTO Settings VALUES (804,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (805,804,'name','dutch');
INSERT INTO Settings VALUES (806,804,'selected','false');
INSERT INTO Settings VALUES (807,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (808,807,'name','english');
INSERT INTO Settings VALUES (809,807,'selected','true');
INSERT INTO Settings VALUES (810,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (811,810,'name','finnish');
INSERT INTO Settings VALUES (812,810,'selected','false');
INSERT INTO Settings VALUES (813,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (814,813,'name','french');
INSERT INTO Settings VALUES (815,813,'selected','false');
INSERT INTO Settings VALUES (816,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (817,816,'name','german');
INSERT INTO Settings VALUES (818,816,'selected','false');
INSERT INTO Settings VALUES (819,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (820,819,'name','hungarian');
INSERT INTO Settings VALUES (821,819,'selected','false');
INSERT INTO Settings VALUES (822,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (823,822,'name','italian');
INSERT INTO Settings VALUES (824,822,'selected','false');
INSERT INTO Settings VALUES (825,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (826,825,'name','norwegian');
INSERT INTO Settings VALUES (827,825,'selected','false');
INSERT INTO Settings VALUES (828,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (829,828,'name','portuguese');
INSERT INTO Settings VALUES (830,828,'selected','false');
INSERT INTO Settings VALUES (831,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (832,831,'name','russian');
INSERT INTO Settings VALUES (833,831,'selected','false');
INSERT INTO Settings VALUES (834,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (835,834,'name','spanish');
INSERT INTO Settings VALUES (836,834,'selected','false');
INSERT INTO Settings VALUES (837,800,'indexlanguage',NULL);
INSERT INTO Settings VALUES (838,837,'name','swedish');
INSERT INTO Settings VALUES (839,837,'selected','false');

UPDATE Settings SET value='2.6.2' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';
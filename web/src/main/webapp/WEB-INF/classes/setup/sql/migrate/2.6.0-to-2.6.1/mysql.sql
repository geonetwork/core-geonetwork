-- ======================================================================

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

INSERT INTO Settings VALUES (85, 80, 'uidAttr', 'uid');
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

UPDATE Settings SET value='2.6.1' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';

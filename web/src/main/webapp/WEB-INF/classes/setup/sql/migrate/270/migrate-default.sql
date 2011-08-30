CREATE TABLE Thesaurus (
    id   varchar(250) not null,
    activated    varchar(1),
    primary key(id)
  );

ALTER TABLE Metadata ALTER COLUMN createDate TYPE varchar(30);
ALTER TABLE Metadata ALTER COLUMN changeDate TYPE varchar(30);
ALTER TABLE Metadata ADD doctype varchar(255);

INSERT INTO Settings VALUES (240,1,'autofixing',NULL);
INSERT INTO Settings VALUES (241,240,'enable','true');

INSERT INTO Settings VALUES (910,1,'metadata',NULL);
INSERT INTO Settings VALUES (911,910,'enableSimpleView','true');
INSERT INTO Settings VALUES (912,910,'enableIsoView','true');
INSERT INTO Settings VALUES (913,910,'enableInspireView','false');
INSERT INTO Settings VALUES (914,910,'enableXmlView','true');
INSERT INTO Settings VALUES (915,910,'defaultView','simple');
INSERT INTO Settings VALUES (920,1,'threadedindexing',NULL);
INSERT INTO Settings VALUES (921,920,'maxthreads','1');

UPDATE Settings SET value='2.7.0' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';
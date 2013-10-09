CREATE TABLE HarvesterSettings
  (
    id        int            not null,
    parentId  int,
    name      varchar(64)    not null,
    value     longvarchar,

    primary key(id),

    foreign key(parentId) references HarvesterSettings(id)
  );
  
INSERT INTO settings (name, value, datatype, position) VALUES ('system/harvesting/mail/recipient', NULL, 0, 9020);
INSERT INTO settings (name, value, datatype, position) VALUES ('system/harvesting/mail/template', '', 0, 9021);
INSERT INTO settings (name, value, datatype, position) VALUES ('system/harvesting/mail/templateError', 'There was an error on the harvesting: $$errorMsg$$', 0, 9022);
INSERT INTO settings (name, value, datatype, position) VALUES ('system/harvesting/mail/templateWarning', '', 0, 9023);
INSERT INTO settings (name, value, datatype, position) VALUES ('system/harvesting/mail/subject', '[$$harvesterType$$] $$harvesterName$$ finished harvesting', 0, 9024);
INSERT INTO settings (name, value, datatype, position) VALUES ('system/harvesting/mail/enabled', 'false', 2, 9025);
INSERT INTO settings (name, value, datatype, position) VALUES ('system/harvesting/mail/level1', 'false', 2, 9026);
INSERT INTO settings (name, value, datatype, position) VALUES ('system/harvesting/mail/level2', 'false', 2, 9027);
INSERT INTO settings (name, value, datatype, position) VALUES ('system/harvesting/mail/level3', 'false', 2, 9028);
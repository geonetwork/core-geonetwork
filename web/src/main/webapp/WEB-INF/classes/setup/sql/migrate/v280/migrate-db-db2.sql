
CREATE TABLE HarvestHistory
  (
    id             int not null,
    harvestDate    varchar(30),
        harvesterUuid  varchar(250),
        harvesterName  varchar(128),
        harvesterType  varchar(128),
    deleted        char(1) default 'n' not null,
    info           CLOB(1G),
    params         CLOB(1G),

    primary key(id)

  );

CREATE INDEX HarvestHistoryNDX1 ON HarvestHistory(harvestDate);


CREATE TABLE MetadataStatus
  (
    metadataId  int not null,
    statusId    int default 0 not null,
    userId      int not null,
    changeDate   varchar(30)    not null,
    changeMessage   varchar(2048) not null,
    primary key(metadataId,statusId,userId,changeDate)
  );


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

CREATE INDEX MetadataNDX3 ON Metadata(owner);
ALTER TABLE MetadataStatus ADD FOREIGN KEY (metadataId) REFERENCES Metadata (id);
ALTER TABLE MetadataStatus ADD FOREIGN KEY (statusId) REFERENCES StatusValues (id);
ALTER TABLE MetadataStatus ADD FOREIGN KEY (userId) REFERENCES Users (id);
ALTER TABLE StatusValuesDes ADD FOREIGN KEY (idDes) REFERENCES StatusValues (id);
ALTER TABLE StatusValuesDes ADD FOREIGN KEY (langId) REFERENCES Languages (id);

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

ALTER TABLE Users ALTER COLUMN username SET DATA TYPE varchar(256);

ALTER TABLE Metadata ALTER COLUMN createDate SET DATA TYPE varchar(30);
ALTER TABLE Metadata ALTER COLUMN changeDate SET DATA TYPE varchar(30);
ALTER TABLE Metadata ADD doctype varchar(255);

DROP TABLE IndexLanguages;

ALTER TABLE Languages DROP COLUMN isocode;

ALTER TABLE IsoLanguages ADD shortcode varchar(2);

ALTER TABLE Categories ALTER COLUMN  name SET DATA TYPE varchar(255);
ALTER TABLE CategoriesDes ALTER COLUMN label SET DATA TYPE varchar(255);
ALTER TABLE Settings ALTER COLUMN name SET DATA TYPE varchar(64);

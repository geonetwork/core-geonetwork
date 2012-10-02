ALTER TABLE "Settings" ALTER COLUMN "value" varchar(max);
ALTER TABLE "Metadata" ALTER COLUMN "data" XML;
ALTER TABLE "MetadataNotifications" ALTER COLUMN "errormsg" varchar(max);
ALTER TABLE "CswServerCapabilitiesInfo" ALTER COLUMN "label" varchar(max);

CREATE TABLE HarvestHistory
  (
    id             int not null,
    harvestDate    varchar(30),
        harvesterUuid  varchar(250),
        harvesterName  varchar(128),
        harvesterType  varchar(128),
    deleted        char(1) default 'n' not null,
    info           XML,
    params         XML,

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

ALTER TABLE Users ALTER COLUMN username varchar(256);

ALTER TABLE Metadata ALTER COLUMN createDate varchar(30);
ALTER TABLE Metadata ALTER COLUMN changeDate varchar(30);
ALTER TABLE Metadata ADD doctype varchar(255);

DROP TABLE IndexLanguages;

ALTER TABLE Languages DROP COLUMN isocode;

ALTER TABLE IsoLanguages ADD shortcode varchar(2);

ALTER TABLE Categories ALTER COLUMN  name varchar(255);
ALTER TABLE CategoriesDes ALTER COLUMN label varchar(255);
ALTER TABLE Settings ALTER COLUMN name varchar(64);
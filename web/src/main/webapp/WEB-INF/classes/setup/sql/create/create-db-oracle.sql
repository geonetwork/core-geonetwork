REM ======================================================================
REM ===   Sql Script for Database : Geonet
REM ===
REM === Build : 153
REM ======================================================================

CREATE TABLE Relations
  (
    id         int,
    relatedId  int,
    primary key(id,relatedId)
  );

REM ======================================================================

CREATE TABLE Categories
  (
    id    int,
    name  varchar(32)   not null,
    primary key(id),
    unique(name)
  );

REM ======================================================================

CREATE TABLE CustomElementSet
  (
    xpath  varchar(1000) not null
  );

REM ======================================================================

CREATE TABLE Settings
  (
    id        int,
    parentId  int,
    name      varchar(32)    not null,
    value     long,
    primary key(id)
  );

REM ======================================================================

CREATE TABLE Languages
  (
    id    varchar(5),
    name  varchar(32)   not null,
    isocode varchar(3)  not null,
    primary key(id)
  );

REM ======================================================================

CREATE TABLE Sources
  (
    uuid     varchar(250),
    name     varchar(250),
    isLocal  char(1)        default 'y',
    primary key(uuid)
  );

REM ======================================================================

CREATE TABLE IsoLanguages
  (
    id    int,
    code  varchar(3)   not null,
    primary key(id),
    unique(code)
  );

REM ======================================================================

CREATE TABLE IsoLanguagesDes
  (
    idDes   int,
    langId  varchar(5),
    label   varchar(96)   not null,
    primary key(idDes,langId)
  );

REM ======================================================================

CREATE TABLE Regions
  (
    id     int,
    north  float   not null,
    south  float   not null,
    west   float   not null,
    east   float   not null,
    primary key(id)
  );

REM ======================================================================

CREATE TABLE RegionsDes
  (
    idDes   int,
    langId  varchar(5),
    label   varchar(96)   not null,
    primary key(idDes,langId)
  );

REM ======================================================================

CREATE TABLE Users
  (
    id            int,
    username      varchar(32)    not null,
    password      varchar(40)    not null,
    surname       varchar(32),
    name          varchar(32),
    profile       varchar(32)    not null,
    address       varchar(128),
    city          varchar(128),
    state         varchar(32),
    zip           varchar(16),
    country       varchar(128),
    email         varchar(128),
    organisation  varchar(128),
    kind          varchar(16),
    primary key(id),
    unique(username)
  );

REM ======================================================================

CREATE TABLE Operations
  (
    id        int,
    name      varchar(32)   not null,
    reserved  char(1)       default 'n' not null,
    primary key(id)
  );

REM ======================================================================

CREATE TABLE OperationsDes
  (
    idDes   int,
    langId  varchar(5),
    label   varchar(96)   not null,
    primary key(idDes,langId)
  );

REM ======================================================================

CREATE TABLE Groups
  (
    id           int,
    name         varchar(32)    not null,
    description  varchar(255),
    email        varchar(32),
    referrer     int,
    primary key(id),
    unique(name)
  );

REM ======================================================================

CREATE TABLE GroupsDes
  (
    idDes   int,
    langId  varchar(5),
    label   varchar(96)   not null,
    primary key(idDes,langId)
  );

REM ======================================================================

CREATE TABLE UserGroups
  (
    userId   int,
    groupId  int,
    primary key(userId,groupId)
  );

REM ======================================================================

CREATE TABLE CategoriesDes
  (
    idDes   int,
    langId  varchar(5),
    label   varchar(96)   not null,
    primary key(idDes,langId)
  );

REM ======================================================================

CREATE TABLE Metadata
  (
    id           int,
    uuid         varchar(250)   not null,
    schemaId     varchar(32)    not null,
    isTemplate   char(1)        default 'n' not null,
    isHarvested  char(1)        default 'n' not null,
    createDate   varchar(24)    not null,
    changeDate   varchar(24)    not null,
    data         long           not null,
    source       varchar(250)   not null,
    title        varchar(255),
    root         varchar(255),
    harvestUuid  varchar(250)   default null,
    owner        int            not null,
    groupOwner   int            default null,
    harvestUri   varchar(255)   default null,
    rating       int            default 0 not null,
    popularity   int            default 0 not null,
	displayorder int,
    primary key(id),
    unique(uuid)
  );

REM ======================================================================

CREATE TABLE MetadataCateg
  (
    metadataId  int,
    categoryId  int,
    primary key(metadataId,categoryId)
  );

REM ======================================================================

CREATE TABLE OperationAllowed
  (
    groupId      int,
    metadataId   int,
    operationId  int,
    primary key(groupId,metadataId,operationId)
  );

REM ======================================================================

CREATE TABLE MetadataRating
  (
    metadataId  int,
    ipAddress   varchar(32),
    rating      int           not null,
    primary key(metadataId,ipAddress)
  );

REM ======================================================================

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

REM ======================================================================

CREATE TABLE MetadataNotifications
  (
    metadataId         int,
    notifierId         int,
    notified           char(1)        default 'n' not null,
    metadataUuid       varchar(250)   not null,
    action             char(1)        not null,
    errormsg           long,

    primary key(metadataId,notifierId)
  );


REM ======================================================================

CREATE TABLE CswServerCapabilitiesInfo
  (
    idField   int,
    langId    varchar(5)    not null,
    field     varchar(32)   not null,
    label     long,

    primary key(idField)
  );

REM ======================================================================

CREATE TABLE IndexLanguages
  (
    id            int,
    languageName  varchar(32)   not null,
    selected      char(1)       default 'n' not null,

    primary key(id, languageName)

  );

REM ======================================================================

REM CREATE INDEX MetadataNDX1 ON Metadata(uuid);
CREATE INDEX MetadataNDX2 ON Metadata(source);

ALTER TABLE CategoriesDes ADD FOREIGN KEY (idDes) REFERENCES Categories (id);
ALTER TABLE CategoriesDes ADD FOREIGN KEY (langId) REFERENCES Languages (id);
ALTER TABLE Groups ADD FOREIGN KEY (referrer) REFERENCES Users (id);
ALTER TABLE GroupsDes ADD FOREIGN KEY (langId) REFERENCES Languages (id);
ALTER TABLE GroupsDes ADD FOREIGN KEY (idDes) REFERENCES Groups (id);
ALTER TABLE IsoLanguagesDes ADD FOREIGN KEY (langId) REFERENCES Languages (id);
ALTER TABLE IsoLanguagesDes ADD FOREIGN KEY (idDes) REFERENCES IsoLanguages (id);
ALTER TABLE Metadata ADD FOREIGN KEY (owner) REFERENCES Users (id);
ALTER TABLE Metadata ADD FOREIGN KEY (groupOwner) REFERENCES Groups (id);
ALTER TABLE MetadataCateg ADD FOREIGN KEY (categoryId) REFERENCES Categories (id);
ALTER TABLE MetadataCateg ADD FOREIGN KEY (metadataId) REFERENCES Metadata (id);
ALTER TABLE MetadataRating ADD FOREIGN KEY (metadataId) REFERENCES Metadata (id);
ALTER TABLE OperationAllowed ADD FOREIGN KEY (operationId) REFERENCES Operations (id);
ALTER TABLE OperationAllowed ADD FOREIGN KEY (groupId) REFERENCES Groups (id);
ALTER TABLE OperationAllowed ADD FOREIGN KEY (metadataId) REFERENCES Metadata (id);
ALTER TABLE OperationsDes ADD FOREIGN KEY (langId) REFERENCES Languages (id);
ALTER TABLE OperationsDes ADD FOREIGN KEY (idDes) REFERENCES Operations (id);
ALTER TABLE RegionsDes ADD FOREIGN KEY (langId) REFERENCES Languages (id);
ALTER TABLE RegionsDes ADD FOREIGN KEY (idDes) REFERENCES Regions (id);
ALTER TABLE Settings ADD FOREIGN KEY (parentId) REFERENCES Settings (id);
ALTER TABLE UserGroups ADD FOREIGN KEY (userId) REFERENCES Users (id);
ALTER TABLE UserGroups ADD FOREIGN KEY (groupId) REFERENCES Groups (id);
ALTER TABLE MetadataNotifications ADD FOREIGN KEY (notifierId) REFERENCES MetadataNotifiers(id);
ALTER TABLE CswServerCapabilitiesInfo ADD FOREIGN KEY (langId) REFERENCES Languages (id);

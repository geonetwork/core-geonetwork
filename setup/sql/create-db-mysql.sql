-- ======================================================================
-- ===   Sql Script for Database : Geonet
-- ===
-- === Build : 127
-- ======================================================================

CREATE TABLE Metadata
  (
    id           int,
    uuid         varchar(36)    not null,
    schemaId     varchar(32)    not null,
    isTemplate   char(1)        default 'n' not null,
    isHarvested  char(1)        default 'n' not null,
    createDate   varchar(24)    not null,
    changeDate   varchar(24)    not null,
    data         text           not null,
    source       varchar(36)    not null,
    sourceUri    varchar(255),
    title        varchar(255),
    root         varchar(255),

    primary key(id),
    unique(uuid)
  );

CREATE INDEX MetadataNDX1 ON Metadata(source);

-- ======================================================================

CREATE TABLE Categories
  (
    id    int,
    name  varchar(32)   not null,

    primary key(id),
    unique(name)
  );

-- ======================================================================

CREATE TABLE Settings
  (
    id        int,
    parentId  int,
    name      varchar(32)    not null,
    value     varchar(250),

    primary key(id),

    foreign key(parentId) references Settings(id)
  );

-- ======================================================================

CREATE TABLE Languages
  (
    id    varchar(5),
    name  varchar(32)   not null,

    primary key(id)
  );

-- ======================================================================

CREATE TABLE KnownNodes
  (
    siteId   varchar(36),
    name     varchar(250),
    host     varchar(250),
    port     int,
    servlet  varchar(250),

    primary key(siteId)
  );

-- ======================================================================

CREATE TABLE Operations
  (
    id        int,
    name      varchar(32)   not null,
    reserved  char(1)       default 'n' not null,

    primary key(id)
  );

-- ======================================================================

CREATE TABLE OperationsDes
  (
    idDes   int,
    langId  varchar(5),
    label   varchar(32)   not null,

    primary key(idDes,langId),

    foreign key(idDes) references Operations(id),
    foreign key(langId) references Languages(id)
  );

-- ======================================================================

CREATE TABLE Regions
  (
    id     int,
    north  float   not null,
    south  float   not null,
    west   float   not null,
    east   float   not null,

    primary key(id)
  );

-- ======================================================================

CREATE TABLE RegionsDes
  (
    idDes   int,
    langId  varchar(5),
    label   varchar(32)   not null,

    primary key(idDes,langId),

    foreign key(idDes) references Regions(id),
    foreign key(langId) references Languages(id)
  );

-- ======================================================================

CREATE TABLE Users
  (
    id            int,
    username      varchar(32)    not null,
    password      varchar(32)    not null,
    surname       varchar(32),
    name          varchar(32),
    profile       varchar(32)    not null,
    address       varchar(128),
    state         varchar(32),
    zip           varchar(16),
    country       varchar(128),
    email         varchar(128),
    organisation  varchar(128),
    kind          varchar(16),

    primary key(id),
    unique(username)
  );

-- ======================================================================

CREATE TABLE MetadataCateg
  (
    metadataId  int,
    categoryId  int,

    primary key(metadataId,categoryId),

    foreign key(metadataId) references Metadata(id),
    foreign key(categoryId) references Categories(id)
  );

-- ======================================================================

CREATE TABLE Groups
  (
    id           int,
    name         varchar(32)    not null,
    description  varchar(255),
    email        varchar(32),
    referrer     int,

    primary key(id),
    unique(name),

    foreign key(referrer) references Users(id)
  );

-- ======================================================================

CREATE TABLE GroupsDes
  (
    idDes   int,
    langId  varchar(5),
    label   varchar(32)   not null,

    primary key(idDes,langId),

    foreign key(idDes) references Groups(id),
    foreign key(langId) references Languages(id)
  );

-- ======================================================================

CREATE TABLE UserGroups
  (
    userId   int,
    groupId  int,

    primary key(userId,groupId),

    foreign key(userId) references Users(id),
    foreign key(groupId) references Groups(id)
  );

-- ======================================================================

CREATE TABLE CategoriesDes
  (
    idDes   int,
    langId  varchar(5),
    label   varchar(32)   not null,

    primary key(idDes,langId),

    foreign key(idDes) references Categories(id),
    foreign key(langId) references Languages(id)
  );

-- ======================================================================

CREATE TABLE OperationAllowed
  (
    groupId      int,
    metadataId   int,
    operationId  int,

    primary key(groupId,metadataId,operationId),

    foreign key(groupId) references Groups(id),
    foreign key(metadataId) references Metadata(id),
    foreign key(operationId) references Operations(id)
  );

-- ======================================================================


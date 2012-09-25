CREATE TABLE Workspace
  (
    id           varchar2(36),
    uuid         varchar2(250)   not null,
    schemaId     varchar2(32)    not null,
    isTemplate   char(1)        default 'n' not null,
    isHarvested  char(1)        default 'n' not null,
    isLocked     char(1)        default 'n' not null,
    lockedBy     varchar2(36),
    createDate   varchar2(30)    not null,
    changeDate   varchar2(30)    not null,
    data         text    not null,
    source       varchar2(250)   not null,
    title        varchar2(255),
    root         varchar2(255),
    harvestUuid  varchar2(250)   default null,
    owner        varchar2(36)    not null,
    doctype      varchar2(255),
    harvestUri   varchar2(255)   default null,
    rating       int            default 0 not null,
    popularity   int            default 0 not null,
		displayorder int,

    primary key(id),
    unique(uuid)
  );
  
  CREATE TABLE ValidationWorkspace
  (
    metadataId   varchar2(36),
    valType      varchar2(40),
    status       int,
    tested       int,
    failed       int,
    valDate      varchar2(30),

    primary key(metadataId, valType)
);

ALTER TABLE Metadata ADD lockedBy varchar2(36);
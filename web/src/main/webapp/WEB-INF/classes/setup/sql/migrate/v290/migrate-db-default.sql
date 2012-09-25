CREATE TABLE Workspace
  (
    id           varchar(36),
    uuid         varchar(250)   not null,
    schemaId     varchar(32)    not null,
    isTemplate   char(1)        default 'n' not null,
    isHarvested  char(1)        default 'n' not null,
    createDate   varchar(30)    not null,
    changeDate   varchar(30)    not null,
    isLocked     char(1)        default 'n' not null,
    lockedBy     varchar(36),
    data         longvarchar    not null,
    source       varchar(250)   not null,
    title        varchar(255),
    root         varchar(255),
    harvestUuid  varchar(250)   default null,
    owner        varchar(36)    not null,
    doctype      varchar(255),
    harvestUri   varchar(255)   default null,
    rating       int            default 0 not null,
    popularity   int            default 0 not null,
		displayorder int,

    primary key(id),
    unique(uuid)
  );

CREATE TABLE ValidationWorkspace
  (
    metadataId   varchar(36),
    valType      varchar(40),
    status       int,
    tested       int,
    failed       int,
    valDate      varchar(30),

    primary key(metadataId, valType),
);


ALTER TABLE Metadata ADD lockedBy varchar(36);
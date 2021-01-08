-- Create temporary tables used when modifying a column type

-- Convert Profile column to the profile enumeration ordinal

CREATE TABLE USERGROUPS_TMP 
(
   USERID int NOT NULL,
   GROUPID int NOT NULL,
   PROFILE int NOT NULL
);

-- Convert Profile column to the profile enumeration ordinal

CREATE TABLE USERS_TMP
  (
    id            int         ,
    username      varchar(256),
    password      varchar(120),
    surname       varchar(32),
    name          varchar(32),
    profile       int,
    organisation  varchar(128),
    kind          varchar(16),
    security      varchar(128),
    authtype      varchar(32),
	
    primary key(id),
    unique(username)
  );

-- ----  Change notifier actions column to map to the MetadataNotificationAction enumeration

CREATE TABLE MetadataNotifications_Tmp
  (
    metadataId         int            not null,
    notifierId         int            not null,
    notified           char(1)        default 'n' not null,
    metadataUuid       varchar(250)   not null,
    action             int            not null,
    errormsg           text
  );

-- ----  Change params querytype column to map to the LuceneQueryParamType enumeration

CREATE TABLE Params_TEMP
  (
    id          int           not null,
    requestId   int,
    queryType   int,
    termField   varchar(128),
    termText    varchar(128),
    similarity  float,
    lowerText   varchar(128),
    upperText   varchar(128),
    inclusive   char(1)
);

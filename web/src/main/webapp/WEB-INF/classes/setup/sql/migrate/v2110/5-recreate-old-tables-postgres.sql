-- Drop the old tables (that are being migrated to an enum) and create them again with new definition

-- Update UserGroups profiles to be one of the enumerated profiles

DROP TABLE USERGROUPS;
CREATE TABLE USERGROUPS
  (
    userId   int          not null,
    groupId  int          not null,
    profile  int          not null,

    primary key(userId,groupId,profile),

    foreign key(userId) references Users(id),
    foreign key(groupId) references Groups(id)
  );
-- Update UserGroups profiles to be one of the enumerated profiles

INSERT INTO USERGROUPS SELECT * FROM USERGROUPS_TMP;
DROP TABLE USERGROUPS_TMP;


-- Convert Profile column to the profile enumeration ordinal

ALTER TABLE metadata DROP CONSTRAINT IF EXISTS metadata_owner_fkey;
ALTER TABLE metadatastatus DROP CONSTRAINT IF EXISTS metadatastatus_userid_fkey;
ALTER TABLE useraddress DROP CONSTRAINT IF EXISTS useraddress_userid_fkey;
ALTER TABLE email DROP CONSTRAINT IF EXISTS email_user_id_fkey;
ALTER TABLE groups DROP CONSTRAINT IF EXISTS groups_referrer_fkey;
ALTER TABLE usergroups DROP CONSTRAINT IF EXISTS usergroups_userid_fkey;
DROP TABLE Users;
CREATE TABLE Users
  (
    id            int           not null,
    username      varchar(256)  not null,
    password      varchar(120)  not null,
    surname       varchar(32),
    name          varchar(32),
    profile       int not null,
    organisation  varchar(128),
    kind          varchar(16),
    security      varchar(128)  default '',
    authtype      varchar(32),
    primary key(id),
    unique(username)
  );


-- Convert Profile column to the profile enumeration ordinal

INSERT INTO USERS SELECT * FROM USERS_TMP;
DROP TABLE USERS_TMP;


ALTER TABLE metadata ADD CONSTRAINT metadata_owner_fkey FOREIGN KEY (owner)
      REFERENCES users (id);
ALTER TABLE metadatastatus ADD CONSTRAINT metadatastatus_userid_fkey FOREIGN KEY (userid)
      REFERENCES users (id);
ALTER TABLE useraddress ADD CONSTRAINT useraddress_userid_fkey FOREIGN KEY (userid)
      REFERENCES users (id);
ALTER TABLE email ADD CONSTRAINT email_user_id_fkey FOREIGN KEY (user_id)
      REFERENCES users (id);
ALTER TABLE groups ADD CONSTRAINT groups_referrer_fkey FOREIGN KEY (referrer)
      REFERENCES users (id);


-- ----  Change notifier actions column to map to the MetadataNotificationAction enumeration

DROP TABLE MetadataNotifications;
CREATE TABLE MetadataNotifications
  (
    metadataId         int            not null,
    notifierId         int            not null,
    notified           char(1)        default 'n' not null,
    metadataUuid       varchar(250)   not null,
    action             int        not null,
    errormsg           text,
    primary key(metadataId,notifierId)
  );

-- ----  Change notifier actions column to map to the MetadataNotificationAction enumeration

INSERT INTO MetadataNotifications SELECT * FROM MetadataNotifications_Tmp;
DROP TABLE MetadataNotifications_Tmp;

-- ----  Change params querytype column to map to the LuceneQueryParamType enumeration

DROP TABLE Params;

CREATE TABLE Params
  (
    id          int           not null,
    requestId   int,
    queryType   int,
    termField   varchar(128),
    termText    varchar(128),
    similarity  float,
    lowerText   varchar(128),
    upperText   varchar(128),
    inclusive   char(1),
    primary key(id),
    foreign key(requestId) references Requests(id)
  );

-- ----  Change params querytype column to map to the LuceneQueryParamType enumeration

INSERT INTO Params SELECT * FROM Params_TEMP;
DROP TABLE Params_TEMP;

CREATE INDEX ParamsNDX1 ON Params(requestId);
CREATE INDEX ParamsNDX2 ON Params(queryType);
CREATE INDEX ParamsNDX3 ON Params(termField);
CREATE INDEX ParamsNDX4 ON Params(termText);
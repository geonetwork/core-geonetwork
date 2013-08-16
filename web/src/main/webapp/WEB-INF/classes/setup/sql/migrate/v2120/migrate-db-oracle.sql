ALTER TABLE operations DROP COLUMN reserved;

CREATE TABLE USERGROUPS_TMP 
(
   USERID int NOT NULL,
   GROUPID int NOT NULL,
   PROFILE int NOT NULL,
);

INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 0 FROM USERGROUPS where profile='Administrator';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 1 FROM USERGROUPS where profile='UserAdmin';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 2 FROM USERGROUPS where profile='Reviewer';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 3 FROM USERGROUPS where profile='Editor';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 4 FROM USERGROUPS where profile='RegisteredUser';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 5 FROM USERGROUPS where profile='Guest';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 6 FROM USERGROUPS where profile='Monitor';

DROP TABLE USERGROUPS;
CREATE TABLE USERGROUPS
  (
    userId   int          not null,
    groupId  int          not null,
    profile  int          not null,

    primary key(userId,groupId,profile)
  );

INSERT INTO USERGROUPS (SELECT * FROM USERGROUPS_TMP);


-- Convert Profile column to the profile enumeration ordinal
-- create address and email tables to allow multiple per user

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

INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 0, organisation, kind, security, authtype FROM USERS where profile='Administrator';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 1, organisation, kind, security, authtype FROM USERS where profile='UserAdmin';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 2, organisation, kind, security, authtype FROM USERS where profile='Reviewer';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 3, organisation, kind, security, authtype FROM USERS where profile='Editor';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 4, organisation, kind, security, authtype FROM USERS where profile='RegisteredUser';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 5, organisation, kind, security, authtype FROM USERS where profile='Guest';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 6, organisation, kind, security, authtype FROM USERS where profile='Monitor';

CREATE TABLE USER_ADDRESS 
(
	userid 	int not null,
	addressid int not null,
	primary key(userid,addressid)
);

CREATE TABLE ADDRESS 
(
	id			  int 			not null,
	address       varchar(128),
	city          varchar(128),
	state         varchar(32),
	zip           varchar(16),
	country       varchar(128),
	primary key(id)
);

CREATE TABLE EMAIL 
(
	user_id			  int 			not null,
	email         varchar(128),
	primary key(userid)
);

INSERT INTO ADDRESS SELECT id, address, city, state, zip, country FROM Users;
INSERT INTO USER_ADDRESS SELECT id, id FROM Users;
INSERT INTO EMAIL SELECT id, email FROM Users;
  
DROP TABLE Users;
CREATE TABLE Users
  (
    id            int           not null,
    username      varchar(256)  not null,
    password      varchar(120)  not null,
    surname       varchar(32),
    name          varchar(32),
    profile       varchar(32)   not null,
    organisation  varchar(128),
    kind          varchar(16),
    security      varchar(128)  default '',
    authtype      varchar(32),
    primary key(id),
    unique(username)
  );
  
INSERT INTO USERS SELECT * FROM USERS_TMP;
DROP TABLE USERS_TMP;

-- ----  Change notifier actions column to map to the MetadataNotificationAction enumeration

CREATE TABLE MetadataNotifications_Tmp
  (
    metadataId         int            not null,
    notifierId         int            not null,
    notified           char(1)        default 'n' not null,
    metadataUuid       varchar(250)   not null,
    action             char(1)        not null,
    errormsg           clob
  );


INSERT INTO MetadataNotifications_Tmp SELECT metadataId, notifierId, notified, metadataUuid, 0, errormsg FROM MetadataNotifications where action='u';
INSERT INTO MetadataNotifications_Tmp SELECT metadataId, notifierId, notified, metadataUuid, 1, errormsg FROM MetadataNotifications where action='d';

DROP TABLE MetadataNotifications;
CREATE TABLE MetadataNotifications
  (
    metadataId         int            not null,
    notifierId         int            not null,
    notified           char(1)        default 'n' not null,
    metadataUuid       varchar(250)   not null,
    action             int        not null,
    errormsg           clob,
    primary key(metadataId,notifierId)
  );
  
INSERT INTO MetadataNotifications SELECT * FROM MetadataNotifications_Tmp;
DROP TABLE MetadataNotifications_Tmp;

-- ----  Change params querytype column to map to the LuceneQueryParamType enumeration

CREATE TABLE Params_TEMP
  (
    id          int           not null,
    requestId   int,
    queryType   int,
    termField   varchar2(128),
    termText    varchar2(128),
    similarity  float,
    lowerText   varchar2(128),
    upperText   varchar2(128),
    inclusive   char(1)
);


INSERT INTO Params_TEMP SELECT id, requestId, 0, termField, termText, similarity, lowerText, upperText, inclusive FROM MetadataNotifications where action='BOOLEAN_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 1, termField, termText, similarity, lowerText, upperText, inclusive FROM MetadataNotifications where action='TERM_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 2, termField, termText, similarity, lowerText, upperText, inclusive FROM MetadataNotifications where action='FUZZY_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 3, termField, termText, similarity, lowerText, upperText, inclusive FROM MetadataNotifications where action='PREFIX_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 4, termField, termText, similarity, lowerText, upperText, inclusive FROM MetadataNotifications where action='MATCH_ALL_DOCS_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 5, termField, termText, similarity, lowerText, upperText, inclusive FROM MetadataNotifications where action='WILDCARD_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 6, termField, termText, similarity, lowerText, upperText, inclusive FROM MetadataNotifications where action='PHRASE_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 7, termField, termText, similarity, lowerText, upperText, inclusive FROM MetadataNotifications where action='RANGE_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 8, termField, termText, similarity, lowerText, upperText, inclusive FROM MetadataNotifications where action='NUMERIC_RANGE_QUERY';

DROP TABLE Params;
DROP INDEX ParamsNDX1 ON Params(requestId);
DROP INDEX ParamsNDX2 ON Params(queryType);
DROP INDEX ParamsNDX3 ON Params(termField);
DROP INDEX ParamsNDX4 ON Params(termText);

CREATE TABLE Params
  (
    id          int           not null,
    requestId   int,
    queryType   int,
    termField   varchar2(128),
    termText    varchar2(128),
    similarity  float,
    lowerText   varchar2(128),
    upperText   varchar2(128),
    inclusive   char(1),
    primary key(id),
    foreign key(requestId) references Requests(id)
  );

CREATE INDEX ParamsNDX1 ON Params(requestId);
CREATE INDEX ParamsNDX2 ON Params(queryType);
CREATE INDEX ParamsNDX3 ON Params(termField);
CREATE INDEX ParamsNDX4 ON Params(termText);


INSERT INTO Params SELECT * FROM Params_TEMP;
DROP TABLE Params_TEMP;

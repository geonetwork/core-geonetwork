-- Creates New tables required for this version

CREATE TABLE HarvesterSettings
  (
    id        int            not null,
    parentId  int,
    name      varchar(64)    not null,
    value     longtext,

    primary key(id),

    foreign key(parentId) references HarvesterSettings(id)
  );

CREATE TABLE USERADDRESS
(
	userid 	int not null,
	addressid int not null,
	primary key(userid,addressid),
    foreign key(userid) references Users(id),
    foreign key(addressid) references Address(id)
);

CREATE TABLE USERADDRESS
(
	userid 	int not null,
	addressid int not null,
	primary key(userid,addressid),
    foreign key(userid) references Users(id),
    foreign key(addressid) references Address(id)
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

	primary key(userid),
    foreign key(userId) references Users(id),
);

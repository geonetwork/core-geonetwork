-- Creates New tables required for this version

ALTER TABLE Services ADD PRIMARY KEY (id);

CREATE TABLE HarvesterSettings
  (
    id        int            not null,
    parentId  int,
    name      varchar(64)    not null,
    value     CLOB(1G),
    primary key(id)
  );

ALTER TABLE HarvesterSettings ADD FOREIGN KEY (parentId) REFERENCES HarvesterSettings (id);

CREATE TABLE Address
(
	id			  int 			not null,
	address       varchar(128),
	city          varchar(128),
	state         varchar(32),
	zip           varchar(16),
	country       varchar(128),
	primary key(id)
);

CREATE TABLE UserAddress
(
	userid 	int not null,
	addressid int not null,
	primary key(userid,addressid)
);

CREATE TABLE Email
(
	user_id			  int 			not null,
	email         varchar(128),
	primary key(user_id)
);

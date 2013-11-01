-- Creates New tables required for this version

CREATE TABLE HarvesterSettings
  (
    id        int            not null,
    parentId  int,
    name      varchar2(64)   not null,
    value     clob,
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

CREATE TABLE USER_ADDRESS
(
	userId 	int not null,
	addressId int not null,
	primary key(userId,addressId)
);

CREATE TABLE Email
(
	user_id			  int 			not null,
	email         varchar(128),
	primary key(user_id)
);

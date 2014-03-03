-- Creates New tables required for this version

CREATE TABLE HarvesterSettings
  (
    id        int            not null,
    parentId  int,
    name      varchar(64)    not null,
    value     varchar(max),

    primary key(id),

    foreign key(parentId) references HarvesterSettings(id)
  );

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
	userId 	int not null,
	addressid int not null,
	primary key(userId,addressid),
    foreign key(userId) references Users(id),
    foreign key(addressid) references Address(id)
);

CREATE TABLE Email
(
	user_id			  int 			not null,
	email         varchar(128),

	primary key(user_id),
    foreign key(user_id) references Users(id)
);

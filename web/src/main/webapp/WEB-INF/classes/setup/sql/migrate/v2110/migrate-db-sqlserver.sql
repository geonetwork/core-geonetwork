CREATE TABLE HarvesterSettings
  (
    id        int            not null,
    parentId  int,
    name      varchar(64)    not null,
    value     varchar(max),

    primary key(id),

    foreign key(parentId) references HarvesterSettings(id)
  );
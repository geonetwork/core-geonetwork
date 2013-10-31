CREATE TABLE HarvesterSettings
  (
    id        int            not null,
    parentid  int,
    name      varchar(64)    not null,
    value     longvarchar,

    primary key(id),

    foreign key(parentId) references HarvesterSettings(id)
  );

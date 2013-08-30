CREATE TABLE HarvesterSettings
  (
    id        int            not null,
    parentId  int,
    name      varchar(64)    not null,
    value     CLOB(1G),
    primary key(id)
  );

ALTER TABLE HarvesterSettings ADD FOREIGN KEY (parentId) REFERENCES HarvesterSettings (id);
CREATE TABLE HarvesterSettings
  (
    id        int            not null,
    parentId  int,
    name      varchar2(64)   not null,
    value     clob,
    primary key(id)
  );
  
ALTER TABLE HarvesterSettings ADD FOREIGN KEY (parentId) REFERENCES HarvesterSettings (id);
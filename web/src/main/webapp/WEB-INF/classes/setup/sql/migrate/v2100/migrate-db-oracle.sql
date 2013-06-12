-- Spring security
ALTER TABLE Users ADD security varchar2(128) default '';
ALTER TABLE Users ADD authtype varchar2(32);
ALTER TABLE Users MODIFY password varchar2(120);

-- Support multiple profiles per user
ALTER TABLE usergroups ADD profile varchar2(32);
UPDATE usergroups SET profile = (SELECT profile from users WHERE id = userid);
ALTER TABLE usergroups DROP PRIMARY KEY;
ALTER TABLE usergroups ADD PRIMARY KEY (userid, profile, groupid);

ALTER TABLE Metadata MODIFY harvestUri varchar2(512);

ALTER TABLE HarvestHistory ADD elapsedTime int;

CREATE TABLE Services
  (
    id         int,
    name       varchar2(64)   not null,
    class       varchar2(1048)   not null,
    description       varchar2(1048),
    primary key(id)
  );
  

CREATE TABLE ServiceParameters
  (
    id         int,
    service     int,
    name       varchar2(64)   not null,
    value       varchar2(1048)   not null,
    primary key(id)
  );
  
ALTER TABLE ServiceParameters ADD FOREIGN KEY (service) REFERENCES services (id);

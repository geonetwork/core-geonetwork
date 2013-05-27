-- Spring security
ALTER TABLE Users ADD security varchar(128) default '';
ALTER TABLE Users ADD authtype varchar(32);
ALTER TABLE users ALTER "password" TYPE character varying(120);

-- Support multiple profiles per user
ALTER TABLE usergroups ADD profile varchar(32);
UPDATE usergroups SET profile = (SELECT profile from users WHERE id = userid);
ALTER TABLE usergroups DROP CONSTRAINT usergroups_pkey;
ALTER TABLE usergroups ADD PRIMARY KEY (userid, profile, groupid);

ALTER TABLE Metadata ALTER harvestUri TYPE varchar(512);

ALTER TABLE HarvestHistory ADD elapsedTime int;

CREATE TABLE Services
  (
  
    id         int,
    name       varchar(64)   not null,
    class       varchar(1048)   not null,
    description       varchar(1048),
        
    primary key(id)
  );
  

CREATE TABLE ServiceParameters
  (
    id         int,
    service     int,
    name       varchar(64)   not null,
    value       varchar(1048)   not null,
    
    primary key(id),
        
    foreign key(service) references Services(id)
  );


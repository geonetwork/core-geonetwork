ALTER TABLE usergroups DROP PRIMARY KEY;
ALTER TABLE usergroups ADD PRIMARY KEY (userid, profile, groupid);
ALTER TABLE Users MODIFY COLUMN password varchar(120) not null;
ALTER TABLE Metadata MODIFY COLUMN harvestUri varchar(455);

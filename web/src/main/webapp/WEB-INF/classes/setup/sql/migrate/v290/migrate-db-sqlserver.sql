ALTER TABLE usergroups DROP PRIMARY KEY;
ALTER TABLE usergroups ADD PRIMARY KEY (userid, profile, groupid);
ALTER TABLE Users ALTER COLUMN password varchar(120) not null;
ALTER TABLE Metadata ALTER COLUMN harvestUri varchar(455);

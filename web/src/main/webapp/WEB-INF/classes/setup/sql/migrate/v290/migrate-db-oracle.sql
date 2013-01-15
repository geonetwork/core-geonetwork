ALTER TABLE usergroups DROP PRIMARY KEY;
ALTER TABLE usergroups ADD PRIMARY KEY (userid, profile, groupid);
ALTER TABLE Users MODIFY password varchar2(120);
ALTER TABLE Metadata MODIFY harvestUri varchar2(512);

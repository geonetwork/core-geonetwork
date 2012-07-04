-- Support multiple profiles per user
ALTER TABLE usergroups ADD profile varchar(32);
ALTER TABLE usergroups DROP PRIMARY KEY;
ALTER TABLE usergroups ADD PRIMARY KEY (userid, profile, groupid);

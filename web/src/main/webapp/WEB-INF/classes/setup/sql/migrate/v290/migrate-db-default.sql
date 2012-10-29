-- Support multiple profiles per user
ALTER TABLE usergroups ADD profile varchar(32);

UPDATE usergroups SET profile = (SELECT profile from users WHERE id = userid);

ALTER TABLE usergroups DROP PRIMARY KEY;
ALTER TABLE usergroups ADD PRIMARY KEY (userid, profile, groupid);

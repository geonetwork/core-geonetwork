-- Support multiple profiles per user
ALTER TABLE usergroups ADD profile varchar(32);
ALTER TABLE usergroups DROP PRIMARY KEY;
ALTER TABLE usergroups ADD PRIMARY KEY (userid, profile, groupid);

INSERT INTO Settings VALUES (24,20,'securePort','8443');

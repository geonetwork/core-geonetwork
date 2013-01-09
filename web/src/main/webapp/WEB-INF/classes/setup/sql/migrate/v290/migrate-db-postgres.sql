ALTER TABLE usergroups DROP CONSTRAINT usergroups_pkey;
ALTER TABLE usergroups ADD PRIMARY KEY (userid, profile, groupid);
ALTER TABLE users ALTER "password" TYPE character varying(120);
ALTER TABLE Metadata ALTER harvestUri TYPE varchar(455);

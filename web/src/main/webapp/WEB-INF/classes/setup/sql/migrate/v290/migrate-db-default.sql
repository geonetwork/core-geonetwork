-- Support multiple profiles per user
ALTER TABLE usergroups ADD profile varchar(32);

UPDATE usergroups SET profile = (SELECT profile from users WHERE id = userid);

ALTER TABLE usergroups DROP PRIMARY KEY;
ALTER TABLE usergroups ADD PRIMARY KEY (userid, profile, groupid);

INSERT INTO Settings VALUES (24,20,'securePort','8443');

INSERT INTO Settings VALUES (960,1,'wiki',NULL);
INSERT INTO Settings VALUES (961,960,'markup','none');
INSERT INTO Settings VALUES (962,960,'output','strip');
INSERT INTO Settings VALUES (963,960,'mefoutput','strip');
INSERT INTO Settings VALUES (964,1,'wysiwyg',NULL);
INSERT INTO Settings VALUES (965,964,'enable','false');
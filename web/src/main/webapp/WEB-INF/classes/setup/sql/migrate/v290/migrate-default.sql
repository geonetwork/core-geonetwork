ALTER TABLE Relations ADD COLUMN id2 varchar(36);
UPDATE Relations SET id2 = id;
ALTER TABLE Relations DROP COLUMN id;
ALTER TABLE Relations RENAME COLUMN id2 TO id;
ALTER TABLE Relations ADD PRIMARY KEY (id,relatedId);


ALTER TABLE categories ADD COLUMN id2 varchar(36);
UPDATE categories SET id2 = id;
ALTER TABLE categories DROP COLUMN id CASCADE;
ALTER TABLE categories RENAME COLUMN id2 TO id;
ALTER TABLE categories ADD PRIMARY KEY (id);


ALTER TABLE categoriesdes ADD COLUMN iddes2 varchar(36);
UPDATE categoriesdes SET iddes2 = iddes;
ALTER TABLE categoriesdes DROP COLUMN iddes;
ALTER TABLE categoriesdes RENAME COLUMN iddes2 TO iddes;
ALTER TABLE categoriesdes ADD PRIMARY KEY (iddes, langid);
ALTER TABLE categoriesdes ADD CONSTRAINT categoriesdes_iddes_fkey FOREIGN KEY (iddes) REFERENCES categories (id);


ALTER TABLE users ADD COLUMN id2 varchar(36);
UPDATE users SET id2 = id;
ALTER TABLE users DROP COLUMN id CASCADE;
ALTER TABLE users RENAME COLUMN id2 TO id;
ALTER TABLE users ADD PRIMARY KEY (id);


ALTER TABLE Groups ADD COLUMN id2 varchar(36);
UPDATE Groups SET id2 = id;
ALTER TABLE Groups DROP COLUMN id CASCADE;
ALTER TABLE Groups RENAME COLUMN id2 TO id;
ALTER TABLE Groups ADD PRIMARY KEY (id);

ALTER TABLE Groups ADD COLUMN referrer2 varchar(36);
UPDATE Groups SET referrer2 = referrer;
ALTER TABLE Groups DROP COLUMN referrer CASCADE;
ALTER TABLE Groups RENAME COLUMN referrer2 TO referrer;
ALTER TABLE Groups ADD CONSTRAINT groups_referrer_fkey FOREIGN KEY (referrer) REFERENCES Users (id);


ALTER TABLE UserGroups ADD COLUMN userid2 varchar(36);
UPDATE UserGroups SET userid2 = userid;
ALTER TABLE UserGroups DROP COLUMN userid CASCADE;
ALTER TABLE UserGroups RENAME COLUMN userid2 TO userid;

ALTER TABLE UserGroups ADD COLUMN groupid2 varchar(36);
UPDATE UserGroups SET groupid2 = groupid;
ALTER TABLE UserGroups DROP COLUMN groupid CASCADE;
ALTER TABLE UserGroups RENAME COLUMN groupid2 TO groupid;

ALTER TABLE UserGroups ADD PRIMARY KEY (userid, groupid);
ALTER TABLE UserGroups ADD CONSTRAINT usergroups_userid_fkey FOREIGN KEY (userid) REFERENCES Users (id);
ALTER TABLE UserGroups ADD CONSTRAINT usergroups_groupid_fkey FOREIGN KEY (groupid) REFERENCES Groups (id);


ALTER TABLE GroupsDes ADD COLUMN iddes2 varchar(36);
UPDATE GroupsDes SET iddes2 = iddes;
ALTER TABLE GroupsDes DROP COLUMN iddes;
ALTER TABLE GroupsDes RENAME COLUMN iddes2 TO iddes;
ALTER TABLE GroupsDes ADD PRIMARY KEY (iddes, langid);
ALTER TABLE GroupsDes ADD CONSTRAINT groupsdes_iddes_fkey FOREIGN KEY (iddes) REFERENCES Groups (id);


ALTER TABLE Operations ADD COLUMN id2 varchar(36);
UPDATE Operations SET id2 = id;
ALTER TABLE Operations DROP COLUMN id CASCADE;
ALTER TABLE Operations RENAME COLUMN id2 TO id;
ALTER TABLE Operations ADD PRIMARY KEY (id);


ALTER TABLE Operationsdes ADD COLUMN iddes2 varchar(36);
UPDATE Operationsdes SET iddes2 = iddes;
ALTER TABLE Operationsdes DROP COLUMN iddes;
ALTER TABLE Operationsdes RENAME COLUMN iddes2 TO iddes;
ALTER TABLE Operationsdes ADD PRIMARY KEY (iddes, langid);
ALTER TABLE Operationsdes ADD CONSTRAINT operationsdes_iddes_fkey FOREIGN KEY (iddes) REFERENCES Operations (id);


ALTER TABLE Requests ADD COLUMN id2 varchar(36);
UPDATE Requests SET id2 = id;
ALTER TABLE Requests DROP COLUMN id CASCADE;
ALTER TABLE Requests RENAME COLUMN id2 TO id;
ALTER TABLE Requests ADD PRIMARY KEY (id);


ALTER TABLE Params ADD COLUMN id2 varchar(36);
UPDATE Params SET id2 = id;
ALTER TABLE Params DROP COLUMN id;
ALTER TABLE Params RENAME COLUMN id2 TO id;
ALTER TABLE Params ADD PRIMARY KEY (id);

ALTER TABLE Params ADD COLUMN requestid2 varchar(36);
UPDATE Params SET requestid2 = requestid;
ALTER TABLE Params DROP COLUMN requestid;
ALTER TABLE Params RENAME COLUMN requestid2 TO requestid;
ALTER TABLE Params ADD CONSTRAINT params_requestid_fkey FOREIGN KEY (requestid) REFERENCES Requests (id);


ALTER TABLE HarvestHistory ADD COLUMN id2 varchar(36);
UPDATE HarvestHistory SET id2 = id;
ALTER TABLE HarvestHistory DROP COLUMN id CASCADE;
ALTER TABLE HarvestHistory RENAME COLUMN id2 TO id;
ALTER TABLE HarvestHistory ADD PRIMARY KEY (id);



ALTER TABLE Metadata ADD COLUMN id2 varchar(36);
UPDATE Metadata SET id2 = id;
ALTER TABLE Metadata DROP COLUMN id CASCADE;
ALTER TABLE Metadata RENAME COLUMN id2 TO id;
ALTER TABLE Metadata ADD PRIMARY KEY (id);

ALTER TABLE Metadata ADD COLUMN owner2 varchar(36);
UPDATE Metadata SET owner2 = owner;
ALTER TABLE Metadata DROP COLUMN owner CASCADE;
ALTER TABLE Metadata RENAME COLUMN owner2 TO owner;
ALTER TABLE Metadata ADD CONSTRAINT metadata_owner_fkey FOREIGN KEY (owner) REFERENCES Users (id);

ALTER TABLE Validation ADD COLUMN metadataId2 varchar(36);
UPDATE Validation SET metadataId2 = metadataId;
ALTER TABLE Validation DROP COLUMN metadataId CASCADE;
ALTER TABLE Validation RENAME COLUMN metadataId2 TO metadataId;
ALTER TABLE Validation ADD PRIMARY KEY (metadataId, valType);
ALTER TABLE Validation ADD CONSTRAINT validation_metadataid_fkey FOREIGN KEY (metadataId) REFERENCES Metadata (id);



ALTER TABLE MetadataCateg ADD COLUMN metadataId2 varchar(36);
UPDATE MetadataCateg SET metadataId2 = metadataId;
ALTER TABLE MetadataCateg DROP COLUMN metadataId CASCADE;
ALTER TABLE MetadataCateg RENAME COLUMN metadataId2 TO metadataId;

ALTER TABLE MetadataCateg ADD CONSTRAINT metadatacateg_metadataid_fkey FOREIGN KEY (metadataId) REFERENCES Metadata (id);

ALTER TABLE MetadataCateg ADD COLUMN categoryId2 varchar(36);
UPDATE MetadataCateg SET categoryId2 = categoryId;
ALTER TABLE MetadataCateg DROP COLUMN categoryId CASCADE;
ALTER TABLE MetadataCateg RENAME COLUMN categoryId2 TO categoryId;

ALTER TABLE MetadataCateg ADD PRIMARY KEY (metadataId, categoryId);

ALTER TABLE MetadataCateg ADD CONSTRAINT metadatacateg_categoryid_fkey FOREIGN KEY (categoryId) REFERENCES Categories (id);



ALTER TABLE StatusValues ADD COLUMN id2 varchar(36);
UPDATE StatusValues SET id2 = id;
ALTER TABLE StatusValues DROP COLUMN id CASCADE;
ALTER TABLE StatusValues RENAME COLUMN id2 TO id;
ALTER TABLE StatusValues ADD PRIMARY KEY (id);


ALTER TABLE StatusValuesDes ADD COLUMN iddes2 varchar(36);
UPDATE StatusValuesDes SET iddes2 = iddes;
ALTER TABLE StatusValuesDes DROP COLUMN iddes;
ALTER TABLE StatusValuesDes RENAME COLUMN iddes2 TO iddes;
ALTER TABLE StatusValuesDes ADD PRIMARY KEY (iddes, langid);
ALTER TABLE StatusValuesDes ADD CONSTRAINT statusvaluesdes_iddes_fkey FOREIGN KEY (iddes) REFERENCES StatusValues (id);



ALTER TABLE MetadataStatus ADD COLUMN metadataId2 varchar(36);
UPDATE MetadataStatus SET metadataId2 = metadataId;
ALTER TABLE MetadataStatus DROP COLUMN metadataId CASCADE;
ALTER TABLE MetadataStatus RENAME COLUMN metadataId2 TO metadataId;

ALTER TABLE MetadataStatus ADD COLUMN statusId2 varchar(36);
UPDATE MetadataStatus SET statusId2 = statusId;
ALTER TABLE MetadataStatus DROP COLUMN statusId CASCADE;
ALTER TABLE MetadataStatus RENAME COLUMN statusId2 TO statusId;

ALTER TABLE MetadataStatus ADD COLUMN userId2 varchar(36);
UPDATE MetadataStatus SET userId2 = userId;
ALTER TABLE MetadataStatus DROP COLUMN userId CASCADE;
ALTER TABLE MetadataStatus RENAME COLUMN userId2 TO userId;

ALTER TABLE MetadataStatus ADD PRIMARY KEY (metadataId,statusId,userId,changeDate);
ALTER TABLE MetadataStatus ADD CONSTRAINT metadatastatus_metadataid_fkey FOREIGN KEY (metadataId) REFERENCES Metadata (id);
ALTER TABLE MetadataStatus ADD CONSTRAINT metadatastatus_statusid_fkey FOREIGN KEY (statusId) REFERENCES StatusValues (id);
ALTER TABLE MetadataStatus ADD CONSTRAINT metadatastatus_userid_fkey FOREIGN KEY (userId) REFERENCES Users (id);


ALTER TABLE OperationAllowed ADD COLUMN metadataId2 varchar(36);
UPDATE OperationAllowed SET metadataId2 = metadataId;
ALTER TABLE OperationAllowed DROP COLUMN metadataId CASCADE;
ALTER TABLE OperationAllowed RENAME COLUMN metadataId2 TO metadataId;

ALTER TABLE OperationAllowed ADD COLUMN groupId2 varchar(36);
UPDATE OperationAllowed SET groupId2 = groupId;
ALTER TABLE OperationAllowed DROP COLUMN groupId CASCADE;
ALTER TABLE OperationAllowed RENAME COLUMN groupId2 TO groupId;

ALTER TABLE OperationAllowed ADD COLUMN operationId2 varchar(36);
UPDATE OperationAllowed SET operationId2 = operationId;
ALTER TABLE OperationAllowed DROP COLUMN operationId CASCADE;
ALTER TABLE OperationAllowed RENAME COLUMN operationId2 TO operationId;

ALTER TABLE OperationAllowed ADD PRIMARY KEY (groupId,metadataId,operationId);
ALTER TABLE OperationAllowed ADD CONSTRAINT operationallowed_metadataid_fkey FOREIGN KEY (metadataId) REFERENCES Metadata (id);
ALTER TABLE OperationAllowed ADD CONSTRAINT operationallowed_groupid_fkey FOREIGN KEY (groupId) REFERENCES Groups (id);
ALTER TABLE OperationAllowed ADD CONSTRAINT operationallowed_operationid_fkey FOREIGN KEY (operationId) REFERENCES Operations (id);

ALTER TABLE MetadataRating ADD COLUMN metadataId2 varchar(36);
UPDATE MetadataRating SET metadataId2 = metadataId;
ALTER TABLE MetadataRating DROP COLUMN metadataId CASCADE;
ALTER TABLE MetadataRating RENAME COLUMN metadataId2 TO metadataId;

ALTER TABLE MetadataRating ADD PRIMARY KEY (metadataId,ipAddress);
ALTER TABLE MetadataRating ADD CONSTRAINT metadatarating_metadataid_fkey FOREIGN KEY (metadataId) REFERENCES Metadata (id);


ALTER TABLE MetadataNotifiers ADD COLUMN id2 varchar(36);
UPDATE MetadataNotifiers SET id2 = id;
ALTER TABLE MetadataNotifiers DROP COLUMN id CASCADE;
ALTER TABLE MetadataNotifiers RENAME COLUMN id2 TO id;

ALTER TABLE MetadataNotifiers ADD PRIMARY KEY (id);


ALTER TABLE MetadataNotifications ADD COLUMN metadataId2 varchar(36);
UPDATE MetadataNotifications SET metadataId2 = metadataId;
ALTER TABLE MetadataNotifications DROP COLUMN metadataId CASCADE;
ALTER TABLE MetadataNotifications RENAME COLUMN metadataId2 TO metadataId;

ALTER TABLE MetadataNotifications ADD COLUMN notifierId2 varchar(36);
UPDATE MetadataNotifications SET notifierId2 = notifierId;
ALTER TABLE MetadataNotifications DROP COLUMN notifierId CASCADE;
ALTER TABLE MetadataNotifications RENAME COLUMN notifierId2 TO notifierId;

ALTER TABLE MetadataNotifications ADD PRIMARY KEY (metadataId,notifierId);
ALTER TABLE MetadataNotifications ADD CONSTRAINT metadatanotifications_notifierid_fkey FOREIGN KEY (notifierId) REFERENCES MetadataNotifiers (id);

INSERT INTO Settings VALUES (89,80,'bind',NULL);
INSERT INTO Settings VALUES (102,86,'subtree','false');
INSERT INTO Settings VALUES (140,89,'bindDn','cn=fake.name,ou=people,dc=fao,dc=org');
INSERT INTO Settings VALUES (141,89,'bindPw','fake_password');
INSERT INTO Settings VALUES (150,80,'anonBind','true');

INSERT INTO Settings VALUES (956,1,'clustering',NULL);
INSERT INTO Settings VALUES (957,956,'enable','false');
INSERT INTO Settings VALUES (958,956,'jmsurl','failover://tcp://localhost:61616');

INSERT INTO Settings VALUES (959,1,'symbolicLocking',NULL);
INSERT INTO Settings VALUES (960,959,'enable','false');

ALTER TABLE HarvestHistory ADD elapsedTime int;

ALTER TABLE Metadata ADD isLocked char(1) default 'n' not null;

UPDATE Settings SET value='2.9.0' WHERE name='version';
UPDATE Settings SET value='0' WHERE name='subVersion';
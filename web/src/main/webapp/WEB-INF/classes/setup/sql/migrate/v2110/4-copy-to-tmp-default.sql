-- Copy data from a table that needs a column migrated to an enum to a temporary table

-- Update UserGroups profiles to be one of the enumerated profiles
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 0 FROM USERGROUPS where profile='Administrator';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 1 FROM USERGROUPS where profile='UserAdmin';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 2 FROM USERGROUPS where profile='Reviewer';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 3 FROM USERGROUPS where profile='Editor';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 4 FROM USERGROUPS where profile='RegisteredUser';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 5 FROM USERGROUPS where profile='Guest';
INSERT INTO USERGROUPS_TMP (userid, groupid, profile) SELECT userid, groupid, 6 FROM USERGROUPS where profile='Monitor';

-- Convert Profile column to the profile enumeration ordinal
-- create address and email tables to allow multiple per user

INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 0, organisation, kind, security, authtype FROM USERS where profile='Administrator';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 1, organisation, kind, security, authtype FROM USERS where profile='UserAdmin';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 2, organisation, kind, security, authtype FROM USERS where profile='Reviewer';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 3, organisation, kind, security, authtype FROM USERS where profile='Editor';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 4, organisation, kind, security, authtype FROM USERS where profile='RegisteredUser';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 5, organisation, kind, security, authtype FROM USERS where profile='Guest';
INSERT INTO USERS_TMP SELECT id, username, password, surname, name, 6, organisation, kind, security, authtype FROM USERS where profile='Monitor';

-- ----  Change notifier actions column to map to the MetadataNotificationAction enumeration

INSERT INTO MetadataNotifications_Tmp SELECT metadataId, notifierId, notified, metadataUuid, 0, errormsg FROM MetadataNotifications where action='u';
INSERT INTO MetadataNotifications_Tmp SELECT metadataId, notifierId, notified, metadataUuid, 1, errormsg FROM MetadataNotifications where action='d';

-- ----  Change params querytype column to map to the LuceneQueryParamType enumeration

INSERT INTO Params_TEMP SELECT id, requestId, 0, termField, termText, similarity, lowerText, upperText, inclusive FROM Params where querytype='BOOLEAN_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 1, termField, termText, similarity, lowerText, upperText, inclusive FROM Params where querytype='TERM_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 2, termField, termText, similarity, lowerText, upperText, inclusive FROM Params where querytype='FUZZY_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 3, termField, termText, similarity, lowerText, upperText, inclusive FROM Params where querytype='PREFIX_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 4, termField, termText, similarity, lowerText, upperText, inclusive FROM Params where querytype='MATCH_ALL_DOCS_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 5, termField, termText, similarity, lowerText, upperText, inclusive FROM Params where querytype='WILDCARD_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 6, termField, termText, similarity, lowerText, upperText, inclusive FROM Params where querytype='PHRASE_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 7, termField, termText, similarity, lowerText, upperText, inclusive FROM Params where querytype='RANGE_QUERY';
INSERT INTO Params_TEMP SELECT id, requestId, 8, termField, termText, similarity, lowerText, upperText, inclusive FROM Params where querytype='NUMERIC_RANGE_QUERY';

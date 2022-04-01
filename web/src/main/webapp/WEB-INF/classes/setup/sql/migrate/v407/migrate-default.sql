INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/csvReport/csvName', 'metadata_{datetime}.csv', 0, 12607, 'n');

UPDATE Settings set position = 7213 WHERE name = 'system/inspire/remotevalidation/nodeid';
UPDATE Settings set position = 7214 WHERE name = 'system/inspire/remotevalidation/apikey';
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/urlquery', '', 0, 7212, 'n');

INSERT INTO Users (id, username, password, name, surname, profile, kind, organisation, security, authtype, isenabled) VALUES  (0,'nobody','','nobody','nobody',4,'','','','', 'n');
INSERT INTO Address (id, address, city, country, state, zip) VALUES  (0, '', '', '', '', '');
INSERT INTO UserAddress (userid, addressid) VALUES  (0, 0);


UPDATE Settings SET value='4.0.7' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/nodeid', '', 0, 7212, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/inspire/remotevalidation/apikey', '', 0, 7213, 'y');

DELETE FROM Settings WHERE name = 'system/server/securePort';

INSERT INTO Users (id, username, password, name, surname, profile, kind, organisation, security, authtype, isenabled) VALUES  (0,'nobody','','nobody','nobody',4,'','','','', 'n');
INSERT INTO Address (id, address, city, country, state, zip) VALUES  (0, '', '', '', '', '');
INSERT INTO UserAddress (userid, addressid) VALUES  (0, 0);

UPDATE Settings SET value='4.0.6' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

--Inserts new data and modifies data

ALTER TABLE operations DROP COLUMN reserved;
ALTER TABLE ServiceParameters DROP CONSTRAINT IF EXISTS serviceparameters_service_fkey;
ALTER TABLE ServiceParameters DROP COLUMN IF EXISTS id;


ALTER TABLE Settings ALTER name TYPE varchar(512);

-- 0 is char, 1 is number, 2 is boolean
ALTER TABLE Settings ADD datatype int;
ALTER TABLE Settings ADD position int;
ALTER TABLE Settings ADD internal varchar(1);

UPDATE Settings SET position = id * 10;

UPDATE Settings SET name = 'system/site/name', datatype = 0, internal = 'n' WHERE id = 11;
UPDATE Settings SET name = 'system/site/siteId', datatype = 0, internal = 'n' WHERE id = 12;
UPDATE Settings SET name = 'system/site/organization', datatype = 0, internal = 'n' WHERE id = 13;
UPDATE Settings SET name = 'system/platform/version', datatype = 0, internal = 'n' WHERE id = 15;
UPDATE Settings SET name = 'system/platform/subVersion', datatype = 0, internal = 'n' WHERE id = 16;
UPDATE Settings SET name = 'system/site/svnUuid', datatype = 0 WHERE id = 17;
UPDATE Settings SET name = 'system/server/host', datatype = 0, internal = 'n' WHERE id = 21;
UPDATE Settings SET name = 'system/server/port', datatype = 1, internal = 'n' WHERE id = 22;
UPDATE Settings SET name = 'system/server/protocol', datatype = 0, internal = 'n' WHERE id = 23;
UPDATE Settings SET name = 'system/server/securePort', datatype = 1 WHERE id = 24;
UPDATE Settings SET name = 'system/intranet/network', datatype = 0 WHERE id = 31;
UPDATE Settings SET name = 'system/intranet/netmask', datatype = 0 WHERE id = 32;
UPDATE Settings SET name = 'system/z3950/enable', datatype = 2 WHERE id = 41;
UPDATE Settings SET name = 'system/z3950/port', datatype = 1 WHERE id = 42;
UPDATE Settings SET name = 'system/proxy/use', datatype = 2 WHERE id = 51;
UPDATE Settings SET name = 'system/proxy/host', datatype = 0 WHERE id = 52;
UPDATE Settings SET name = 'system/proxy/port', datatype = 1 WHERE id = 53;
UPDATE Settings SET name = 'system/proxy/username', datatype = 0 WHERE id = 54;
UPDATE Settings SET name = 'system/proxy/password', datatype = 0 WHERE id = 55;
UPDATE Settings SET name = 'system/feedback/email', datatype = 0 WHERE id = 61;
UPDATE Settings SET name = 'system/feedback/mailServer/host', datatype = 0 WHERE id = 63;
UPDATE Settings SET name = 'system/feedback/mailServer/port', datatype = 1 WHERE id = 64;
UPDATE Settings SET name = 'system/removedMetadata/dir', datatype = 0 WHERE id = 71;
UPDATE Settings SET name = 'system/selectionmanager/maxrecords', datatype = 1 WHERE id = 91;
UPDATE Settings SET name = 'system/csw/enable', datatype = 2 WHERE id = 121;
UPDATE Settings SET name = 'system/csw/contactId', datatype = 0 WHERE id = 122;
UPDATE Settings SET name = 'system/csw/metadataPublic', datatype = 2 WHERE id = 131;


UPDATE Settings SET name = 'system/shib/use', datatype = 2 WHERE id = 171;
UPDATE Settings SET name = 'system/shib/path', datatype = 0 WHERE id = 172;
UPDATE Settings SET name = 'system/shib/username', datatype = 0 WHERE id = 174;
UPDATE Settings SET name = 'system/shib/surname', datatype = 0 WHERE id = 175;
UPDATE Settings SET name = 'system/shib/firstname', datatype = 0 WHERE id = 176;
UPDATE Settings SET name = 'system/shib/profile', datatype = 0 WHERE id = 177;
UPDATE Settings SET name = 'system/userSelfRegistration/enable', datatype = 2, internal = 'n' WHERE id = 191;
UPDATE Settings SET name = 'system/clickablehyperlinks/enable', datatype = 2 WHERE id = 201;
UPDATE Settings SET name = 'system/localrating/enable', datatype = 2 WHERE id = 211;
UPDATE Settings SET name = 'system/downloadservice/leave', datatype = 0 WHERE id = 221;
UPDATE Settings SET name = 'system/downloadservice/simple', datatype = 0 WHERE id = 222;
UPDATE Settings SET name = 'system/downloadservice/withdisclaimer', datatype = 0 WHERE id = 223;
UPDATE Settings SET name = 'system/xlinkResolver/enable', datatype = 2, internal = 'n' WHERE id = 231;
UPDATE Settings SET name = 'system/autofixing/enable', datatype = 2 WHERE id = 241;
UPDATE Settings SET name = 'system/searchStats/enable', datatype = 2, internal = 'n' WHERE id = 251;
UPDATE Settings SET name = 'system/indexoptimizer/enable', datatype = 2 WHERE id = 601;
UPDATE Settings SET name = 'system/indexoptimizer/at/hour', datatype = 1 WHERE id = 603;
UPDATE Settings SET name = 'system/indexoptimizer/at/min', datatype = 1 WHERE id = 604;
UPDATE Settings SET name = 'system/indexoptimizer/at/sec', datatype = 1 WHERE id = 605;
UPDATE Settings SET name = 'system/indexoptimizer/interval', datatype = 0 WHERE id = 606;
UPDATE Settings SET name = 'system/indexoptimizer/interval/day', datatype = 1 WHERE id = 607;
UPDATE Settings SET name = 'system/indexoptimizer/interval/hour', datatype = 1 WHERE id = 608;
UPDATE Settings SET name = 'system/indexoptimizer/interval/min', datatype = 1 WHERE id = 609;
UPDATE Settings SET name = 'system/oai/mdmode', datatype = 0 WHERE id = 701;
UPDATE Settings SET name = 'system/oai/tokentimeout', datatype = 1 WHERE id = 702;
UPDATE Settings SET name = 'system/oai/cachesize', datatype = 1 WHERE id = 703;
UPDATE Settings SET name = 'system/inspire/enable', datatype = 2 WHERE id = 721;
UPDATE Settings SET name = 'system/inspire/enableSearchPanel', datatype = 2, internal = 'n' WHERE id = 722;
UPDATE Settings SET name = 'system/harvester/enableEditing', datatype = 2, internal = 'n' WHERE id = 901;
UPDATE Settings SET name = 'system/metadata/enableSimpleView', datatype = 2 WHERE id = 911;
UPDATE Settings SET name = 'system/metadata/enableIsoView', datatype = 2 WHERE id = 912;
UPDATE Settings SET name = 'system/metadata/enableInspireView', datatype = 2 WHERE id = 913;
UPDATE Settings SET name = 'system/metadata/enableXmlView', datatype = 2 WHERE id = 914;
UPDATE Settings SET name = 'system/metadata/defaultView', datatype = 0, internal = 'n' WHERE id = 915;
UPDATE Settings SET name = 'system/metadataprivs/usergrouponly', datatype = 2, internal = 'n' WHERE id = 918;
UPDATE Settings SET name = 'system/threadedindexing/maxthreads', datatype = 1 WHERE id = 921;
UPDATE Settings SET name = 'system/autodetect/enable', datatype = 2 WHERE id = 951;
UPDATE Settings SET name = 'system/requestedLanguage/only', datatype = 0 WHERE id = 953;
UPDATE Settings SET name = 'system/requestedLanguage/sorted', datatype = 2 WHERE id = 954;


DELETE FROM Settings WHERE id = 957;
DELETE FROM Settings WHERE id = 958;


UPDATE Settings SET internal = 'y' WHERE internal IS NULL;
UPDATE Settings SET parentId = null;

DELETE FROM Settings WHERE id = 173;
DELETE FROM Settings WHERE id = 178;
DELETE FROM Settings WHERE id = 179;
DELETE FROM Settings WHERE id = 180;
DELETE FROM Settings WHERE id = 181;
DELETE FROM Settings WHERE id = 182;
DELETE FROM Settings WHERE id = 183;
DELETE FROM Settings WHERE id = 184;

DELETE FROM Settings WHERE id = 120;
DELETE FROM Settings WHERE id = 170;
DELETE FROM Settings WHERE id = 190;
DELETE FROM Settings WHERE id = 200;
DELETE FROM Settings WHERE id = 210;
DELETE FROM Settings WHERE id = 220;
DELETE FROM Settings WHERE id = 230;
DELETE FROM Settings WHERE id = 240;
DELETE FROM Settings WHERE id = 250;
DELETE FROM Settings WHERE id = 600;
DELETE FROM Settings WHERE id = 602;
DELETE FROM Settings WHERE id = 700;
DELETE FROM Settings WHERE id = 720;
DELETE FROM Settings WHERE id = 900;
DELETE FROM Settings WHERE id = 910;
DELETE FROM Settings WHERE id = 917;
DELETE FROM Settings WHERE id = 920;
DELETE FROM Settings WHERE id = 950;
DELETE FROM Settings WHERE id = 952;
DELETE FROM Settings WHERE id = 956;

DELETE FROM Settings WHERE id = 10;
DELETE FROM Settings WHERE id = 14;
DELETE FROM Settings WHERE id = 20;
DELETE FROM Settings WHERE id = 30;
DELETE FROM Settings WHERE id = 40;
DELETE FROM Settings WHERE id = 50;
DELETE FROM Settings WHERE id = 62;
DELETE FROM Settings WHERE id = 60;
DELETE FROM Settings WHERE id = 70;
DELETE FROM Settings WHERE id = 90;

DELETE FROM Settings WHERE id = 0;
DELETE FROM Settings WHERE id = 1;

ALTER TABLE Settings DROP COLUMN parentId;
ALTER TABLE Settings DROP COLUMN id;
ALTER TABLE Settings ADD PRIMARY KEY (name);

-- Add new settings
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/username', '', 0, 642, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/password', '', 0, 643, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/ssl', false, 2, 641, 'y');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/recipient', NULL, 0, 9020, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/template', '', 0, 9021, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/templateError', 'There was an error on the harvesting: $$errorMsg$$', 0, 9022, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/templateWarning', '', 0, 9023, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/subject', '[$$harvesterType$$] $$harvesterName$$ finished harvesting', 0, 9024, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/enabled', 'false', 2, 9025, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/level1', 'false', 2, 9026, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/level2', 'false', 2, 9027, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvesting/mail/level3', 'false', 2, 9028, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/requestedLanguage/ignorechars', '', 0, 9590, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userFeedback/enable', 'true', 2, 1911, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/requestedLanguage/preferUiLanguage', 'true', 2, 9595, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/csw/transactionUpdateCreateXPath', 'true', 2, 1320, 'n');


-- INSERT INTO Settings (name, value, datatype, position, internal) VALUES
--  ('map/backgroundChoices', '{"contextList": []}', 0, 9590, false);
INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('map/config', '{"useOSM":false,"context":"","layer":{"url":"http://www2.demis.nl/mapserver/wms.asp?","layers":"Countries","version":"1.1.1"},"projection":"EPSG:4326","projectionList":["EPSG:4326","EPSG:2154","EPSG:3857"]}', 0, 9590, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES 
  ('map/proj4js', '[{"code":"EPSG:2154","value":"+proj=lcc +lat_1=49 +lat_2=44 +lat_0=46.5 +lon_0=3 +x_0=700000 +y_0=6600000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"}]', 0, 9591, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('metadata/editor/schemaConfig', '{"iso19110":{"defaultTab":"default","displayToolTip":false,"related":{"display":true,"readonly":true,"categories":["dataset"]},"validation":{"display":true}},"iso19139":{"defaultTab":"inspire","displayToolTip":false,"related":{"display":true,"categories":[]},"suggestion":{"display":true},"validation":{"display":true}},"dublin-core":{"defaultTab":"default","related":{"display":true,"readonly":false,"categories":["parent","onlinesrc"]},}}', 0, 10000, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/resourceIdentifierPrefix', 'http://localhost:8080/geonetwork/', 0, 10001, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/localXlinkEnable', 'true', 2, 2311, 'n');

ALTER TABLE StatusValues ADD displayorder int;

UPDATE StatusValues SET displayorder = 0 WHERE id = 0;
UPDATE StatusValues SET displayorder = 1 WHERE id = 1;
UPDATE StatusValues SET displayorder = 3 WHERE id = 2;
UPDATE StatusValues SET displayorder = 5 WHERE id = 3;
UPDATE StatusValues SET displayorder = 2 WHERE id = 4;
UPDATE StatusValues SET displayorder = 4 WHERE id = 5;


-- Version update
UPDATE Settings SET value='2.11.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- Populate new tables from Users
INSERT INTO Address (SELECT id, address, city, state, zip, country FROM Users);
INSERT INTO UserAddress (SELECT id, id FROM Users);
INSERT INTO Email (SELECT id, email FROM Users);


CREATE SEQUENCE HIBERNATE_SEQUENCE START WITH 1 INCREMENT BY 1;



-- Update Requests column type (integer > boolean)
ALTER TABLE Requests ADD COLUMN autogeneratedtemp boolean;
UPDATE Requests SET autogeneratedtemp = false;
UPDATE Requests SET autogeneratedtemp = true WHERE autogenerated = 1;
ALTER TABLE Requests DROP COLUMN autogenerated;
ALTER TABLE Requests ADD COLUMN autogenerated boolean;
UPDATE Requests SET autogeneratedtemp = autogenerated;
ALTER TABLE Requests DROP COLUMN autogeneratedtemp;

ALTER TABLE Requests ADD COLUMN simpletemp boolean;
UPDATE Requests SET simpletemp = false;
UPDATE Requests SET simpletemp = true WHERE simple = 1;
ALTER TABLE Requests DROP COLUMN simple;
ALTER TABLE Requests ADD COLUMN simple boolean;
UPDATE Requests SET simpletemp = simple;
ALTER TABLE Requests DROP COLUMN simpletemp;

UPDATE HarvestHistory SET elapsedTime = 0 WHERE elapsedTime IS NULL;

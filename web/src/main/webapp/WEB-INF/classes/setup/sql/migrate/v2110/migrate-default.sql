INSERT INTO HarvesterSettings VALUES  (1,NULL,'harvesting',NULL);
-- Copy all harvester's root nodes config
INSERT INTO HarvesterSettings SELECT id, 1, name, value FROM Settings WHERE parentId = 2;
-- Copy all harvester's properties (Greater than last 2.10.0 settings ie. keepMarkedElement)
INSERT INTO HarvesterSettings SELECT * FROM Settings WHERE id > (SELECT max(id) FROM HarvesterSettings);
-- Drop harvester config from Settings table
DELETE FROM Settings WHERE id > 958;
DELETE FROM Settings WHERE id=2;


ALTER TABLE Settings ALTER name TYPE varchar(512);

-- 0 is char, 1 is number, 2 is boolean
ALTER TABLE Settings ADD COLUMN datatype int;
ALTER TABLE Settings ADD COLUMN position int;

UPDATE Settings SET position = id * 10;

UPDATE Settings SET name = 'system/site/name', datatype = 0 WHERE id = 11;
UPDATE Settings SET name = 'system/site/siteId', datatype = 0 WHERE id = 12;
UPDATE Settings SET name = 'system/site/organization', datatype = 0 WHERE id = 13;
UPDATE Settings SET name = 'system/platform/version', datatype = 0 WHERE id = 15;
UPDATE Settings SET name = 'system/platform/subVersion', datatype = 0 WHERE id = 16;
UPDATE Settings SET name = 'system/site/svnUuid', datatype = 0 WHERE id = 17;
UPDATE Settings SET name = 'system/server/host', datatype = 0 WHERE id = 21;
UPDATE Settings SET name = 'system/server/port', datatype = 1 WHERE id = 22;
UPDATE Settings SET name = 'system/server/protocol', datatype = 0 WHERE id = 23;
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
UPDATE Settings SET name = 'system/userSelfRegistration/enable', datatype = 2 WHERE id = 191;
UPDATE Settings SET name = 'system/clickablehyperlinks/enable', datatype = 2 WHERE id = 201;
UPDATE Settings SET name = 'system/localrating/enable', datatype = 2 WHERE id = 211;
UPDATE Settings SET name = 'system/downloadservice/leave', datatype = 0 WHERE id = 221;
UPDATE Settings SET name = 'system/downloadservice/simple', datatype = 0 WHERE id = 222;
UPDATE Settings SET name = 'system/downloadservice/withdisclaimer', datatype = 0 WHERE id = 223;
UPDATE Settings SET name = 'system/xlinkResolver/enable', datatype = 2 WHERE id = 231;
UPDATE Settings SET name = 'system/autofixing/enable', datatype = 2 WHERE id = 241;
UPDATE Settings SET name = 'system/searchStats/enable', datatype = 2 WHERE id = 251;
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
UPDATE Settings SET name = 'system/inspire/enableSearchPanel', datatype = 2 WHERE id = 722;
UPDATE Settings SET name = 'system/harvester/enableEditing', datatype = 2 WHERE id = 901;
UPDATE Settings SET name = 'system/metadata/enableSimpleView', datatype = 2 WHERE id = 911;
UPDATE Settings SET name = 'system/metadata/enableIsoView', datatype = 2 WHERE id = 912;
UPDATE Settings SET name = 'system/metadata/enableInspireView', datatype = 2 WHERE id = 913;
UPDATE Settings SET name = 'system/metadata/enableXmlView', datatype = 2 WHERE id = 914;
UPDATE Settings SET name = 'system/metadata/defaultView', datatype = 0 WHERE id = 915;
UPDATE Settings SET name = 'system/metadataprivs/usergrouponly', datatype = 2 WHERE id = 918;
UPDATE Settings SET name = 'system/threadedindexing/maxthreads', datatype = 1 WHERE id = 921;
UPDATE Settings SET name = 'system/autodetect/enable', datatype = 2 WHERE id = 951;
UPDATE Settings SET name = 'system/requestedLanguage/only', datatype = 0 WHERE id = 953;
UPDATE Settings SET name = 'system/requestedLanguage/sorted', datatype = 2 WHERE id = 954;
UPDATE Settings SET name = 'system/hidewithheldelements/enable', datatype = 2 WHERE id = 957;
UPDATE Settings SET name = 'system/hidewithheldelements/keepMarkedElement', datatype = 2 WHERE id = 958;

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


-- Version update
UPDATE Settings SET value='2.11.0' WHERE name='version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='subVersion';

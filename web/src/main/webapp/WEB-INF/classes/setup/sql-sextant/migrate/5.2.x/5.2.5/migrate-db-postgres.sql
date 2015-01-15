
-- See https://github.com/geonetwork/core-geonetwork/commit/8ec082bc48c613b53acb06963d29c9a734482ba9
-- withheld element filter is now configured as a per schema basis
DELETE FROM Settings WHERE name = 'system/hidewithheldelements/enable';
DELETE FROM Settings WHERE name = 'system/hidewithheldelements/keepMarkedElement';
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/hidewithheldelements/enableLogging', 'true', 2, 2320, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/localXlinkEnable', 'true', 2, 2311, 'n');
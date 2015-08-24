
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/workflow/draftWhenInGroup', 'MYOCEAN-CORE-PRODUCTS|SEADATANET|EMODNET_Chemistry', 0, 100002, 'n');


UPDATE Settings SET value='3.0.2' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';
UPDATE Settings SET internal = 'n' WHERE name = 'system/metadataprivs/usergrouponly';

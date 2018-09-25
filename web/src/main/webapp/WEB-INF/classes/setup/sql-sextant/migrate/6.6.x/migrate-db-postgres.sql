-- MyOcean
UPDATE metadata SET schemaid = 'iso19139' WHERE schemaid = 'iso19139.myocean';
UPDATE metadata SET schemaid = 'iso19139' WHERE schemaid = 'iso19139.myocean.short';

UPDATE Settings SET value='3.6.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

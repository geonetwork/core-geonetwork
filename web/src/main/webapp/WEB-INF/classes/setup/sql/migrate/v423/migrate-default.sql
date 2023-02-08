UPDATE Settings SET position = '9165' WHERE name = 'system/server/sitemapLinkUrl';
UPDATE Settings SET name = 'metadata/url/sitemapLinkUrl' WHERE name = 'system/server/sitemapLinkUrl';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/url/sitemapDoiFirst', 'false', 2, 9166, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/url/dynamicAppLinkUrl', NULL, 0, 9167, 'y');


UPDATE harvestersettings SET value = replace(value, 'gmiTogmd', 'iso19139:convert/fromISO19115-2') WHERE value LIKE 'gmiTogmd%';
UPDATE harvestersettings SET value = replace(value, 'DIF-to-ISO19139', 'iso19139:convert/fromDIF-GCMD') WHERE value LIKE 'DIF-to-ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'EsriGeosticker-to-ISO19139', 'iso19139:convert/fromESRI-Geosticker') WHERE value LIKE 'EsriGeosticker-to-ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'ISO19115-to-ISO19139', 'iso19139:convert/fromISO19115') WHERE value LIKE 'ISO19115-to-ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'OGCCSWGetCapabilities-to-ISO19119_ISO19139', 'iso19139:convert/fromOGCCSWGetCapabilities') WHERE value LIKE 'OGCCSWGetCapabilities-to-ISO19119_ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'OGCSLD-to-ISO19139', 'iso19139:convert/fromOGCSLD') WHERE value LIKE 'OGCSLD-to-ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'OGCSOSGetCapabilities-to-ISO19119_ISO19139', 'iso19139:convert/fromOGCSOSGetCapabilities') WHERE value LIKE 'OGCSOSGetCapabilities-to-ISO19119_ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'OGCWCSGetCapabilities-to-ISO19119_ISO19139', 'iso19139:convert/fromOGCWCSGetCapabilities') WHERE value LIKE 'OGCWCSGetCapabilities-to-ISO19119_ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'OGCWFSGetCapabilities-to-ISO19119_ISO19139', 'iso19139:convert/fromOGCWFSGetCapabilities') WHERE value LIKE 'OGCWFSGetCapabilities-to-ISO19119_ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'OGCWMC-OR-OWSC-to-ISO19139', 'iso19139:convert/fromOGCWMC-OR-OWSC') WHERE value LIKE 'OGCWMC-OR-OWSC-to-ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'OGCWMSGetCapabilities-to-ISO19119_ISO19139', 'iso19139:convert/fromOGCWMSGetCapabilities') WHERE value LIKE 'OGCWMSGetCapabilities-to-ISO19119_ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'OGCWPSGetCapabilities-to-ISO19119_ISO19139', 'iso19139:convert/fromOGCWPSGetCapabilities') WHERE value LIKE 'OGCWPSGetCapabilities-to-ISO19119_ISO19139%';
UPDATE harvestersettings SET value = replace(value, 'OGCWxSGetCapabilities-to-ISO19119_ISO19139', 'iso19139:convert/fromOGCWxSGetCapabilities') WHERE value LIKE 'OGCWxSGetCapabilities-to-ISO19119_ISO19139%';

UPDATE harvestersettings SET value = replace(value, 'OGCWFSDescribeFeatureType-to-ISO19110', 'iso19110:convert/fromOGCWFSDescribeFeatureType') WHERE value LIKE 'OGCWFSDescribeFeatureType-to-ISO19110%';

UPDATE harvestersettings SET value = replace(value, 'CKAN-to-ISO19115-3-2018', 'iso19115-3.2018:convert/fromJsonCkan') WHERE value LIKE 'CKAN-to-ISO19115-3-2018%';
UPDATE harvestersettings SET value = replace(value, 'DKAN-to-ISO19115-3-2018', 'iso19115-3.2018:convert/fromJsonDkan') WHERE value LIKE 'DKAN-to-ISO19115-3-2018%';
UPDATE harvestersettings SET value = replace(value, 'ESRIDCAT-to-ISO19115-3-2018', 'iso19115-3.2018:convert/fromJsonLdEsri') WHERE value LIKE 'ESRIDCAT-to-ISO19115-3-2018%';
UPDATE harvestersettings SET value = replace(value, 'ISO19115-3-2014-to-ISO19115-3-2018', 'iso19115-3.2018:convert/fromISO19115-3.2014') WHERE value LIKE 'ISO19115-3-2014-to-ISO19115-3-2018%';
UPDATE harvestersettings SET value = replace(value, 'ISO19139-to-ISO19115-3-2018', 'iso19115-3.2018:convert/fromISO19139') WHERE value LIKE 'ISO19139-to-ISO19115-3-2018%';
UPDATE harvestersettings SET value = replace(value, 'ISO19139-to-ISO19115-3-2018-with-languages-refactor', 'iso19115-3.2018:convert/fromISO19139-with-languages-refactor') WHERE value LIKE 'ISO19139-to-ISO19115-3-2018-with-languages-refactor%';
UPDATE harvestersettings SET value = replace(value, 'OPENDATASOFT-to-ISO19115-3-2018', 'iso19115-3.2018:convert/fromJsonOpenDataSoft') WHERE value LIKE 'OPENDATASOFT-to-ISO19115-3-2018%';
UPDATE harvestersettings SET value = replace(value, 'SPARQL-DCAT-to-ISO19115-3-2018', 'iso19115-3.2018:convert/fromSPARQL-DCAT') WHERE value LIKE 'SPARQL-DCAT-to-ISO19115-3-2018%';
UPDATE harvestersettings SET value = replace(value, 'udata-to-ISO19115-3-2018', 'iso19115-3.2018:convert/fromJsonUdata') WHERE value LIKE 'udata-to-ISO19115-3-2018%';

DROP TABLE Thesaurus;


UPDATE Settings SET value='4.2.3' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('ui/config', '{"langDetector":{"fromHtmlTag":false,"regexp":"^/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+/([a-z]{3})/","default":"eng"},"nodeDetector":{"regexp":"^/[a-zA-Z0-9_-]+/([a-zA-Z0-9_-]+)/[a-z]{3}/","default":"srv"},"mods":{"header":{"enabled":true,"languages":{"eng":"en","dut":"nl","fre":"fr","ger":"ge","kor":"ko","spa":"es","cze":"cz","cat":"ca","fin":"fi","ice":"is","rus":"ru","chi":"zh"}},"home":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/home"},"search":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/search","hitsperpageValues":[10,50,100],"paginationInfo":{"hitsPerPage":20},"defaultSearchString":"","facetsSummaryType":"details","facetConfig":[],"facetTabField":"","filters":{},"sortbyValues":[{"sortBy":"relevance","sortOrder":""},{"sortBy":"changeDate","sortOrder":""},{"sortBy":"title","sortOrder":"reverse"},{"sortBy":"rating","sortOrder":""},{"sortBy":"popularity","sortOrder":""},{"sortBy":"denominatorDesc","sortOrder":""},{"sortBy":"denominatorAsc","sortOrder":"reverse"}],"sortBy":"relevance","resultViewTpls":[{"tplUrl":"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html","tooltip":"Grid","icon":"fa-th"}],"resultTemplate":"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html","formatter":{"list":[{"label":"full","url":"../api/records/{{uuid}}/formatters/xsl-view?root=div&view=advanced"}]},"grid":{"related":["parent","children","services","datasets"]},"linkTypes":{"links":["LINK","kml"],"downloads":["DOWNLOAD"],"layers":["OGC"],"maps":["ows"]},"isFilterTagsDisplayedInSearch":false},"defaultSearchString":"","map":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/map","is3DModeAllowed":true,"isSaveMapInCatalogAllowed":true,"isExportMapAsImageEnabled":true,"bingKey":"AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_p_0hkO1yo78ba8zTLARcLBIf8H6D","storage":"sessionStorage","map":"../../map/config-viewer.xml","listOfServices":{"wms":[],"wmts":[]},"useOSM":true,"context":"","layer":{"url":"http://www2.demis.nl/mapserver/wms.asp?","layers":"Countries","version":"1.1.1"},"projection":"EPSG:3857","projectionList":[{"code":"EPSG:4326","label":"WGS84(EPSG:4326)"},{"code":"EPSG:3857","label":"Googlemercator(EPSG:3857)"}],"disabledTools":{"processes":false,"addLayers":false,"layers":false,"filter":false,"contexts":false,"print":false,"mInteraction":false,"graticule":false,"syncAllLayers":false,"drawVector":false},"searchMapLayers":[],"viewerMapLayers":[]},"editor":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.edit","isUserRecordsOnly":false,"isFilterTagsDisplayed":false,"createPageTpl": "../../catalog/templates/editor/new-metadata-horizontal.html"},"admin":{"enabled":true,"appUrl":"../../srv/{{lang}}/admin.console"},"signin":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.signin"},"signout":{"appUrl":"../../signout"}}}', 3, 10000, 'n');


INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('system/csw/enabledWhenIndexing', 'true', 2, 1211, 'y');

DELETE FROM Settings WHERE  name = 'map/is3DModeAllowed';
DELETE FROM Settings WHERE  name = 'map/isMapViewerEnabled';
DELETE FROM Settings WHERE  name = 'map/config';
DELETE FROM Settings WHERE  name = 'map/proj4js';
DELETE FROM Settings WHERE  name = 'map/isSaveMapInCatalogAllowed';
DELETE FROM Settings WHERE  name = 'map/bingKey';

DELETE FROM Settings WHERE name like 'system/shib%';


INSERT INTO Selections (id, name, isWatchable) VALUES (0, 'PreferredList', 'n');
INSERT INTO Selections (id, name, isWatchable) VALUES (1, 'WatchList', 'y');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'ara','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'ara','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'cat','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'cat','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'chi','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'chi','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'dut','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'dut','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'eng','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'eng','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'fin','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'fin','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'fre','Fiches préférées');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'fre','Fiches observées');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'ger','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'ger','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'ita','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'ita','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'nor','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'nor','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'pol','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'pol','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'por','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'por','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'rus','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'rus','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'spa','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'spa','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'tur','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'tur','Watch list');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (0,'vie','Preferred records');
INSERT INTO SelectionsDes (iddes, langid, label) VALUES (1,'vie','Watch list');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userFeedback/lastNotificationDate', '', 0, 1912, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/import/restrict', '', 0, 11000, 'y');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/referencedDeletionAllowed', 'true', 2, 2313, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/backuparchive/enable', 'false', 2, 12000, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/recaptcha/enable', 'false', 2, 1910, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/recaptcha/publickey', '', 0, 1910, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/userSelfRegistration/recaptcha/secretkey', '', 0, 1910, 'y');


UPDATE Settings SET value='3.3.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

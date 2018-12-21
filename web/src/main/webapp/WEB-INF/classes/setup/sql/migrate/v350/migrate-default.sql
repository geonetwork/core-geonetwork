
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doienabled', 'false', 2, 191, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doiurl', '', 0, 192, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doiusername', '', 0, 193, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doipassword', '', 0, 194, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doikey', '', 0, 195, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/publication/doi/doilandingpagetemplate', 'http://localhost:8080/geonetwork/srv/resources/records/{{uuid}}', 0, 195, 'n');
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/metadata/history/enabled', 'false', 2, 9171, 'n');

UPDATE StatusValues SET type = 'workflow';

UPDATE StatusValues SET notificationLevel = 'recordUserAuthor' WHERE name = 'approved';
UPDATE StatusValues SET notificationLevel = 'recordUserAuthor' WHERE name = 'retired';
UPDATE StatusValues SET notificationLevel = 'recordProfileReviewer' WHERE name = 'submitted';
UPDATE StatusValues SET notificationLevel = 'recordUserAuthor' WHERE name = 'rejected';


INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (100,'doiCreationTask','n', 100, 'task', 'statusUserOwner');


INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (50,'recordcreated','y', 50, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (51,'recordupdated','y', 51, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (52,'attachmentadded','y', 52, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (53,'attachmentdeleted','y', 53, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (54,'recordownerchange','y', 54, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (55,'recordgroupownerchange','y', 55, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (56,'recordprivilegeschange','y', 56, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (57,'recordcategorychange','y', 57, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (58,'recordvalidationtriggered','y', 58, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (59,'recordstatuschange','y', 59, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (60,'recordprocessingchange','y', 60, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (61,'recorddeleted','y', 61, 'event', null);
INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (62,'recordimported','y', 62, 'event', null);

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/vcs/enable', 'false', 2, 9161, 'n');

UPDATE Settings SET value='3.5.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

UPDATE Settings set value = '{"langDetector":{"fromHtmlTag":false,"regexp":"^/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+/([a-z]{3})/","default":"eng"},"nodeDetector":{"regexp":"^/[a-zA-Z0-9_-]+/([a-zA-Z0-9_-]+)/[a-z]{3}/","default":"srv"},"mods":{"header":{"enabled":true,"languages":{"eng":"en","dut":"nl","fre":"fr","ger":"de","kor":"ko","spa":"es","cze":"cs","cat":"ca","fin":"fi","ice":"is", "rus": "ru", "chi": "zh"}},"home":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/home"},"search":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/search","hitsperpageValues":[10,50,100],"paginationInfo":{"hitsPerPage":20},"facetsSummaryType":"details","facetConfig":[],"facetTabField":"","filters":{},"sortbyValues":[{"sortBy":"relevance","sortOrder":""},{"sortBy":"changeDate","sortOrder":""},{"sortBy":"title","sortOrder":"reverse"},{"sortBy":"rating","sortOrder":""},{"sortBy":"popularity","sortOrder":""},{"sortBy":"denominatorDesc","sortOrder":""},{"sortBy":"denominatorAsc","sortOrder":"reverse"}],"sortBy":"relevance","resultViewTpls":[{"tplUrl":"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html","tooltip":"Grid","icon":"fa-th"}],"resultTemplate":"../../catalog/components/search/resultsview/partials/viewtemplates/grid.html","formatter":{"list":[{"label":"full","url":"../api/records/{{uuid}}/formatters/xsl-view?root=div&view=advanced"}]},"grid":{"related":["parent","children","services","datasets"]},"linkTypes":{"links":["LINK","kml"],"downloads":["DOWNLOAD"],"layers":["OGC"],"maps":["ows"]},"isFilterTagsDisplayedInSearch":false},"map":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/map","is3DModeAllowed":true,"isSaveMapInCatalogAllowed":true,"isExportMapAsImageEnabled":true,"bingKey":"AnElW2Zqi4fI-9cYx1LHiQfokQ9GrNzcjOh_p_0hkO1yo78ba8zTLARcLBIf8H6D","storage":"sessionStorage","map":"../../map/config-viewer.xml","listOfServices":{"wms":[],"wmts":[]},"useOSM":true,"context":"","layer":{"url":"http://www2.demis.nl/mapserver/wms.asp?","layers":"Countries","version":"1.1.1"},"projection":"EPSG:3857","projectionList":[{"code":"EPSG:4326","label":"WGS84(EPSG:4326)"},{"code":"EPSG:3857","label":"Googlemercator(EPSG:3857)"}],"disabledTools":{"processes":false,"addLayers":false,"layers":false,"filter":false,"contexts":false,"print":false,"mInteraction":false,"graticule":false,"syncAllLayers":false,"drawVector":false},"searchMapLayers":[],"viewerMapLayers":[]}, "recordview": {"enabled": true, "isSocialbarEnabled": true},"editor":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.edit","isUserRecordsOnly":false,"isFilterTagsDisplayed":false,"createPageTpl": "../../catalog/templates/editor/new-metadata-horizontal.html"},"admin":{"enabled":true,"appUrl":"../../srv/{{lang}}/admin.console"},"signin":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.signin"},"signout":{"appUrl":"../../signout"}}}', datatype = 3, position = 10000, internal = 'n' where name like 'ui/config';

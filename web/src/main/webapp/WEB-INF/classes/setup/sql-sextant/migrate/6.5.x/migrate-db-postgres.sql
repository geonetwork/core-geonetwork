INSERT INTO Settings (name, value, datatype, position, internal) VALUES
  ('ui/config', '{"langDetector":{"fromHtmlTag":false,"regexp":"^/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+/([a-z]{3})/","default":"eng"},"nodeDetector":{"regexp":"^/[a-zA-Z0-9_-]+/([a-zA-Z0-9_-]+)/[a-z]{3}/","default":"srv"},"mods":{"header":{"enabled":true,"languages":{"eng":"en","fre":"fr"}},"home":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/home"},"search":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/search","hitsperpageValues":[10,50,100],"paginationInfo":{"hitsPerPage":20},"facetsSummaryType":"hits","facetConfig":[],"facetTabField":"","filters":{"type":"dataset or series or publication or nonGeographicDataset or feature or featureCatalog or map"},"sortbyValues":[{"sortBy":"popularity","sortOrder":""},{"sortBy":"title","sortOrder":"reverse"},{"sortBy":"changeDate","sortOrder":""}],"sortBy":"popularity","resultViewTpls":[{"tplUrl":"../../catalog/views/sextant/templates/mdview/grid.html","tooltip":"Grid","icon":"fa-th"}],"resultTemplate":"../../catalog/views/sextant/templates/mdview/grid.html","formatter":{"list":[{"label":"full","url":"../api/records/{{md.getUuid()}}/formatters/xsl-view?root=div&view=advanced"}]},"linkTypes":{"links":["LINK","kml"],"downloads":["DOWNLOAD"],"layers":["OGC"],"maps":["ows"]}},"map":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.search#/map","is3DModeAllowed":false,"isSaveMapInCatalogAllowed":true,"bingKey":"AplOhn33DW5iCpDv0bY-CzSriFoi6GE2r5cY94SqSi47koQ1s4XlylK8DUB7NZFZ","storage":"sessionStorage","map":"../../map/config-viewer.xml","listOfServices":{"wms":[],"wmts":[]},"useOSM":true,"context":"","layer":{"url":"http://www2.demis.nl/mapserver/wms.asp?","layers":"Countries","version":"1.1.1"},"projection":"EPSG:3857","projectionList":[{"code":"EPSG:4326","label":"WGS84(EPSG:4326)"},{"code":"EPSG:3857","label":"Googlemercator(EPSG:3857)"}]},"editor":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.edit"},"admin":{"enabled":true,"appUrl":"../../srv/{{lang}}/admin.console"},"signin":{"enabled":true,"appUrl":"../../srv/{{lang}}/catalog.signin"},"signout":{"appUrl":"../../signout"}}}', 3, 10000, 'n');

DELETE FROM Settings WHERE  name = 'map/is3DModeAllowed';
DELETE FROM Settings WHERE  name = 'map/isMapViewerEnabled';
DELETE FROM Settings WHERE  name = 'map/config';
DELETE FROM Settings WHERE  name = 'map/proj4js';
DELETE FROM Settings WHERE  name = 'map/isSaveMapInCatalogAllowed';
DELETE FROM Settings WHERE  name = 'map/bingKey';


UPDATE Settings SET value='3.4.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


-- Fix multilingual record in italian Cannot convert ita to 2 char iso lang code
UPDATE isolanguages SET shortcode = 'it' WHERE code = 'ita';
UPDATE isolanguages SET shortcode = 'cp' WHERE code = 'cpf';


-- Do not use 3 letters id in language identifier
UPDATE metadata SET data = replace(data, 'locale="#ITA"', 'locale="#IT"') WHERE data LIKE '%locale="#ITA"%';
UPDATE metadata SET data = replace(data, 'locale="#FRE"', 'locale="#FR"') WHERE data LIKE '%locale="#FRE"%';
UPDATE metadata SET data = replace(data, 'locale="#ENG"', 'locale="#EN"') WHERE data LIKE '%locale="#ENG"%';
UPDATE metadata SET data = replace(data, 'locale="#SPA"', 'locale="#ES"') WHERE data LIKE '%locale="#SPA"%';
UPDATE metadata SET data = replace(data, 'locale="#CPF"', 'locale="#CP"') WHERE data LIKE '%locale="#CPF"%';

UPDATE metadata SET data = replace(data, 'PT_Locale id="ITA"', 'PT_Locale id="IT"') WHERE data LIKE '%PT_Locale id="ITA"%';
UPDATE metadata SET data = replace(data, 'PT_Locale id="FRE"', 'PT_Locale id="FR"') WHERE data LIKE '%PT_Locale id="FRE"%';
UPDATE metadata SET data = replace(data, 'PT_Locale id="ENG"', 'PT_Locale id="EN"') WHERE data LIKE '%PT_Locale id="ENG"%';
UPDATE metadata SET data = replace(data, 'PT_Locale id="SPA"', 'PT_Locale id="ES"') WHERE data LIKE '%PT_Locale id="SPA"%';
UPDATE metadata SET data = replace(data, 'PT_Locale id="CPF"', 'PT_Locale id="CP"') WHERE data LIKE '%PT_Locale id="CPF"%';

-- Fix some strange codelist value in language code causing indexing errors.
UPDATE metadata SET data = replace(data, 'codeListValue="France"', 'codeListValue="fre"') WHERE data LIKE '%codeListValue="France"%';

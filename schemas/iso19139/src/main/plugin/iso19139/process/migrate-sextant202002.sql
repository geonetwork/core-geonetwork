-- Thesaurus titles
SELECT schemaId, count(*) FROM metadata
    WHERE data LIKE '%>external.theme.gemet-theme<%'
    GROUP BY 1;
"iso19139";24

SELECT schemaId, count(*) FROM metadata
    WHERE data LIKE '%>external.theme.httpinspireeceuropaeutheme-theme<%'
    GROUP BY 1;
"iso19139";2374


-- Small thumbnails and old API links
SELECT schemaId, count(*) FROM metadata
    WHERE data LIKE '%_s.png<%'
    GROUP BY 1;
"iso19115-3";273
"iso19115-3.2018";7
"iso19139";3612

UPDATE metadata SET data = replace(data, 'https://sextant.ifremer.fr/geonetwork/srv/eng//resources.get', 'https://sextant.ifremer.fr/geonetwork/srv/eng/resources.get') WHERE data LIKE '%https://sextant.ifremer.fr/geonetwork/srv/eng//resources.get%';
UPDATE metadata SET data = replace(data, 'https://sextant.ifremer.fr/geonetwork/srv/fre//resources.get', 'https://sextant.ifremer.fr/geonetwork/srv/fre/resources.get') WHERE data LIKE '%https://sextant.ifremer.fr/geonetwork/srv/fre//resources.get%';

SELECT schemaId, count(*) FROM metadata
    WHERE data LIKE '%resources.get%'
    GROUP BY 1;
"iso19115-3";245
"iso19139";4095
After DB migration task

SELECT schemaId, isharvested, count(*) FROM metadata
    WHERE data LIKE '%resources.get%'
    group by 1, 2;
"iso19139";"n";64
"iso19139";"y";154


-- To do manually
SELECT schemaId, uuid FROM metadata
    WHERE data LIKE '%resources.get%' AND isHarvested = 'n';
"iso19139";"1b653b30-0818-11de-9af4-000086f6a603"
"iso19139";"306468ef-b808-44aa-9054-c879d3885bc1"
"iso19139";"e46fc7e0-55eb-11de-bc94-000086f6a603"
"iso19139";"b6d60b50-55eb-11de-bc94-000086f6a603"
"iso19139";"4b427b70-598a-11dd-b99a-000086f6a62e"



WITH ns AS (
select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
       ARRAY['gco', 'http://www.isotc211.org/2005/gco'],
       ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
       ARRAY['gmx', 'http://www.isotc211.org/2005/gmx']] AS n
)

SELECT uuid, schemaid,
unnest(xpath('count(//gmd:graphicOverview)',
	 XMLPARSE(DOCUMENT data), n))::text  AS overviews,
unnest(xpath('count(//gmd:graphicOverview[contains(*/gmd:fileName/*/text(), "resources.get")])',
	 XMLPARSE(DOCUMENT data), n))::text  AS oldapi,
 unnest(xpath('count(//gmd:graphicOverview[*/gmd:fileDescription/*/text() = "thumbnail" and substring(*/gmd:fileName/*/text(), string-length(*/gmd:fileName/*/text()) - string-length("_s.png") +1) = "_s.png"] )',
	 XMLPARSE(DOCUMENT data), n))::text  AS small
	FROM metadata, ns
	WHERE isTemplate = 'n';




-- Contacts
DROP TABLE metadata_contact_list;


CREATE TABLE metadata_contact_list AS (
WITH ns AS (
select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
       ARRAY['gco', 'http://www.isotc211.org/2005/gco'],
       ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
       ARRAY['gmx', 'http://www.isotc211.org/2005/gmx']] AS n
)

SELECT uuid, isTemplate, isHarvested,
        unnest(xpath('//gmd:CI_ResponsibleParty',
         XMLPARSE(DOCUMENT data), n))  AS contact
        FROM metadata, ns
        WHERE isTemplate = 'n');


WITH ns AS (
select ARRAY[ARRAY['xlink', 'http://www.w3.org/1999/xlink'],
       ARRAY['gco', 'http://www.isotc211.org/2005/gco'],
       ARRAY['gmd', 'http://www.isotc211.org/2005/gmd'],
       ARRAY['gmx', 'http://www.isotc211.org/2005/gmx']] AS n
)

SELECT uuid, isTemplate, isHarvested,
     unnest(xpath('//gmd:individualName/*/text()',
     XMLPARSE(DOCUMENT contact), n))::text  AS individualName,
     unnest(xpath('//gmd:organisationName/*/text()',
     XMLPARSE(DOCUMENT contact), n))::text  AS organisationName,
     unnest(xpath('//gmd:positionName/*/text()',
     XMLPARSE(DOCUMENT contact), n))::text  AS positionName,
     unnest(xpath('//gmd:electronicMailAddress/*/text()',
     XMLPARSE(DOCUMENT contact), n))::text  AS electronicMailAddress
    FROM metadata_contact_list, ns
    GROUP BY 1, 2, 3, 4, 5, 6, 7
    ORDER BY 4;



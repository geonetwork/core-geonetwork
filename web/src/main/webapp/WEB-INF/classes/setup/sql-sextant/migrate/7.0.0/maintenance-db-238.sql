--https://gitlab.ifremer.fr/sextant/geonetwork/-/issues/238

-- Clean up sources
DELETE FROM sourcesdes
WHERE iddes not in (SELECT distinct(source) FROM metadata);

DELETE FROM sources
WHERE uuid not in (SELECT distinct(source) FROM metadata);



-- Migrate virtual csw to portal
INSERT INTO sources (uuid, name, creationdate, filter, groupowner, logo, servicerecord, type, uiconfig)
SELECT replace(s.name, 'csw-', ''), replace(s.name, 'csw-', ''),
       '20201022', '+' || p.name || ':' || p.value,
       null, null, null, 'subportal', null
 FROM services s LEFT JOIN serviceparameters p
 ON s.id = p.service;

INSERT INTO sourcesdes (iddes, label, langid)
SELECT replace(s.name, 'csw-', ''), replace(s.name, 'csw-', ''), l.id
FROM services s, languages l;

-- Test portal = virtual csw

-- Drop virtual csw
DELETE FROM serviceparameters;
DELETE FROM services;




-- Old unused tables
DROP TABLE regionsdes;
DROP TABLE regions;


-- Clean up db languages
-- for l in ara cat chi fin ger nor por rus spa vie
-- do
-- echo "DELETE FROM SourcesDes WHERE langid = '$l';"
-- echo "DELETE FROM CswServerCapabilitiesInfo WHERE langid = '$l';"
-- echo "DELETE FROM CategoriesDes WHERE langid = '$l';"
-- echo "DELETE FROM GroupsDes WHERE langid = '$l';"
-- echo "DELETE FROM IsoLanguagesDes WHERE langid = '$l';"
-- echo "DELETE FROM OperationsDes WHERE langid = '$l';"
-- echo "DELETE FROM StatusValuesDes WHERE langid = '$l';"
-- echo "DELETE FROM Languages WHERE id = '$l';"
-- echo ""
-- done
DELETE FROM CswServerCapabilitiesInfo WHERE langid = 'ara';
DELETE FROM CategoriesDes WHERE langid = 'ara';
DELETE FROM GroupsDes WHERE langid = 'ara';
DELETE FROM IsoLanguagesDes WHERE langid = 'ara';
DELETE FROM OperationsDes WHERE langid = 'ara';
DELETE FROM StatusValuesDes WHERE langid = 'ara';
DELETE FROM Languages WHERE id = 'ara';

DELETE FROM CswServerCapabilitiesInfo WHERE langid = 'cat';
DELETE FROM CategoriesDes WHERE langid = 'cat';
DELETE FROM GroupsDes WHERE langid = 'cat';
DELETE FROM IsoLanguagesDes WHERE langid = 'cat';
DELETE FROM OperationsDes WHERE langid = 'cat';
DELETE FROM StatusValuesDes WHERE langid = 'cat';
DELETE FROM Languages WHERE id = 'cat';

DELETE FROM CswServerCapabilitiesInfo WHERE langid = 'chi';
DELETE FROM CategoriesDes WHERE langid = 'chi';
DELETE FROM GroupsDes WHERE langid = 'chi';
DELETE FROM IsoLanguagesDes WHERE langid = 'chi';
DELETE FROM OperationsDes WHERE langid = 'chi';
DELETE FROM StatusValuesDes WHERE langid = 'chi';
DELETE FROM Languages WHERE id = 'chi';

DELETE FROM CswServerCapabilitiesInfo WHERE langid = 'fin';
DELETE FROM CategoriesDes WHERE langid = 'fin';
DELETE FROM GroupsDes WHERE langid = 'fin';
DELETE FROM IsoLanguagesDes WHERE langid = 'fin';
DELETE FROM OperationsDes WHERE langid = 'fin';
DELETE FROM StatusValuesDes WHERE langid = 'fin';
DELETE FROM Languages WHERE id = 'fin';

DELETE FROM CswServerCapabilitiesInfo WHERE langid = 'ger';
DELETE FROM CategoriesDes WHERE langid = 'ger';
DELETE FROM GroupsDes WHERE langid = 'ger';
DELETE FROM IsoLanguagesDes WHERE langid = 'ger';
DELETE FROM OperationsDes WHERE langid = 'ger';
DELETE FROM StatusValuesDes WHERE langid = 'ger';
DELETE FROM Languages WHERE id = 'ger';

DELETE FROM CswServerCapabilitiesInfo WHERE langid = 'nor';
DELETE FROM CategoriesDes WHERE langid = 'nor';
DELETE FROM GroupsDes WHERE langid = 'nor';
DELETE FROM IsoLanguagesDes WHERE langid = 'nor';
DELETE FROM OperationsDes WHERE langid = 'nor';
DELETE FROM StatusValuesDes WHERE langid = 'nor';
DELETE FROM Languages WHERE id = 'nor';

DELETE FROM CswServerCapabilitiesInfo WHERE langid = 'por';
DELETE FROM CategoriesDes WHERE langid = 'por';
DELETE FROM GroupsDes WHERE langid = 'por';
DELETE FROM IsoLanguagesDes WHERE langid = 'por';
DELETE FROM OperationsDes WHERE langid = 'por';
DELETE FROM StatusValuesDes WHERE langid = 'por';
DELETE FROM Languages WHERE id = 'por';

DELETE FROM CswServerCapabilitiesInfo WHERE langid = 'rus';
DELETE FROM CategoriesDes WHERE langid = 'rus';
DELETE FROM GroupsDes WHERE langid = 'rus';
DELETE FROM IsoLanguagesDes WHERE langid = 'rus';
DELETE FROM OperationsDes WHERE langid = 'rus';
DELETE FROM StatusValuesDes WHERE langid = 'rus';
DELETE FROM Languages WHERE id = 'rus';

DELETE FROM CswServerCapabilitiesInfo WHERE langid = 'spa';
DELETE FROM CategoriesDes WHERE langid = 'spa';
DELETE FROM GroupsDes WHERE langid = 'spa';
DELETE FROM IsoLanguagesDes WHERE langid = 'spa';
DELETE FROM OperationsDes WHERE langid = 'spa';
DELETE FROM StatusValuesDes WHERE langid = 'spa';
DELETE FROM Languages WHERE id = 'spa';

DELETE FROM CswServerCapabilitiesInfo WHERE langid = 'vie';
DELETE FROM CategoriesDes WHERE langid = 'vie';
DELETE FROM GroupsDes WHERE langid = 'vie';
DELETE FROM IsoLanguagesDes WHERE langid = 'vie';
DELETE FROM OperationsDes WHERE langid = 'vie';
DELETE FROM StatusValuesDes WHERE langid = 'vie';
DELETE FROM Languages WHERE id = 'vie';

DELETE FROM SourcesDes WHERE langid = 'ara';
DELETE FROM SourcesDes WHERE langid = 'cat';
DELETE FROM SourcesDes WHERE langid = 'chi';
DELETE FROM SourcesDes WHERE langid = 'fin';
DELETE FROM SourcesDes WHERE langid = 'ger';
DELETE FROM SourcesDes WHERE langid = 'nor';
DELETE FROM SourcesDes WHERE langid = 'por';
DELETE FROM SourcesDes WHERE langid = 'rus';
DELETE FROM SourcesDes WHERE langid = 'spa';
DELETE FROM SourcesDes WHERE langid = 'vie';

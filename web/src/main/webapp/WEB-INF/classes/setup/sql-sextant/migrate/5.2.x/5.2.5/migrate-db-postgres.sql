
-- See https://github.com/geonetwork/core-geonetwork/commit/8ec082bc48c613b53acb06963d29c9a734482ba9
-- withheld element filter is now configured as a per schema basis
DELETE FROM Settings WHERE name = 'system/hidewithheldelements/enable';
DELETE FROM Settings WHERE name = 'system/hidewithheldelements/keepMarkedElement';
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/xlinkResolver/localXlinkEnable', 'true', 2, 2311, 'n');



UPDATE metadata SET data = replace(data,
'local.feature-type.seadatanet.feature-type',
'external.feature-type.NVS.L02')
WHERE data like '%local.feature-type.seadatanet.feature-type%';

UPDATE metadata SET data = replace(data,
'local.parameter.myocean.ocean-variables',
'external.parameter.myocean.ocean-variables')
WHERE data like '%local.parameter.myocean.ocean-variables%';

UPDATE metadata SET data = replace(data,
'local.parameter.seadatanet.parameter.groups.P03',
'external.parameter.NVS.P03')
WHERE data like '%local.parameter.seadatanet.parameter.groups.P03%';


UPDATE metadata SET data = replace(data,
'local.parameter.seadatanet.parameter.usage.P01',
'external.parameter.NVS.P01')
WHERE data like '%local.parameter.seadatanet.parameter.usage.P01%';


UPDATE metadata SET data = replace(data,
'local.parameter.seadatanet-ocean-chemistry-variable',
'external.parameter.NVS.P35')
WHERE data like '%local.parameter.seadatanet-ocean-chemistry-variable%';


UPDATE metadata SET data = replace(data,
'local.parameter.seadatanet-ocean-discovery-parameter',
'external.parameter.NVS.P02')
WHERE data like '%local.parameter.seadatanet-ocean-discovery-parameter%';


UPDATE metadata SET data = replace(data,
'local.reference-geographical-area.seadatanet.reference-geographical-area',
'external.reference-geographical-area.NVS.C19')
WHERE data like '%local.reference-geographical-area.seadatanet.reference-geographical-area%';


UPDATE metadata SET data = replace(data,
'local.use-limitation.seadatanet.use-limitation',
'external.use-limitation.NVS.L08')
WHERE data like '%local.use-limitation.seadatanet.use-limitation%';

UPDATE metadata SET data = replace(data,
'external.theme.medsea.data.delivery.mechanism',
'local.theme.medsea.data.delivery.mechanism')
WHERE data like '%external.theme.medsea.data.delivery.mechanism%';

UPDATE metadata SET data = replace(data,
'external.theme.medsea.challenges',
'local.theme.medsea.challenges')
WHERE data like '%external.theme.medsea.challenges%';

UPDATE metadata SET data = replace(data,
'external.theme.medsea.production.mode',
'local.theme.medsea.production.mode')
WHERE data like '%external.theme.medsea.production.mode%';

UPDATE metadata SET data = replace(data,
'external.theme.medsea.level.of.characteristics',
'local.theme.medsea.level.of.characteristics')
WHERE data like '%external.theme.medsea.level.of.characteristics%';

UPDATE metadata SET data = replace(data,
'external.theme.medsea.environmental.matrix',
'local.theme.medsea.environmental.matrix')
WHERE data like '%external.theme.medsea.environmental.matrix%';


-- Updates for thesaurus not in github
UPDATE metadata SET data = replace(data,
'Portail-donnees-facette-discipline',
'portail-donnees-facette-delai')
WHERE data like '%Portail-donnees-facette-discipline%';

UPDATE metadata SET data = replace(data,
'Portail-donnees-facette-delai',
'portail-donnees-facette-discipline')
WHERE data like '%Portail-donnees-facette-delai%';

UPDATE metadata SET data = replace(data,
'Portail-donnees-facette-comment',
'portail-donnees-facette-comment')
WHERE data like '%Portail-donnees-facette-comment%';

UPDATE metadata SET data = replace(data,
'Portail-donnees-facette-type',
'portail-donnees-facette-type')
WHERE data like '%Portail-donnees-facette-type%';





UPDATE metadata SET data = replace(data,
'external.theme.dcsmm-type-espace',
'local.theme.dcsmm-type-espace')
WHERE data like '%external.theme.dcsmm-type-espace%';

UPDATE metadata SET data = replace(data,
'external.theme.dcsmm-descripteur',
'local.theme.dcsmm-descripteur')
WHERE data like '%external.theme.dcsmm-descripteur%';

UPDATE metadata SET data = replace(data,
'external.theme.dcsmm-methode',
'local.theme.dcsmm-methode')
WHERE data like '%external.theme.dcsmm-methode%';


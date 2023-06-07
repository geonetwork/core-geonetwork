
UPDATE settings SET name = 'metadata/workflow/allowSubmitApproveInvalidMd'
WHERE name = 'metadata/workflow/allowSumitApproveInvalidMd';

UPDATE settings_ui
  SET  configuration = replace(configuration,
    '"showMapInFacet": true', '"searchMapPlacement": "facets"')
  WHERE configuration LIKE '%"showMapInFacet": true%';

UPDATE settings_ui
  SET  configuration = replace(configuration,
    '"showMapInFacet": false', '"searchMapPlacement": "results"')
  WHERE configuration LIKE '%"showMapInFacet": false%';

-- Update translations for status values
UPDATE StatusValuesDes SET label = 'Entwurf' WHERE iddes = 1 AND langid = 'ger';
UPDATE StatusValuesDes SET label = 'Genehmigt' WHERE iddes = 2 AND langid = 'ger';
UPDATE StatusValuesDes SET label = 'Übermittelt' WHERE iddes = 4 AND langid = 'ger';
UPDATE StatusValuesDes SET label = 'Abgelehnt' WHERE iddes = 5 AND langid = 'ger';

UPDATE StatusValuesDes SET label = 'Luonnos' WHERE iddes = 1 AND langid = 'fin';
UPDATE StatusValuesDes SET label = 'Hyväksytty' WHERE iddes = 2 AND langid = 'fin';
UPDATE StatusValuesDes SET label = 'Poistettu' WHERE iddes = 3 AND langid = 'fin';
UPDATE StatusValuesDes SET label = 'Lähetetty' WHERE iddes = 4 AND langid = 'fin';

UPDATE StatusValuesDes SET label = 'Rascunho' WHERE iddes = 1 AND langid = 'por';
UPDATE StatusValuesDes SET label = 'Aprovado' WHERE iddes = 2 AND langid = 'por';
UPDATE StatusValuesDes SET label = 'Retirado' WHERE iddes = 3 AND langid = 'por';
UPDATE StatusValuesDes SET label = 'Enviado' WHERE iddes = 4 AND langid = 'por';
UPDATE StatusValuesDes SET label = 'Rejeitado' WHERE iddes = 5 AND langid = 'por';

UPDATE StatusValuesDes SET label = 'Borrador' WHERE iddes = 1 AND langid = 'spa';
UPDATE StatusValuesDes SET label = 'Aprobado' WHERE iddes = 2 AND langid = 'spa';
UPDATE StatusValuesDes SET label = 'Retirado' WHERE iddes = 3 AND langid = 'spa';
UPDATE StatusValuesDes SET label = 'Enviado' WHERE iddes = 4 AND langid = 'spa';
UPDATE StatusValuesDes SET label = 'Rechazado' WHERE iddes = 5 AND langid = 'spa';


UPDATE Settings SET value='4.2.5' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

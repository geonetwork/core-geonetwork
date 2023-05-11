
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

UPDATE Settings SET value='4.2.5' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

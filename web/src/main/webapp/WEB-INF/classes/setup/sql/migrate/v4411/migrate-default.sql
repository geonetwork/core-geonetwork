UPDATE settings_ui
SET configuration = replace(
  configuration,
  '"gn-recordview-manage-menu"',
  '"gn-recordview-manage-menu","gn-recordview-download-menu"'
                    )
WHERE configuration LIKE '%"gn-recordview-manage-menu"%'
  AND configuration NOT LIKE '%"gn-recordview-download-menu"%';

UPDATE Settings SET value='4.4.11' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

UPDATE StatusValues SET type = 'workflow';

UPDATE Settings SET value='3.5.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

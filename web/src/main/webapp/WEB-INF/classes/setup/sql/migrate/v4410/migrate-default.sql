UPDATE Settings SET value='4.4.10' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

UPDATE groups SET type='RecordPrivilege' WHERE id<2 AND type IS NULL;
UPDATE groups SET type='Workspace' WHERE id>=2 AND type IS NULL;

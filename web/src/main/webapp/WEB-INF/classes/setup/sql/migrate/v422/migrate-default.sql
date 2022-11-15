DELETE FROM Settings WHERE name = 'system/downloadservice/leave';
DELETE FROM Settings WHERE name = 'system/downloadservice/simple';
DELETE FROM Settings WHERE name = 'system/downloadservice/withdisclaimer';

UPDATE Settings SET value='4.2.2' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO StatusValues (id, name, reserved, displayorder, type, notificationLevel) VALUES  (63,'recordrestored','y', 63, 'event', null);

UPDATE Settings SET value='3.9.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

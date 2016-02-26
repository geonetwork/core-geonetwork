UPDATE Settings SET value='3.1.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/feedback/mailServer/tls', 'false', 2, 644, 'y');
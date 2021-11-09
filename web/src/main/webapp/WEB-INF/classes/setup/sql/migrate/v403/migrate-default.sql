UPDATE Settings SET value='4.0.3' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- Changes were back ported to version 3.12.x so they are no longer required unless upgrading from v400, v401, v402 which did not have 3.12.x  migrations steps.
-- So lets try to only add the records if they don't already exists.
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/security/passwordEnforcement/minLength', '6', 1, 12000, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/security/passwordEnforcement/minLength');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/security/passwordEnforcement/maxLength', '20', 1, 12001, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/security/passwordEnforcement/maxLength');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/security/passwordEnforcement/usePattern', 'true', 2, 12002, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/security/passwordEnforcement/usePattern');
INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/security/passwordEnforcement/pattern', '^((?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*(_|[^\w])).*)$', 0, 12003, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/security/passwordEnforcement/pattern');

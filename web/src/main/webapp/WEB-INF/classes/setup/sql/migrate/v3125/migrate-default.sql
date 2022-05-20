
-- WARNING: Security / Add this settings only if you need to allow admin
-- users to be able to reset user password. If you have mail server configured
-- user can reset password directly. If not, then you may want to add that settings
-- if you don't have access to the database.
-- INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/security/password/allowAdminReset', 'false', 2, 12004, 'n');

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('metadata/import/userprofile', 'Editor', 0, 11005, 'n');

UPDATE Settings SET value='3.12.5' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

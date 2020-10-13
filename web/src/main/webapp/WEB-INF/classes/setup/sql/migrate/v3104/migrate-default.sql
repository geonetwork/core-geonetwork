UPDATE Settings SET value='3.10.4' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/users/identicon', 'gravatar:mp', 0, 9110, 'n');

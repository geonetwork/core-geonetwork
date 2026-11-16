INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadata/thesaurusUrlAllowlist', '', 0, 9162, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadata/thesaurusUrlAllowlist');

-- Move metadata/vcs/enable to a free position so it no longer shares position 9161 with system/metadata/thesaurusNamespace.
UPDATE Settings SET position = '9164' WHERE name = 'metadata/vcs/enable' AND position = '9161';

INSERT INTO Settings (name, value, datatype, position, internal) SELECT distinct 'system/metadata/thesaurusUrlAllowlist', '', 0, 9162, 'n' from settings WHERE NOT EXISTS (SELECT name FROM Settings WHERE name = 'system/metadata/thesaurusUrlAllowlist');

-- Move metadata/vcs/enable to a free position so it no longer shares position 9161 with system/metadata/thesaurusNamespace.
UPDATE Settings SET position = '9164' WHERE name = 'metadata/vcs/enable' AND position = '9161';
UPDATE Settings SET value='4.2.18' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

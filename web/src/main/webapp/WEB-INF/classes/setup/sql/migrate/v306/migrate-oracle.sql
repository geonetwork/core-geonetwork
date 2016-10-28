UPDATE Settings SET value='3.0.6' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

ALTER TABLE users ADD COLUMN isenabled CHAR(1) DEFAULT 'y';
UPDATE users SET isenabled = 'y' WHERE enabled = 1;
UPDATE users SET isenabled = 'n' WHERE enabled = 0;
ALTER TABLE users  DROP COLUMN enabled;

ALTER TABLE groups ADD COLUMN enableCategoriesRestriction CHAR(1) DEFAULT 'n';
UPDATE groups SET enableCategoriesRestriction = 'y' WHERE ENABLEALLOWEDCATEGORIES = 1;
UPDATE groups SET enableCategoriesRestriction = 'n' WHERE ENABLEALLOWEDCATEGORIES = 0;
ALTER TABLE groups DROP COLUMN ENABLEALLOWEDCATEGORIES;

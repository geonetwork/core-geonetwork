UPDATE Settings SET value='3.2.2' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

-- This ALTER TABLE should be performerd automatically by hibernate
-- ALTER TABLE users ADD COLUMN isenabled CHAR(1) DEFAULT 'y';
UPDATE users SET isenabled = 'y' WHERE enabled = true;
UPDATE users SET isenabled = 'n' WHERE enabled = false;
ALTER TABLE users  DROP COLUMN enabled;

-- This ALTER TABLE should be performerd automatically by hibernate
-- ALTER TABLE groups ADD COLUMN enableCategoriesRestriction CHAR(1) DEFAULT 'n';
UPDATE groups SET enableCategoriesRestriction = 'y' WHERE ENABLEALLOWEDCATEGORIES = true;
UPDATE groups SET enableCategoriesRestriction = 'n' WHERE ENABLEALLOWEDCATEGORIES = false;
ALTER TABLE groups DROP COLUMN ENABLEALLOWEDCATEGORIES;

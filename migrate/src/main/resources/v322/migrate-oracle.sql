UPDATE Settings SET value='3.2.2' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

-- These 2 ALTER TABLE should be performerd automatically by hibernate
-- ALTER TABLE users ADD COLUMN isenabled CHAR(1) DEFAULT 'y';
-- ALTER TABLE groups ADD COLUMN enableCategoriesRestriction CHAR(1) DEFAULT 'n';

-- In case your GN 3.2.1 was able to properly create a DB schema, you should
-- update the DB with the following commands.
-- According to how your workarounds on oracle incompatibilities were implemented,
-- you may want to change the following lines accordingly.

--UPDATE users SET isenabled = 'y' WHERE enabled = true;
--UPDATE users SET isenabled = 'n' WHERE enabled = false;
--ALTER TABLE users  DROP COLUMN enabled;
--UPDATE groups SET enableCategoriesRestriction = 'y' WHERE ENABLEALLOWEDCATEGORIES = true;
--UPDATE groups SET enableCategoriesRestriction = 'n' WHERE ENABLEALLOWEDCATEGORIES = false;
--ALTER TABLE groups DROP COLUMN ENABLEALLOWEDCATEGORIES;

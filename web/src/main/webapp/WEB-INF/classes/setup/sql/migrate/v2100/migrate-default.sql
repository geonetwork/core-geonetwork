-- Spring security
UPDATE Users SET security='update_hash_required';

-- Delete LDAP settings
DELETE FROM Settings WHERE parentid=86;
DELETE FROM Settings WHERE parentid=87;
DELETE FROM Settings WHERE parentid=89;
DELETE FROM Settings WHERE parentid=80;
DELETE FROM Settings WHERE id=80;

-- New settings 
INSERT INTO Settings VALUES (24,20,'securePort','8443');
INSERT INTO Settings VALUES (1000956,1,'hidewithheldelements',NULL);
INSERT INTO Settings VALUES (1000957,1000956,'enable','false');
INSERT INTO Settings VALUES (1000958,1000956,'keepMarkedElement','true');
--INSERT INTO Settings VALUES (1000955,1000956,'ignored','true');

-- Version update
UPDATE Settings SET value='2.10.0' WHERE name='version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='subVersion';

UPDATE HarvestHistory SET elapsedTime = 0 WHERE elapsedTime IS NULL;

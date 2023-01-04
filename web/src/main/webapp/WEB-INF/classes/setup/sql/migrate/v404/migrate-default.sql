
DELETE FROM Schematrondes WHERE iddes IN (SELECT id FROM schematron WHERE filename LIKE 'schematron-rules-inspire%');
DELETE FROM Schematroncriteria WHERE group_name || group_schematronid IN (SELECT name || schematronid FROM schematroncriteriagroup WHERE schematronid IN (SELECT id FROM schematron WHERE filename LIKE 'schematron-rules-inspire%'));
DELETE FROM Schematroncriteriagroup WHERE schematronid IN (SELECT id FROM schematron WHERE filename LIKE 'schematron-rules-inspire%');
DELETE FROM Schematron WHERE filename LIKE 'schematron-rules-inspire%';

UPDATE Settings SET value='4.0.4' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';

-- Changes were back ported to version 3.12.x so they are no longer required unless upgrading from v400, v401, v402, v403 which did not have 3.12.x migrations steps
-- For security reasons, the statements will remain as it will simply change the values back to true if they were previously changed to false.
-- ALTER TABLE Settings ADD COLUMN encrypted VARCHAR(1) DEFAULT 'n';
UPDATE Settings SET encrypted='y' WHERE name='system/proxy/password';
UPDATE Settings SET encrypted='y' WHERE name='system/feedback/mailServer/password';
UPDATE Settings SET encrypted='y' WHERE name='system/publication/doi/doipassword';

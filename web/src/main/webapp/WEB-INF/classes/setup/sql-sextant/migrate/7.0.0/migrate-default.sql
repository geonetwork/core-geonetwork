-- #############################################
-- Before application starts
-- #############################################
CREATE TABLE metadatastatus_backup AS SELECT * FROM metadatastatus;
DROP TABLE metadatastatus;


ALTER TABLE groupsdes ALTER COLUMN label TYPE varchar(255);
ALTER TABLE sourcesdes ALTER COLUMN label TYPE varchar(255);
ALTER TABLE schematrondes ALTER COLUMN label TYPE varchar(255);



-- Increase the length of Validation type (where the schematron file name is stored)
ALTER TABLE Validation ALTER COLUMN valType TYPE varchar(128);


INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/users/identicon', 'gravatar:mp', 0, 9110, 'n');

ALTER TABLE usersearch ALTER COLUMN url TYPE text;

DROP TABLE CswServerCapabilitiesInfo;

-- keep these at the bottom of the file!
DROP INDEX idx_metadatafiledownloads_metadataid;
DROP INDEX idx_metadatafileuploads_metadataid;
DROP INDEX idx_operationallowed_metadataid;


DROP TABLE metadatanotifications;
DROP TABLE metadatanotifiers;

DELETE FROM Settings WHERE name LIKE 'system/indexoptimizer%';
DELETE FROM Settings WHERE name LIKE 'system/requestedLanguage%';
DELETE FROM Settings WHERE name = 'system/inspire/enableSearchPanel';
DELETE FROM Settings WHERE name = 'system/autodetect/enable';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/index/indexingTimeRecordLink', 'false', 2, 9209, 'n');

UPDATE metadata
    SET data = REGEXP_REPLACE(data, '[a-z]{3}\/thesaurus\.download\?ref=', 'api/registries/vocabularies/', 'g')
    WHERE data LIKE '%thesaurus.download?ref=%';




UPDATE Settings SET value = 'Europe/Paris' WHERE name = 'system/server/timeZone';


-- User feedback
UPDATE Settings SET value = 'advanced', internal = 'n', datatype = 0  WHERE name = 'system/localrating/enable';

INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (-1, 'Average', 'y');
-- INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (0, 'Completeness', 'n');
-- INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (1, 'Discoverability', 'n');
-- INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (2, 'Readability', 'n');
INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (3, 'DataQuality', 'n');
-- INSERT INTO GUF_RatingCriteria (id, name, isinternal) VALUES (4, 'ServiceQuality', 'n');

INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'eng', 'Average');
-- INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'eng', 'Completeness#Is the information on this page complete enough to know what you can expect from this dataset?');
-- INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'eng', 'Discoverability#Was it easy to find this information page?');
-- INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'eng', 'Readability#Was it easy to read and understand the contents of this page?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'eng', 'Data quality#Does the dataset contain the information you expected, the dataset has enough accuracy, the data is valid/up-to-date?');
-- INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'eng', 'Service quality#The dataset is provided as a service or mediatype that is easy to work with?');


INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (-1,'fre', 'Moyenne');
-- INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (0,'fre', 'Complétude#Est-ce que les informations sur cette page sont suffisamment précises pour savoir ce que vous pouvez attendre de cette ressource ?');
-- INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (1,'fre', 'Découvrabilité#Était-il facile de trouver cette page ?');
-- INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (2,'fre', 'Lisibilité#Était-il facile de comprendre le contenu de cette page ?');
INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (3,'fre', 'Qualité des données#Est-ce que cette ressource contient les informations attendues ? Les données sont-elles assez précises ? assez récentes ?');
-- INSERT INTO GUF_RatingCriteriaDes (iddes, langid, label) VALUES (4,'fre', 'Cette données est elle accessible dans un format ou via un service simple à utiliser ?');

-- Delete unused criteria
DELETE FROM GUF_RatingCriteriaDes WHERE iddes NOT IN (-1, 3);
DELETE FROM GUF_userfeedbacks_guf_rating;
DELETE FROM GUF_Rating WHERE criteria_id NOT IN (-1, 3);
DELETE FROM GUF_RatingCriteria WHERE id NOT IN (-1, 3);

ALTER TABLE guf_userfeedbacks_guf_rating DROP COLUMN GUF_UserFeedbacks_uuid;


UPDATE Settings SET value='4.0.6' WHERE name='system/platform/version';
UPDATE Settings SET value='0' WHERE name='system/platform/subVersion';

DELETE FROM settings_ui;

UPDATE settings SET value = '1' WHERE name = 'system/threadedindexing/maxthreads';
DELETE FROM settings_ui;

UPDATE StatusValues SET notificationLevel = 'catalogueAdministrator' WHERE name = 'doiCreationTask';

-- #############################################
-- After application starts
-- #############################################

SELECT setval('address_id_seq', (SELECT max(id) + 1 FROM address));
SELECT setval('files_id_seq', (SELECT max(id) + 1 FROM files));
SELECT setval('group_id_seq', (SELECT max(id) + 1 FROM groups));
SELECT setval('gufkey_id_seq', (SELECT max(id) + 1 FROM guf_keywords));
SELECT setval('gufrat_id_seq', (SELECT max(id) + 1 FROM guf_rating));
SELECT setval('harvest_history_id_seq', (SELECT max(id) + 1 FROM harvesthistory));
SELECT setval('harvester_setting_id_seq', (SELECT max(id) + 1 FROM harvestersettings));
SELECT setval('inspire_atom_feed_id_seq', (SELECT max(id) + 1 FROM inspireatomfeed));
SELECT setval('iso_language_id_seq', (SELECT max(id) + 1 FROM isolanguages));
SELECT setval('link_id_seq', (SELECT max(id) + 1 FROM links));
SELECT setval('linkstatus_id_seq', (SELECT max(id) + 1 FROM linkstatus));
SELECT setval('mapserver_id_seq', (SELECT max(id) + 1 FROM mapservers));
SELECT setval('metadata_category_id_seq', (SELECT max(id) + 1 FROM categories));
SELECT setval('metadata_filedownload_id_seq', (SELECT max(id) + 1 FROM metadatafiledownloads));
SELECT setval('metadata_fileupload_id_seq', (SELECT max(id) + 1 FROM metadatafileuploads));
SELECT setval('metadata_id_seq', (SELECT max(id) + 1 FROM metadata));
SELECT setval('metadata_identifier_template_id_seq', (SELECT max(id) + 1 FROM metadataidentifiertemplate));
SELECT setval('operation_id_seq', (SELECT max(id) + 1 FROM operations));
SELECT setval('rating_criteria_id_seq', (SELECT max(id) + 1 FROM guf_ratingcriteria));
SELECT setval('schematron_criteria_id_seq', (SELECT max(id) + 1 FROM schematroncriteria));
SELECT setval('schematron_id_seq', (SELECT max(id) + 1 FROM schematron));
SELECT setval('selection_id_seq', (SELECT max(id) + 1 FROM selections));
SELECT setval('status_value_id_seq', (SELECT max(id) + 1 FROM statusvalues));
SELECT setval('user_id_seq', (SELECT max(id) + 1 FROM users));
SELECT setval('user_search_id_seq', (SELECT max(id) + 1 FROM usersearch));

UPDATE messageproducerentity SET strategy = 'investigator';

INSERT INTO metadatastatus (id, changedate, changemessage, closedate, currentstate, duedate, metadataid, owner, previousstate, titles, userid, uuid, statusid)
SELECT nextval('metadatastatus_id_seq'), changedate, changemessage, closedate, currentstate, duedate, metadataid, COALESCE(owner, 0), previousstate, null, userid, (SELECT uuid FROM metadata WHERE id = s.metadataid), statusid
FROM metadatastatus_backup s;

-- DROP TABLE metadatastatus_backup;

-- https://gitlab.ifremer.fr/sextant/geonetwork/-/issues/385
INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/harvester/enablePrivilegesManagement', 'false', 2, 9010, 'n');

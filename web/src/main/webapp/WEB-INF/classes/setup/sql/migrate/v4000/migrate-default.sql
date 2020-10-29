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

UPDATE settings SET value = '1' WHERE name = 'system/threadedindexing/maxthreads';

UPDATE Settings SET value='4.0.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


-- Utility script to update sequence to current value on Postgres
-- https://github.com/geonetwork/core-geonetwork/pull/5003
-- SELECT setval('address_id_seq', (SELECT max(id) + 1 FROM address));
-- SELECT setval('csw_server_capabilities_info_id_seq', (SELECT max(idfield) FROM cswservercapabilitiesinfo));
-- SELECT setval('files_id_seq', (SELECT max(id) + 1 FROM files));
-- SELECT setval('group_id_seq', (SELECT max(id) + 1 FROM groups));
-- SELECT setval('gufkey_id_seq', (SELECT max(id) + 1 FROM guf_keywords));
-- SELECT setval('gufrat_id_seq', (SELECT max(id) + 1 FROM guf_rating));
-- SELECT setval('harvest_history_id_seq', (SELECT max(id) + 1 FROM harvesthistory));
-- SELECT setval('harvester_setting_id_seq', (SELECT max(id) + 1 FROM harvestersettings));
-- SELECT setval('inspire_atom_feed_id_seq', (SELECT max(id) + 1 FROM inspireatomfeed));
-- SELECT setval('iso_language_id_seq', (SELECT max(id) + 1 FROM isolanguages));
-- SELECT setval('link_id_seq', (SELECT max(id) + 1 FROM links));
-- SELECT setval('linkstatus_id_seq', (SELECT max(id) + 1 FROM linkstatus));
-- SELECT setval('mapserver_id_seq', (SELECT max(id) + 1 FROM mapservers));
-- SELECT setval('metadata_category_id_seq', (SELECT max(id) + 1 FROM categories));
-- SELECT setval('metadata_filedownload_id_seq', (SELECT max(id) + 1 FROM metadatafiledownloads));
-- SELECT setval('metadata_fileupload_id_seq', (SELECT max(id) + 1 FROM metadatafileuploads));
-- SELECT setval('metadata_id_seq', (SELECT max(id) + 1 FROM metadata));
-- SELECT setval('metadata_identifier_template_id_seq', (SELECT max(id) + 1 FROM metadataidentifiertemplate));
-- SELECT setval('operation_id_seq', (SELECT max(id) + 1 FROM operations));
-- SELECT setval('rating_criteria_id_seq', (SELECT max(id) + 1 FROM guf_ratingcriteria));
-- SELECT setval('schematron_criteria_id_seq', (SELECT max(id) + 1 FROM schematroncriteria));
-- SELECT setval('schematron_id_seq', (SELECT max(id) + 1 FROM schematron));
-- SELECT setval('selection_id_seq', (SELECT max(id) + 1 FROM selections));
-- SELECT setval('status_value_id_seq', (SELECT max(id) + 1 FROM statusvalues));
-- SELECT setval('user_id_seq', (SELECT max(id) + 1 FROM users));
-- SELECT setval('user_search_id_seq', (SELECT max(id) + 1 FROM usersearch));

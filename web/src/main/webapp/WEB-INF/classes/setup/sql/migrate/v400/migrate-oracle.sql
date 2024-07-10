-- Consider running the following script before upgrading. It will resequence all sequences,
-- so that UpdateAllSequenceValueToMax.java won't hang if sequences are way out of sync.
--
-- declare 
--   type t_seq_tab is record (sequence_name varchar2(255), table_name varchar2(255));
--   type nt_seq_tab is table of t_seq_tab;
--   v_seq_tab nt_seq_tab := nt_seq_tab(
--  t_seq_tab('ADDRESS_ID_SEQ'                     , 'ADDRESS')
-- ,t_seq_tab('CSW_SERVER_CAPABILITIES_INFO_ID_SEQ', 'CSWSERVERCAPABILITIESINFO')
-- ,t_seq_tab('FILES_ID_SEQ'                       , 'FILES')
-- ,t_seq_tab('GROUP_ID_SEQ'                       , 'GROUPS')
-- ,t_seq_tab('GUFKEY_ID_SEQ'                      , 'GUF_KEYWORDS')
-- ,t_seq_tab('GUFRAT_ID_SEQ'                      , 'GUF_RATING')
-- ,t_seq_tab('HARVESTER_SETTING_ID_SEQ'           , 'HARVESTERSETTINGS')
-- ,t_seq_tab('HARVEST_HISTORY_ID_SEQ'             , 'HARVESTHISTORY')
-- ,t_seq_tab('INSPIRE_ATOM_FEED_ID_SEQ'           , 'INSPIREATOMFEED')
-- ,t_seq_tab('ISO_LANGUAGE_ID_SEQ'                , 'ISOLANGUAGES')
-- ,t_seq_tab('MAPSERVER_ID_SEQ'                   , 'MAPSERVERS')
-- ,t_seq_tab('METADATA_CATEGORY_ID_SEQ'           , 'CATEGORIES')
-- ,t_seq_tab('METADATA_FILEDOWNLOAD_ID_SEQ'       , 'METADATAFILEDOWNLOADS')
-- ,t_seq_tab('METADATA_FILEUPLOAD_ID_SEQ'         , 'METADATAFILEUPLOADS')
-- ,t_seq_tab('METADATA_IDENTIFIER_TEMPLATE_ID_SEQ', 'METADATAIDENTIFIERTEMPLATE')
-- ,t_seq_tab('METADATA_ID_SEQ'                    , 'METADATA')
-- ,t_seq_tab('METADATA_NOTIFIER_ID_SEQ'           , 'METADATANOTIFIERS')
-- ,t_seq_tab('OPERATION_ID_SEQ'                   , 'OPERATIONS')
-- ,t_seq_tab('RATING_CRITERIA_ID_SEQ'             , 'GUF_RATINGCRITERIA')
-- ,t_seq_tab('SCHEMATRON_CRITERIA_ID_SEQ'         , 'SCHEMATRONCRITERIA')
-- ,t_seq_tab('SCHEMATRON_ID_SEQ'                  , 'SCHEMATRON')
-- ,t_seq_tab('SELECTION_ID_SEQ'                   , 'SELECTIONS')
-- ,t_seq_tab('SERVICE_ID_SEQ'                     , 'SERVICES')
-- ,t_seq_tab('SERVICEPARAMETERS_ID_SEQ'           , 'SERVICEPARAMETERS')
-- ,t_seq_tab('STATUS_VALUE_ID_SEQ'                , 'STATUSVALUES')
-- ,t_seq_tab('USER_ID_SEQ'                        , 'USERS')
-- );
--   v_pk varchar2(255);
--   v_max number;
--   v_stmt varchar2(1000);
-- begin
--   for i in 1..v_seq_tab.count loop
--     select cc.column_name
--       into v_pk
--       from user_constraints c
--       join user_cons_columns cc on (c.constraint_name = cc.constraint_name)
--      where c.table_name = v_seq_tab(i).table_name
--        and c.constraint_type = 'P';
--     execute immediate 'select max(' || v_pk || ') from ' ||  v_seq_tab(i).table_name into v_max;
--     v_stmt := 'alter sequence ' || v_seq_tab(i).sequence_name || ' restart start with ' || to_number(nvl(v_max, 0) + 1);
--     dbms_output.put_line( v_stmt);
--     execute immediate v_stmt;
--   end loop;
-- end;
-- /


DROP TABLE metadatanotifications;
DROP TABLE metadatanotifiers;

DELETE FROM Settings WHERE name LIKE 'system/indexoptimizer%';
DELETE FROM Settings WHERE name LIKE 'system/requestedLanguage%';
DELETE FROM Settings WHERE name = 'system/inspire/enableSearchPanel';
DELETE FROM Settings WHERE name = 'system/autodetect/enable';

INSERT INTO Settings (name, value, datatype, position, internal) VALUES ('system/index/indexingTimeRecordLink', 'false', 2, 9209, 'n');

UPDATE metadata
    SET data = REGEXP_REPLACE(data, '[a-z]{3}\/thesaurus\.download\?ref=', 'api/registries/vocabularies/')
    WHERE data LIKE '%thesaurus.download?ref=%';

UPDATE settings SET value = '1' WHERE name = 'system/threadedindexing/maxthreads';

UPDATE Settings SET value='4.0.0' WHERE name='system/platform/version';
UPDATE Settings SET value='SNAPSHOT' WHERE name='system/platform/subVersion';


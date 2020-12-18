/*
 * Copyright (C) 2001-2020 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package v402;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.migration.DatabaseMigrationException;
import org.fao.geonet.utils.DateUtil;
import org.fao.geonet.utils.Log;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DateTimeMigrationTask extends DatabaseMigrationTask {
    private final static Logger LOGGER = Log.createLogger(Geonet.GEONETWORK + ".databasemigration");

    private static final int BATCH_SIZE = 50;

    @Override
    public void update(Connection connection) throws SQLException, DatabaseMigrationException {
        try {
            DataSource ds = applicationContext.getBean(DataSource.class);
            NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(ds);
            updateMetadataTable(jdbcTemplate);
            updateMetadataDraftTable(jdbcTemplate);
            updateHarvestHistoryTable(jdbcTemplate);
            updateLinkTable(jdbcTemplate);
            updateLinkStatusTable(jdbcTemplate);
            updateMetadataStatusTable(jdbcTemplate);
            updateMetadataValidationTable(jdbcTemplate);
            updateSourceTable(jdbcTemplate);
            updateUserTable(jdbcTemplate);
            updateMetadataUploadTable(jdbcTemplate);
            updateMetadataDownloadTable(jdbcTemplate);
        } catch (Exception e) {
            LOGGER.error("Error in DatabaseMigrationTask: " + e.getMessage());
            LOGGER.error(e);
            throw new DatabaseMigrationException(e);
        }
    }

    private void updateMetadataDraftTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + MetadataDraft.TABLENAME);

        final String getAllMetadataDraftSql = "SELECT * FROM " + MetadataDraft.TABLENAME;
        final String updateMetadataDraftSql = String
                .format("UPDATE %s SET %s=:CHANGE_DATE_VALUE, %s=:CREATE_DATE_VALUE WHERE %s=:ID_VALUE", MetadataDraft.TABLENAME,
                        MetadataDataInfo.CHANGE_DATE_COLUMN_NAME, MetadataDataInfo.CREATE_DATE_COLUMN_NAME, MetadataDraft.ID_COLUMN_NAME);
        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("CHANGE_DATE_VALUE", MetadataDataInfo.CHANGE_DATE_COLUMN_NAME);
        parametersMap.put("CREATE_DATE_VALUE", MetadataDataInfo.CREATE_DATE_COLUMN_NAME);

        Map<String, SqlParameter> primaryKeyMap = new LinkedHashMap<>();
        primaryKeyMap.put("ID_VALUE", new SqlParameter(MetadataDraft.ID_COLUMN_NAME, Types.INTEGER));
        processTable(jdbcTemplate, getAllMetadataDraftSql, updateMetadataDraftSql, primaryKeyMap, parametersMap);

    }

    private void updateMetadataDownloadTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + MetadataFileDownload.TABLE_NAME);

        final String getAllMetadataDownloadSql = "SELECT * FROM " + MetadataFileDownload.TABLE_NAME;
        final String updateMetadataDownloadSql = String
                .format("UPDATE %s SET %s=:DOWNLOAD_DATE_VALUE WHERE %s=:ID_VALUE", MetadataFileDownload.TABLE_NAME,
                        MetadataFileDownload.DOWNLOAD_DATE_COLUMN_NAME, MetadataFileDownload.ID_COLUMN_NAME);
        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("DOWNLOAD_DATE_VALUE", MetadataFileDownload.DOWNLOAD_DATE_COLUMN_NAME);

        Map<String, SqlParameter> primaryKey = new LinkedHashMap<>();
        primaryKey.put("ID_VALUE", new SqlParameter(MetadataFileDownload.ID_COLUMN_NAME, Types.INTEGER));

        processTable(jdbcTemplate, getAllMetadataDownloadSql, updateMetadataDownloadSql, primaryKey, parametersMap);

    }

    private void updateMetadataUploadTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + MetadataFileUpload.TABLE_NAME);

        final String getAllMetadataUploadSql = "SELECT * FROM " + MetadataFileUpload.TABLE_NAME;
        final String updateMetadataUploadSql = String
                .format("UPDATE %s SET %s=:UPLOAD_DATE_VALUE, %s=:DELETED_DATE_VALUE WHERE %s=:ID_VALUE", MetadataFileUpload.TABLE_NAME,
                        MetadataFileUpload.UPLOAD_DATE_COLUMN_NAME, MetadataFileUpload.DELETED_DATE_COLUMN_NAME,
                        MetadataFileUpload.ID_COLUMN_NAME);
        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("UPLOAD_DATE_VALUE", MetadataFileUpload.UPLOAD_DATE_COLUMN_NAME);
        parametersMap.put("DELETED_DATE_VALUE", MetadataFileUpload.DELETED_DATE_COLUMN_NAME);

        Map<String, SqlParameter> primaryKey = new LinkedHashMap<>();
        primaryKey.put("ID_VALUE", new SqlParameter(MetadataFileUpload.ID_COLUMN_NAME, Types.INTEGER));

        processTable(jdbcTemplate, getAllMetadataUploadSql, updateMetadataUploadSql, primaryKey, parametersMap);
    }

    private void updateUserTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + User.TABLE_NAME);

        final String getAllUserSql = "SELECT * FROM " + User.TABLE_NAME;
        final String updateUserSql = String
                .format("UPDATE %s SET %s=:LAST_LOGIN_DATE_VALUE WHERE %s=:ID_VALUE", User.TABLE_NAME, User.LAST_LOGIN_DATE_COLUMN_NAME,
                        User.ID_COLUMN_NAME);
        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("LAST_LOGIN_DATE_VALUE", User.LAST_LOGIN_DATE_COLUMN_NAME);

        Map<String, SqlParameter> primaryKey = new LinkedHashMap<>();
        primaryKey.put("ID_VALUE", new SqlParameter(User.ID_COLUMN_NAME, Types.INTEGER));
        processTable(jdbcTemplate, getAllUserSql, updateUserSql, primaryKey, parametersMap);
    }

    private void updateSourceTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + Source.TABLE_NAME);

        final String getAllSourceSql = "SELECT * FROM " + Source.TABLE_NAME;
        final String updateSourceSql = String
                .format("UPDATE %s SET %s=:CREATE_DATE_VALUE WHERE %s=:ID_VALUE", Source.TABLE_NAME, Source.CREATION_DATE_COLUMN_NAME,
                        Source.ID_COLUMN_NAME);

        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("CREATE_DATE_VALUE", Source.CREATION_DATE_COLUMN_NAME);

        Map<String, SqlParameter> primaryKeyMap = new LinkedHashMap<>();
        primaryKeyMap.put("ID_VALUE", new SqlParameter(Source.ID_COLUMN_NAME, Types.VARCHAR));
        processTable(jdbcTemplate, getAllSourceSql, updateSourceSql, primaryKeyMap, parametersMap);
    }

    private void updateMetadataValidationTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + MetadataValidation.TABLE_NAME);

        final String getAllMdValidationSql = "SELECT * FROM " + MetadataValidation.TABLE_NAME;
        final String updateMdValidationSql = String
                .format("UPDATE %s SET %s=:VALIDATION_DATE_VALUE WHERE %s=:METADATA_ID_VALUE AND %s=:VALIDATION_TYPE_VALUE",
                        MetadataValidation.TABLE_NAME, MetadataValidation.VALIDATION_DATE_COLUMN_NAME,
                        MetadataValidationId.METADATA_ID_COLUMN_NAME, MetadataValidationId.VALIDATION_TYPE_COLUMN_NAME);

        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("VALIDATION_DATE_VALUE", MetadataValidation.VALIDATION_DATE_COLUMN_NAME);

        Map<String, SqlParameter> primaryKeyMap = new LinkedHashMap<>();
        primaryKeyMap.put("METADATA_ID_VALUE", new SqlParameter(MetadataValidationId.METADATA_ID_COLUMN_NAME, Types.INTEGER));
        primaryKeyMap.put("VALIDATION_TYPE_VALUE", new SqlParameter(MetadataValidationId.VALIDATION_TYPE_COLUMN_NAME, Types.VARCHAR));

        processTable(jdbcTemplate, getAllMdValidationSql, updateMdValidationSql, primaryKeyMap, parametersMap);
    }

    private void updateMetadataStatusTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + MetadataStatus.TABLE_NAME);

        final String getAllMdStatusSql = String.format("SELECT * FROM %s", MetadataStatus.TABLE_NAME);
        String updateMdStatusSql = String
                .format("UPDATE %s SET %s=:CLOSE_DATE_VALUE, %s=:CHANGE_DATE_VALUE, %s=:DUE_DATE_VALUE WHERE %s=:CHANGE_DATE_ORIGINAL_VALUE AND %s=:METADATA_ID_VALUE AND %s=:STATUS_ID_VALUE AND %s=:USER_ID_VALUE",
                        MetadataStatus.TABLE_NAME, MetadataStatus.CLOSE_DATE_COLUMN_NAME, MetadataStatus.CHANGE_DATE_COLUMN_NAME,
                        MetadataStatus.DUE_DATE_COLUMN_NAME, MetadataStatus.CHANGE_DATE_COLUMN_NAME, MetadataStatus.METADATA_ID_COLUMN_NAME,
                        MetadataStatus.STATUS_ID_COLUMN_NAME, MetadataStatus.USER_ID_COLUMN_NAME);

        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("CLOSE_DATE_VALUE", MetadataStatus.CLOSE_DATE_COLUMN_NAME);
        parametersMap.put("CHANGE_DATE_VALUE", MetadataStatus.CHANGE_DATE_COLUMN_NAME);
        parametersMap.put("DUE_DATE_VALUE", MetadataStatus.DUE_DATE_COLUMN_NAME);

        Map<String, SqlParameter> primaryKeyMap = new LinkedHashMap<>();
        primaryKeyMap.put("CHANGE_DATE_ORIGINAL_VALUE", new SqlParameter(MetadataStatus.CHANGE_DATE_COLUMN_NAME, Types.VARCHAR));
        primaryKeyMap.put("METADATA_ID_VALUE", new SqlParameter(MetadataStatus.METADATA_ID_COLUMN_NAME, Types.INTEGER));
        primaryKeyMap.put("STATUS_ID_VALUE", new SqlParameter(MetadataStatus.STATUS_ID_COLUMN_NAME, Types.INTEGER));
        primaryKeyMap.put("USER_ID_VALUE", new SqlParameter(MetadataStatus.USER_ID_COLUMN_NAME, Types.INTEGER));

        processTable(jdbcTemplate, getAllMdStatusSql, updateMdStatusSql, primaryKeyMap, parametersMap);
    }

    private void updateLinkStatusTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + LinkStatus.TABLE_NAME);

        final String getAllLinkStatusSql = "SELECT * FROM " + LinkStatus.TABLE_NAME;
        final String updateLinkStatusSql = String
                .format("UPDATE %s SET %s=:CHECK_DATE_VALUE WHERE %s=:ID_VALUE", LinkStatus.TABLE_NAME, LinkStatus.CHECK_DATE_COLUMN_NAME,
                        LinkStatus.ID_COLUMN_NAME);

        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("CHECK_DATE_VALUE", LinkStatus.CHECK_DATE_COLUMN_NAME);

        Map<String, SqlParameter> primaryKeyMap = new LinkedHashMap<>();
        primaryKeyMap.put("ID_VALUE", new SqlParameter(LinkStatus.ID_COLUMN_NAME, Types.INTEGER));
        processTable(jdbcTemplate, getAllLinkStatusSql, updateLinkStatusSql, primaryKeyMap, parametersMap);
    }

    private void updateLinkTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + Link.TABLE_NAME);

        final String getAllLinkSql = "SELECT * FROM " + Link.TABLE_NAME;
        final String updateLinkSql = String
                .format("UPDATE %s SET %s=:LAST_CHECK_VALUE WHERE %s=:ID_VALUE", Link.TABLE_NAME, Link.LAST_CHECK_COLUMN_NAME,
                        Link.ID_COLUMN_NAME);

        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("LAST_CHECK_VALUE", Link.LAST_CHECK_COLUMN_NAME);

        Map<String, SqlParameter> primaryKeyMap = new LinkedHashMap<>();
        primaryKeyMap.put("ID_VALUE", new SqlParameter(Link.ID_COLUMN_NAME, Types.INTEGER));
        processTable(jdbcTemplate, getAllLinkSql, updateLinkSql, primaryKeyMap, parametersMap);
    }

    private void updateHarvestHistoryTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + HarvestHistory.TABLE_NAME);

        final String getAllHarvestHistorySql = "SELECT * FROM " + HarvestHistory.TABLE_NAME;
        final String updateHarvestHistorySql = String
                .format("UPDATE %s SET %s=:HARVEST_DATE_VALUE WHERE %s=:ID_VALUE", HarvestHistory.TABLE_NAME,
                        HarvestHistory.HARVEST_DATE_COLUMN_NAME, HarvestHistory.ID_COLUMN_NAME);

        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("HARVEST_DATE_VALUE", HarvestHistory.HARVEST_DATE_COLUMN_NAME);

        Map<String, SqlParameter> primaryKeyMap = new LinkedHashMap<>();
        primaryKeyMap.put("ID_VALUE", new SqlParameter(HarvestHistory.ID_COLUMN_NAME, Types.INTEGER));
        processTable(jdbcTemplate, getAllHarvestHistorySql, updateHarvestHistorySql, primaryKeyMap, parametersMap);
    }

    private void updateMetadataTable(NamedParameterJdbcTemplate jdbcTemplate) {
        LOGGER.info("Updating data of " + Metadata.TABLENAME);

        final String getAllMetadataSql = String
                .format("SELECT %s, %s, %s FROM %s", Metadata.ID_COLUMN_NAME, MetadataDataInfo.CHANGE_DATE_COLUMN_NAME,
                        MetadataDataInfo.CREATE_DATE_COLUMN_NAME, Metadata.TABLENAME);
        final String updateMetadataSql = String
                .format("UPDATE %s SET %s=:CHANGE_DATE_VALUE, %s=:CREATE_DATE_VALUE WHERE %s=:ID_VALUE", Metadata.TABLENAME,
                        MetadataDataInfo.CHANGE_DATE_COLUMN_NAME, MetadataDataInfo.CREATE_DATE_COLUMN_NAME, Metadata.ID_COLUMN_NAME);

        Map<String, String> parametersMap = new LinkedHashMap<>();
        parametersMap.put("CHANGE_DATE_VALUE", MetadataDataInfo.CHANGE_DATE_COLUMN_NAME);
        parametersMap.put("CREATE_DATE_VALUE", MetadataDataInfo.CREATE_DATE_COLUMN_NAME);

        Map<String, SqlParameter> primaryKeyMap = new LinkedHashMap<>();
        primaryKeyMap.put("ID_VALUE", new SqlParameter(Metadata.ID_COLUMN_NAME, Types.INTEGER));
        processTable(jdbcTemplate, getAllMetadataSql, updateMetadataSql, primaryKeyMap, parametersMap);
    }

    private void processTable(NamedParameterJdbcTemplate jdbcTemplate, String querySql, String updateSql,
            Map<String, SqlParameter> primaryKey, Map<String, String> timeParameters) {

        Map<String, Object>[] parameters = new Map[BATCH_SIZE];
        final int[] index = { 0 };
        RowCallbackHandler rch = rs -> {
            boolean skipRecord = false;
            try {
                Map<String, Object> updateParameters = new HashMap<>();
                for (Map.Entry<String, SqlParameter> entry : primaryKey.entrySet()) {
                    int sqlType = entry.getValue().getSqlType();
                    if (sqlType == Types.INTEGER) {
                        updateParameters.put(entry.getKey(), rs.getInt(entry.getValue().getName()));
                    } else if (sqlType == Types.BIGINT) {
                        updateParameters.put(entry.getKey(), rs.getLong(entry.getValue().getName()));
                    } else if (sqlType == Types.VARCHAR || sqlType == Types.LONGVARCHAR) {
                        updateParameters.put(entry.getKey(), rs.getString(entry.getValue().getName()));
                    }
                }
                for (Map.Entry<String, String> entry : timeParameters.entrySet()) {
                    String currentDbValue = rs.getString(entry.getValue());
                    String now = new ISODate().getDateAndTimeUtc();
                    String newDbValue =
                        StringUtils.isEmpty(currentDbValue)
                            ? now
                            : DateUtil.convertToISOZuluDateTime(currentDbValue);
                    if (newDbValue == null) {
                        // Can't parse record date, assigning now.
                        newDbValue = now;
                    }
                    updateParameters.put(entry.getKey(), newDbValue);
                }
                parameters[index[0]] = updateParameters;

            } catch (Exception e) {
                LOGGER.error("Error in DateTimeMigrationTask. Problem is: " + e.getMessage());
                LOGGER.error(e);
                skipRecord = true;
            }

            if (!skipRecord) {
                if ((index[0] + 1) % BATCH_SIZE == 0) {
                    jdbcTemplate.batchUpdate(updateSql, parameters);
                    Arrays.fill(parameters, null);
                    index[0] = 0;
                } else {
                    index[0] = index[0] + 1;
                }
            }
        };

        jdbcTemplate.query(querySql, rch);
        if (index[0] != 0) {
            int size = index[0];
            Map<String, Object>[] lastBatchParameters = ArrayUtils.subarray(parameters, 0, size);
            jdbcTemplate.batchUpdate(updateSql, lastBatchParameters);
        }

    }
}

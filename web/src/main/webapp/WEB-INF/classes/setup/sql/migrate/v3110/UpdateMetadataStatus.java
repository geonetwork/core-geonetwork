/*
 * Copyright (C) 2001-2018 Food and Agriculture Organization of the
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

package v3110;

import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.MetadataStatus_;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.utils.Log;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class to be executed during the migration which will update some new not null columns
 * on the metadataStatus table.
 *
 * Note: After this step is complete most of the changes should be in place however their may still be
 * some missing JPA settings that would initially fail due to these NOT NULL columns.
 *
 * It is recommended that after the initial startup and migration execution that the system be stopped and
 * restarted to ensure that all JPA settings are applied correclty.
 */
public class UpdateMetadataStatus extends DatabaseMigrationTask {

    private MetadataStatusRepository metadataStatusRepository;
    //private LanguageRepository languageRepository;
    private IMetadataUtils metadataUtils;

    /**
     * Override the setContext so do the autowire of the other fields.
     * @param applicationContext
     */
    @Override
    public void setContext(ApplicationContext applicationContext)  {
        super.setContext(applicationContext);
        metadataUtils = applicationContext.getBean(IMetadataUtils.class);
        metadataStatusRepository = applicationContext.getBean(MetadataStatusRepository.class);
        //languageRepository = applicationContext.getBean(LanguageRepository.class);
    }

    /**
     * Maing flow for the updating of the ID. UUID and TITLES fields
     * @param connection
     * @throws SQLException
     */
        @Override
    public void update(Connection connection) throws SQLException {

        final MetadataStatus metadataStatusObject = new MetadataStatus();

            DialectResolutionInfo dialectResolutionInfo = new DatabaseMetaDataDialectResolutionInfoAdapter(connection.getMetaData());
            Dialect dialect = new StandardDialectResolver().resolveDialect(dialectResolutionInfo);

            Log.debug(Geonet.DB, "UpdateMetadataStatus");

            // First add the id and uuid as nullable.
            addMissingColumn(connection, dialect);

            // Now update the id to sequence values so that all id's are not null.
            updatePKValue(connection, dialect);

            // Now update uuid and titles for the existing records
            updateOtherNewFields();
            // commit the changes
            connection.commit();
    }

    /**
     *  JPA will not be able to create the ID and UUID because they are not null
     *  So we need to add them as nullable initialy until we update all the data in the tables.
     *
     * @param connection
     * @param dialect - specific to each database - i.e. oracke, h2, postgresl...
     * @throws SQLException
     */
    private void addMissingColumn(final Connection connection, Dialect dialect ) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + MetadataStatus.TABLE_NAME + " " + dialect.getAddColumnString() + "  " + MetadataStatus_.id.getName() + " INTEGER NULL");
        } catch (Exception e) {
            // If there was an erro then we will log the error and continue.
            // Most likely cause is that the column already exists which should be fine.
            Log.error(Geonet.DB, "  Exception while adding new ID column to metadataStatus. " +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, e);
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + MetadataStatus.TABLE_NAME + " " + dialect.getAddColumnString() + "  " + MetadataStatus_.uuid.getName() + " VARCHAR(255) NULL");
        } catch (Exception e) {
            // If there was an erro then we will log the error and continue.
            // Most likely cause is that the column already exists which should be fine.
            Log.error(Geonet.DB, "  Exception while adding new ID column to metadataStatus. " +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, e);
        }
    }

    /**
     * Update the ID (primary key field) for the database to be equal to the sequence values.
     * @param connection
     * @param dialect - specific to each database - i.e. oracke, h2, postgresl...
     * @throws SQLException
     */

    private void updatePKValue(final Connection connection, Dialect dialect) throws SQLException {

        Statement statement = null;
        Integer rowcount=null;
        try {
            statement = connection.createStatement();
            rowcount = statement.executeUpdate("update " + MetadataStatus.TABLE_NAME +
                    " set " + MetadataStatus_.id.getName()  + " = " + dialect.getSelectSequenceNextValString(MetadataStatus.ID_SEQ_NAME) +
                    " where " + MetadataStatus_.id.getName()  + " IS NULL");
        } finally {
            if (statement != null) {
                statement.close();
            }
        }

        // Need to commit changes or they it will not be available to JPA calls.
        connection.commit();

        Log.info(Geonet.DB, "Migration: Updated " + rowcount + " primary key values for '" +  MetadataStatus.TABLE_NAME + "'");
    }

    /**
     * Update the new UUID and Titles field based on existing data.
     * @throws SQLException
     */
    private void updateOtherNewFields() throws SQLException {

        Pageable pageRequest = new PageRequest(0, 1000, new Sort("id"));
        int totalRowCount = 0;
        int updateRowCount = 0;
        int uuidRowCount = 0;
        int titleRowCount = 0;
        Map<Integer, Map<String, String>> titlesMap = new HashMap<>();
        Map<Integer, String> uuidMap = new HashMap<>();
        //List<Language> languages = languageRepository.findAll();
        Page<MetadataStatus> page;
        do {
            page = metadataStatusRepository.findAll(pageRequest);
            if (page != null && page.hasContent()) {
                for (MetadataStatus metadataStatus : page.getContent()) {
                    totalRowCount++;
                    if (metadataStatus.getUuid() == null || metadataStatus.getUuid().length() == 0 ||
                            metadataStatus.getTitles() == null || metadataStatus.getTitles().length() == 0) {

                        boolean changeflag = false;
                        if (metadataStatus.getUuid() == null || metadataStatus.getUuid().length() == 0) {
                            String uuid = uuidMap.get(metadataStatus.getMetadataId());
                            if (uuid == null) {
                                try {
                                    uuid = metadataUtils.getMetadataUuid(Integer.toString(metadataStatus.getMetadataId()));
                                    if (uuid != null) {
                                        uuidMap.put(metadataStatus.getMetadataId(), uuid);
                                    }
                                } catch (Exception e) {
                                    Log.error(Geonet.DATA_MANAGER, String.format(
                                            "Error locating uuid for metadata id: %d", +metadataStatus.getMetadataId()), e);
                                }
                                if (uuid == null || uuid.length() == 0) {
                                    Log.error(Geonet.DATA_MANAGER, String.format(
                                            "Could not located uuid for metadata id: %d", + metadataStatus.getMetadataId()));
                                }
                            }
                            if (uuid != null && uuid.length() > 0) {
                                metadataStatus.setUuid(uuid);
                                uuidRowCount++;
                                changeflag = true;
                            }
                        }
                        if (metadataStatus.getTitles() == null || metadataStatus.getTitles().length() == 0) {
                            Map<String, String> titles = titlesMap.get(metadataStatus.getMetadataId());
                            // Try to get the titles from the schema.
                            // Note: Schemas are not registered at this point so this is not possible.
                            //      Generated errors similar to the following
                            //             Schema not registered : dublin-core
                            // This would be the preferred option but not work so commenting this option for now.
                        /*if (titles == null) {
                            try {
                                titles = metadataUtils.extractTitles(Integer.toString(metadataStatus.getMetadataId()));
                                titlesMap.put(metadataStatus.getMetadataId(), titles);
                            } catch (Exception e) {
                                Log.error(Geonet.DATA_MANAGER, String.format(
                                        "Error locating titles for metadata id: %d", +metadataStatus.getMetadataId()), e);
                            }
                        }*/

                            // Try to get the titles from the index.
                            // Getting the following errors
                            //        There needs to be a ServiceContext in the thread local for this thread
                            // So skipping this one as well as it does not seem like the index are ready to be used at this point.
                        /*if (titles == null) {
                            Map<String, String> indexTitles = new LinkedHashMap<>();
                            try {
                                try {
                                    String title = LuceneSearcher.getMetadataFromIndexById(Geonet.DEFAULT_LANGUAGE, metadataStatus.getMetadataId() + "", "title");
                                    if (title != null) {
                                        indexTitles.put(Geonet.DEFAULT_LANGUAGE, title);
                                    }
                                } catch (Exception e) {
                                    Log.debug(Geonet.DATA_MANAGER, "Error getting title from index for metadata id '" + metadataStatus.getMetadataId() + "' for default language '" + Geonet.DEFAULT_LANGUAGE + "'", e);
                                }
                                for (Language lang : languages) {
                                    if (!Geonet.DEFAULT_LANGUAGE.equals(lang.getId())) {
                                        try {
                                            String title = LuceneSearcher.getMetadataFromIndexById(lang.getId(), metadataStatus.getMetadataId() + "", "title");
                                            if (title != null) {
                                                indexTitles.put(lang.getId(), title);
                                            }
                                        } catch (Exception e) {
                                            Log.debug(Geonet.DATA_MANAGER, "Error getting title from index for metadata id '" + metadataStatus.getMetadataId() + "' for language '" + lang.getId() + "'", e);
                                        }
                                    }
                                }
                                if (indexTitles.size() > 0) {
                                    titles = indexTitles;
                                    titlesMap.put(metadataStatus.getMetadataId(), titles);
                                }
                            } catch (Exception e) {
                                Log.error(Geonet.DATA_MANAGER, String.format(
                                        "Error locating titles for metadata id: %d", +metadataStatus.getMetadataId()), e);
                            }
                        }*/

                            // Last option - just use the metadata title field
                            // but I believe this is being depreciated as they all seem to be null
                            if (titles == null) {
                                Map<String, String> indexTitles = new LinkedHashMap<>();
                                try {
                                    String title = metadataUtils.getMetadataTitle(Integer.toString(metadataStatus.getMetadataId()));
                                    if (title != null) {
                                        indexTitles.put(Geonet.DEFAULT_LANGUAGE, title);
                                    }
                                } catch (Exception e) {
                                    Log.error(Geonet.DATA_MANAGER, String.format(
                                            "Error locating titles for metadata id: %d", +metadataStatus.getMetadataId()), e);
                                }
                            }

                            if (titles != null && titles.size() > 0) {
                                metadataStatus.setTitles(titles);
                                titleRowCount++;
                                changeflag = true;
                            }
                        }
                        if (changeflag = true) {
                            updateRowCount++;
                            metadataStatusRepository.save(metadataStatus);
                            changeflag=false;
                        }
                    }
                }
            }
            pageRequest=page.nextPageable();
        } while (pageRequest != null && page.hasContent());
        Log.info(Geonet.DB, "Migration: Updated " + updateRowCount + " records from a total of " + totalRowCount + " for talbe '" + MetadataStatus.TABLE_NAME +
                "'. (uuid:" + uuidRowCount + ", Titles:" + titleRowCount + ")");
    }
}

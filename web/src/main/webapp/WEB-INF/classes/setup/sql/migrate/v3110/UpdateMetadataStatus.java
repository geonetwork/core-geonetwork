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
 * restarted to ensure that all JPA settings are applied correctly.
 */
public class UpdateMetadataStatus extends DatabaseMigrationTask {

    private MetadataStatusRepository metadataStatusRepository;
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

            // finallize the change
            finalizeChanges(connection, dialect);
    }

    /**
     *  JPA will not be able to create the ID and UUID because they are not null
     *  So we need to add them as nullable initially until we update all the data in the tables.
     *
     * @param connection
     * @param dialect - specific to each database - i.e. oracke, h2, postgresl...
     * @throws SQLException
     */
    private void addMissingColumn(final Connection connection, Dialect dialect ) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + MetadataStatus.TABLE_NAME + " " + dialect.getAddColumnString() + "  " + MetadataStatus_.id.getName() + " INTEGER NULL");
        } catch (Exception e) {
            // If there was an error then we will log the error and continue.
            // Most likely cause is that the column already exists which should be fine.
            Log.error(Geonet.DB, "  Exception while adding new " + MetadataStatus_.id.getName() + " column to " + MetadataStatus.TABLE_NAME + ". " +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, e);
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + MetadataStatus.TABLE_NAME + " " + dialect.getAddColumnString() + "  " + MetadataStatus_.uuid.getName() + " VARCHAR(255) NULL");
        } catch (Exception e) {
            // If there was an erro then we will log the error and continue.
            // Most likely cause is that the column already exists which should be fine.
            Log.error(Geonet.DB, "  Exception while adding new " + MetadataStatus_.uuid.getName() + " column to " + MetadataStatus.TABLE_NAME + ". " +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, e);
        }

        connection.commit();
    }

    /**
     * Update the ID (primary key field) for the database to be equal to the sequence values.
     * @param connection
     * @param dialect - specific to each database - i.e. oracke, h2, postgresl...
     * @throws SQLException
     */

    private void updatePKValue(final Connection connection, Dialect dialect) throws SQLException {

        String HIBERNATE_SEQUENCE = getDatabaseObjectName(connection, "HIBERNATE_SEQUENCE");

        Statement statement = null;
        Integer rowcount = null;
        try {
            statement = connection.createStatement();
            try {
                rowcount = statement.executeUpdate("update " + MetadataStatus.TABLE_NAME +
                        " set " + MetadataStatus_.id.getName() + " = " + dialect.getSelectSequenceNextValString(MetadataStatus.ID_SEQ_NAME) +
                        " where " + MetadataStatus_.id.getName() + " IS NULL");
            } catch (SQLException e1) {
                try {
                    connection.rollback();
                    if (statement != null) {
                        statement.close();
                    }
                    statement = connection.createStatement();
                    rowcount = statement.executeUpdate("update " + MetadataStatus.TABLE_NAME +
                            " set " + MetadataStatus_.id.getName() + " = " + dialect.getSelectSequenceNextValString(HIBERNATE_SEQUENCE) +
                            " where " + MetadataStatus_.id.getName() + " IS NULL");
                } catch (SQLException e2) {
                    throw new SQLException("Error updating table \"" + MetadataStatus.TABLE_NAME + "." + MetadataStatus_.id.getName() +
                            "\" values to sequence value using sequence \"" + MetadataStatus.ID_SEQ_NAME + "\" and \"" + HIBERNATE_SEQUENCE + "\"\n" + e1.getMessage() + "\n", e2);
                }
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
        }

        // Need to commit changes or they it will not be available to JPA calls.
        connection.commit();

        Log.info(Geonet.DB, "Migration: Updated " + rowcount + " primary key values for '" + MetadataStatus.TABLE_NAME + "'");
    }

    /**
     * Update the new UUID and Titles field based on existing data.
     * @throws SQLException
     */
    private void updateOtherNewFields() throws SQLException {

        Pageable pageRequest = PageRequest.of(0, 1000, Sort.by("id"));
        int totalRowCount = 0;
        int updateRowCount = 0;
        int uuidRowCount = 0;
        int titleRowCount = 0;
        Map<Integer, LinkedHashMap<String, String>> titlesMap = new HashMap<>();
        Map<Integer, String> uuidMap = new HashMap<>();
        //List<Language> languages = languageRepository.findAll();
        Page<MetadataStatus> page;
        do {
            page = metadataStatusRepository.findAll(pageRequest);
            if (page != null && page.hasContent()) {
                for (MetadataStatus metadataStatus : page.getContent()) {
                    totalRowCount++;
                    if (metadataStatus.getUuid() == null || metadataStatus.getUuid().length() == 0 ||
                            metadataStatus.getTitles() == null || metadataStatus.getTitles().size() == 0) {

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
                        if (metadataStatus.getTitles() == null || metadataStatus.getTitles().size() == 0) {
                            LinkedHashMap<String, String> titles = titlesMap.get(metadataStatus.getMetadataId());
                            if (titles == null) {
                                LinkedHashMap<String, String> indexTitles = new LinkedHashMap<>();
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

    /**
     * ANow that the data should be populated alter ID and UUID fields and make them not null.
     * @param connection
     * @param dialect - specific to each database - i.e. oracle, h2, postgresl...
     */

    private void finalizeChanges(final Connection connection, Dialect dialect ) throws SQLException {

        // Now that data has been updated, add the not null constraints to fields that were previously created as null.
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + MetadataStatus.TABLE_NAME + " " + getAlterTableNotNullString(connection, MetadataStatus_.id.getName(), "INTEGER"));
        } catch (Exception e) {
            connection.rollback();
            // If there was an error then we will log the error and continue.
            // JPA will probably apply the constraint correctly on next restart.
            Log.error(Geonet.DB, "  Exception while modifying " + MetadataStatus_.id.getName() + " column of " + MetadataStatus.TABLE_NAME + " to NOT NULL. " +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, e);
        }

        connection.commit();

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + MetadataStatus.TABLE_NAME + " " + getAlterTableNotNullString(connection, MetadataStatus_.uuid.getName(), "VARCHAR(255)"));
        } catch (Exception e) {
            connection.rollback();
            // If there was an erro then we will log the error and continue.
            // Most likely cause is that the column already exists which should be fine.
            Log.error(Geonet.DB, "  Exception while modifying " + MetadataStatus_.uuid.getName() + " column of " + MetadataStatus.TABLE_NAME + " to NOT NULL. " +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, e);
        }

        connection.commit();

        String metadataStatusTableName = getDatabaseObjectName(connection, MetadataStatus.TABLE_NAME);

        // The next 3 statements are to drop the pk.  Not all databases will accept them all but hopefully at then
        // end of all these statements the pk has been dropped.

        // lets drop the old primary key constraint on the table
        String pkName =  null;
        try (Statement statement = connection.createStatement()) {
            DatabaseMetaData databaseMetaData  = connection.getMetaData();
            ResultSet pkResultSet  = databaseMetaData.getPrimaryKeys(connection.getCatalog(), connection.getSchema(), metadataStatusTableName);
            if (pkResultSet.next()) {
                pkName = pkResultSet.getString("PK_NAME");
            }
            if (pkName == null) {
                throw new RuntimeException("Error getting primary key constraint name for table " + MetadataStatus.TABLE_NAME);
            }
            statement.execute("ALTER TABLE " + MetadataStatus.TABLE_NAME + " drop constraint " + pkName);
        } catch (Exception e) {
            connection.rollback();
            Log.error(Geonet.DB, "  Exception while dropping old primary key constraint on table " + MetadataStatus.TABLE_NAME + ". Restart application and check logs for database errors.  If errors exists then may need to manually drop the primary key for this table." +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, "Full stack", e);
        }

        connection.commit();

        // lets drop the old primary key on the table
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + MetadataStatus.TABLE_NAME + " drop primary key");
        } catch (Exception e) {
            connection.rollback();
            Log.error(Geonet.DB, "  Exception while dropping old primary key on table " + MetadataStatus.TABLE_NAME + ". Restart application and check logs for database errors.  If errors exists then may need to manually drop the primary key for this table. " +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, "Full stack", e);
        }

        connection.commit();

        // lets add the new primary key
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + MetadataStatus.TABLE_NAME + " " + dialect.getAddPrimaryKeyConstraintString(MetadataStatus.TABLE_NAME + "Pk") + " (" + MetadataStatus_.id.getName() + ")");
        } catch (Exception e) {
            connection.rollback();
            Log.error(Geonet.DB, "  Exception while adding primary key on " + MetadataStatus_.id.getName() + " column for " + MetadataStatus.TABLE_NAME + ". " +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, "Full stack", e);
        }

        connection.commit();

        // lets add the FK relatedMetadataStatusId
        try (Statement statement = connection.createStatement()) {
            String[] fkColumns = new String[] {"relatedMetadataStatusId"};
            String[] pkColumns = new String[] {MetadataStatus_.id.getName()};

            statement.execute("ALTER TABLE " + MetadataStatus.TABLE_NAME + " " +
                dialect.getAddForeignKeyConstraintString(MetadataStatus.TABLE_NAME + "RelMdStatusIdFk",
                    fkColumns, MetadataStatus.TABLE_NAME, pkColumns, true));
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
            // If there was an error then we will log the error and continue.
            // Most likely cause is that the column already exists which should be fine.
            Log.error(Geonet.DB, "  Exception while adding foreign key on relatedMetadataStatusId column of " + MetadataStatus.TABLE_NAME + ". " +
                "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, e);
        }
    }

    private String getDatabaseObjectName(final Connection connection, String objectName) throws SQLException {
        // postgres uses lowercase names while other databases use uppercase.
        if (connection.getMetaData().getDriverName().matches("(?i).*postgres.*")) {
            return objectName.toLowerCase();
        } else {
            return objectName.toUpperCase();
        }
    }

    private String getAlterTableNotNullString(final Connection connection, String columnName, String datatype) throws SQLException {
        // postgres set to not null.
        if (connection.getMetaData().getDriverName().matches("(?i).*postgres.*")) {
            return "alter column " + columnName.toLowerCase() + " SET NOT NULL";
        } else {
            return "modify " + columnName.toUpperCase() + " " + datatype + " NOT NULL";
        }
    }

}

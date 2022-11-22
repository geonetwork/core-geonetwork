package v400;

/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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


import org.apache.commons.lang.StringUtils;
import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataDraft;
import org.fao.geonet.utils.Log;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.tool.schema.extract.spi.ExtractionContext;
import org.hibernate.tool.schema.extract.spi.SequenceInformation;
import org.hibernate.tool.schema.extract.spi.SequenceInformationExtractor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.persistence.*;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;

/**
 * Class to be executed during the migration which will update all the sequences
 * to be greater than the max id for the table.
 *
 * This works be locating all the @SequenceGenerator annotation and determining the name
 * of the sequence, table and column id from the class.  It will use this information
 * to get the max value of the id for the table and then update the sequence.
 */
public class UpdateAllSequenceValueToMax extends DatabaseMigrationTask {
    @Override
    public void update(Connection connection) throws SQLException {
        Log.debug(Geonet.DB, "UpdateAllSequenceValueToMax");

        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(true);

        scanner.addIncludeFilter(new AnnotationTypeFilter(SequenceGenerator.class));

        for (BeanDefinition bd : scanner.findCandidateComponents("org.fao.geonet.domain")) {
            Class<?> cl;

            try {
                cl = Class.forName(bd.getBeanClassName());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            processClass(connection, cl);
        }

        // For metadata the auto detect will not work because they are based on an abstract class so they need to be
        // added manually.
        updateTableSequence(connection, Metadata.TABLENAME, "id", AbstractMetadata.ID_SEQ_NAME);
        updateTableSequence(connection, MetadataDraft.TABLENAME, "id", AbstractMetadata.ID_SEQ_NAME);
    }

    public static void processClass(final Connection connection, final Class<?> cl) throws SQLException {

        // Get sequence name
        String sequenceName = cl.getAnnotation(SequenceGenerator.class).sequenceName();
        String name = cl.getAnnotation(SequenceGenerator.class).name();
        if (StringUtils.isEmpty(sequenceName) && StringUtils.isEmpty(name)) {
            Log.debug(Geonet.DB, "  Sequence named " + cl.getAnnotation(SequenceGenerator.class).name() + " does not have a sequenceName or name field. Skipping sequence");
            return;
        }
        if (StringUtils.isEmpty(sequenceName)) {
            sequenceName = name;
        }

        // Get table name
        String tablename;
        if (cl.isAnnotationPresent(Table.class)) {
            tablename = cl.getAnnotation(Table.class).name();
            if (StringUtils.isEmpty(tablename)) {
                tablename = cl.getSimpleName();
            }
        } else {
            tablename = cl.getSimpleName();
        }

        // Get primary key column name
        String keyColumnName = null;
        for (Field field : cl.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                if (field.isAnnotationPresent(Column.class)) {
                    keyColumnName = field.getAnnotation(Column.class).name();
                }
                if (keyColumnName == null || keyColumnName.isEmpty()) {
                    keyColumnName = field.getName();
                }
            }
        }
        if (keyColumnName == null || keyColumnName.isEmpty()) {
            for (Method method : cl.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Id.class)) {
                    if (method.isAnnotationPresent(Column.class)) {
                        keyColumnName = method.getAnnotation(Column.class).name();
                    }
                    if (keyColumnName == null || keyColumnName.isEmpty()) {
                        keyColumnName = getFieldName(method);
                    }
                }
            }
        }
        if (keyColumnName == null || keyColumnName.isEmpty()) {
            keyColumnName = "id";
        }

        // Update the sequence.
        updateTableSequence(connection, tablename, keyColumnName, sequenceName);
    }

    private static void updateTableSequence(final Connection connection, final String tableName, final String keyColumnName, final String sequenceName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            final String tableMaxIdSQL = "SELECT max(" + keyColumnName + ") as NB FROM " + tableName;

            ResultSet tableMaxIdResultSet = statement.executeQuery(tableMaxIdSQL);
            long maxId = 0;
            try {
                if (tableMaxIdResultSet.next()) {
                    maxId = tableMaxIdResultSet.getLong(1);
                }

                if (maxId == 0L) {
                    Log.debug(Geonet.DB, "  Table " + tableName + " does not have any data. Skipping sequence " + sequenceName + " update");
                    return;
                }

                Log.debug(Geonet.DB, "  Max id for table " + tableName + " : " + maxId + ". Related sequence: " + sequenceName);
            } finally {
                tableMaxIdResultSet.close();
            }

            // Update the sequence to the max id.
            updateSequence(connection, sequenceName, maxId);
        } catch (Exception e) {
            Log.debug(Geonet.DB, "  Exception while updating sequence " + sequenceName + " . " +
                "Error is: " + e.getMessage());
            Log.error(Geonet.DB, e);
        }
    }

    private static void updateSequence(final Connection connection, final String sequenceName, long desiredVal) throws SQLException {
        DialectResolutionInfo dialectResolutionInfo = new DatabaseMetaDataDialectResolutionInfoAdapter(connection.getMetaData());
        Dialect dialect = new StandardDialectResolver().resolveDialect(dialectResolutionInfo);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        long currval = 0L;
        long loopCount = 0L;
        try {
            // For Postgres, use setval
            if (connection.getMetaData().getDriverName().matches("(?i).*postgres.*")) {
                preparedStatement = connection.prepareStatement("SELECT setval(?, ?);");
                preparedStatement.setString(1, sequenceName);
                preparedStatement.setLong(2, desiredVal);

                preparedStatement.executeQuery();

                Log.info(Geonet.DB, "  Sequence " + sequenceName + " updated. Currval: " + desiredVal);
            } else {
                preparedStatement = connection.prepareStatement(dialect.getSequenceNextValString(sequenceName));

                // There may be a better way to adjust the sequence other than looping though them
                // but this is the only database agnostic approach that could be found at the moment..
                while (currval < desiredVal) {
                    resultSet = preparedStatement.executeQuery();
                    if (resultSet.next()) {
                        currval = resultSet.getLong(1);
                        loopCount++;
                    } else {
                        break;
                    }
                    resultSet.close();
                    resultSet = null;
                }
                Log.info(Geonet.DB, "  Sequence " + sequenceName + " updated. Increased by: " + loopCount + ".  Currval: " + currval);
            }
        } catch (SQLException e) {
            throw e;
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    public static String getFieldName(Method method) {
        try {
            Class<?> clazz = method.getDeclaringClass();
            BeanInfo info = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] props = info.getPropertyDescriptors();
            for (PropertyDescriptor pd : props) {
                if (method.equals(pd.getWriteMethod()) || method.equals(pd.getReadMethod())) {
                    return pd.getName();
                }
            }
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}

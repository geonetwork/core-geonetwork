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
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.GeonetEntity;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.repository.MetadataStatusRepository;
import org.fao.geonet.utils.Log;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to be executed during the migration which will update all the sequences
 * to be greater than the max id for the table.
 *
 * This works be locating all the @SequenceGenerator annotation and determining the name
 * of the sequence, table and column id from the class.  It will use this information
 * to get the max value of the id for the table and then update the sequence.
 */
public class UpdateMetadataStatus extends DatabaseMigrationTask {

    @Embeddable
    @Access(AccessType.PROPERTY)
    class MetadataStatusId implements Serializable {
        private static final long serialVersionUID = -4395314364468537427L;
        private ISODate _changedate;
        private int _metadataId;
        private int _statusId;
        private int _userId;

        @AttributeOverride(name = "dateAndTime", column = @Column(name = "changeDate", nullable = false, length = 30))
        public ISODate getChangeDate() {
            return _changedate;
        }

        public void setChangeDate(ISODate changedate) {
            this._changedate = changedate;
        }

        public int getMetadataId() {
            return _metadataId;
        }

        public void setMetadataId(int metadataId) {
            this._metadataId = metadataId;
        }

        public int getStatusId() {
            return _statusId;
        }

        public void setStatusId(int statusId) {
            this._statusId = statusId;
        }

        public int getUserId() {
            return _userId;
        }

        public void setUserId(int userId) {
            this._userId = userId;
        }

    }

    @Entity
    @Access(AccessType.PROPERTY)
    @Table(name = MetadataStatus.MetadataStatusTableName,
            indexes = {
                    @Index(name="idx_metadatastatus_metadataid", columnList = "metadataid"),
                    @Index(name="idx_metadatastatus_statusid", columnList = "statusid"),
                    @Index(name="idx_metadatastatus_userid", columnList = "userid"),
                    @Index(name="idx_metadatastatus_changedate", columnList = "changedate")
            }
    )
    class MetadataStatus extends GeonetEntity {
        public final static String MetadataStatusTableName = "MetadataStatus";
        public final static String MetadataStatusSequenceName = "metadataStatus_id_seq";
        public final static String MetadataStatusNewIdName = "id";
        public final static String MetadataStatusNewUUIDName = "uuid";


        private MetadataStatusId oldId = new MetadataStatusId();
        private long id;
        private String uuid;

        @EmbeddedId
        public MetadataStatusId getOldId() {
            return oldId;
        }

        public void setOldId(MetadataStatusId oldId) {
            this.oldId = oldId;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }
    @Autowired
    private MetadataStatusRepository statusRepository;

    @Autowired
    private IMetadataUtils metadataUtils;

    @Override
    public void update(Connection connection) throws SQLException {
        final MetadataStatus metadataStatusObject = new MetadataStatus();

        Log.debug(Geonet.DB, "UpdateMetadataStatus");

        // First add the id and uuid as nullable.
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + metadataStatusObject.MetadataStatusTableName + " ADD COLUMN " + metadataStatusObject.MetadataStatusNewIdName + " INTEGER NULL");
        } catch (Exception e) {
            // If there was an erro then we will log the error and continue.
            // Most likely cause is that the column already exists which should be fine.
            Log.error(Geonet.DB, "  Exception while adding new ID column to metadataStatus. " +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, e);
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + metadataStatusObject.MetadataStatusTableName + " ADD COLUMN " + metadataStatusObject.MetadataStatusNewUUIDName + " VARCHAR(255) NULL");
        } catch (Exception e) {
            // If there was an erro then we will log the error and continue.
            // Most likely cause is that the column already exists which should be fine.
            Log.error(Geonet.DB, "  Exception while adding new ID column to metadataStatus. " +
                    "Error is: " + e.getMessage());
            Log.debug(Geonet.DB, e);
        }

        // Now update the id to sequence values so that all id are not null.
        updateTableNullValues(connection, metadataStatusObject);

        // finally lets set the column to not null.
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + metadataStatusObject.MetadataStatusTableName + "  MODIFY COLUMN  ID NOT NULL;");
        } catch (Exception e) {
            Log.debug(Geonet.DB, "  Exception while modifying ID column for metadataStatus to NOT NULL. " +
                    "Error is: " + e.getMessage());
            Log.error(Geonet.DB, e, e);
        }
    }

    private void updateTableNullValues(final Connection connection, final MetadataStatus metadataStatusObject) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            final String tableCountNullSQL = "SELECT count(*) as NB FROM " + metadataStatusObject.MetadataStatusTableName + " where " + metadataStatusObject.MetadataStatusNewIdName + " is NULL";

            ResultSet tableCountNullResultSet = statement.executeQuery(tableCountNullSQL);
            long countNull = 0;
            try {
                if (tableCountNullResultSet.next()) {
                    countNull = tableCountNullResultSet.getLong(1);
                }

                if (countNull == 0L) {
                    Log.debug(Geonet.DB, "  Table " + metadataStatusObject.MetadataStatusTableName  + " does not have any data. Skipping");
                    return;
                }

                Log.debug(Geonet.DB, "  table " + metadataStatusObject.MetadataStatusTableName  + " contains " + countNull + " records to be updated");
            } finally {
                tableCountNullResultSet.close();
            }

            int processedCount=0;
            int BATCHSIZE=100;
            int pageNumber=0;

            EntityManagerFactory emf=Persistence.createEntityManagerFactory("Migration");
            EntityManager em=emf.createEntityManager();

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<MetadataStatus> cq = cb.createQuery(MetadataStatus.class);
            Root<MetadataStatus> rootEntry = cq.from(MetadataStatus.class);
            CriteriaQuery<MetadataStatus> all = cq.where().select(rootEntry);
            TypedQuery<MetadataStatus> allQuery = em.createQuery(all);


            while (processedCount < countNull) {
                List<Long> batchList;
                if ((countNull-processedCount) > BATCHSIZE) {
                    batchList = getSequenceValue(connection, metadataStatusObject.MetadataStatusSequenceName, BATCHSIZE);
                } else {
                    batchList = getSequenceValue(connection, metadataStatusObject.MetadataStatusSequenceName, (int)(countNull-processedCount));
                }

                allQuery.setFirstResult(pageNumber);
                allQuery.setMaxResults(BATCHSIZE);
                List<MetadataStatus> metadataStatusList = allQuery.getResultList();
                allQuery.getSingleResult();

                int x=0;
                em.getTransaction().begin();
                for (MetadataStatus metadataStatus : metadataStatusList) {
                    metadataStatus.id = batchList.get(x);
                    // set metadata uuid.
                    try {
                    metadataStatus.setUuid(metadataUtils.getMetadataUuid(Integer.toString(metadataStatus.getOldId().getMetadataId())));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                em.getTransaction().commit();
            }

        }
    }

    private List<Long> getSequenceValue(final Connection connection, final String sequenceName, int numSeq) throws SQLException {
        DialectResolutionInfo dialectResolutionInfo = new DatabaseMetaDataDialectResolutionInfoAdapter(connection.getMetaData());
        Dialect dialect = new StandardDialectResolver().resolveDialect(dialectResolutionInfo);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Long> sequenceList = new ArrayList<>();
        try {
            preparedStatement = connection.prepareStatement(dialect.getSequenceNextValString(sequenceName));
            for (int x=0; x < numSeq; x++) {
                resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    sequenceList.add(resultSet.getLong(1));
                } else {
                    break;
                }
                resultSet.close();
                resultSet = null;
            }
        } finally {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (resultSet != null) {
                resultSet.close();
            }
        }

        Log.debug(Geonet.DB, "  Retrieved " + sequenceList.size() + " sequence values from  " + sequenceName);

        return sequenceList;
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

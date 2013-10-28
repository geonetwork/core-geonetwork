package org.fao.geonet;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
 
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.sql.Connection;
import java.sql.SQLException;
 
public class HibernateExtendedJpaDialect extends HibernateJpaDialect {
 
    private Logger logger = LoggerFactory.getLogger(HibernateExtendedJpaDialect.class);

    @Autowired
    private ApplicationContext _applicationContext;

    /**
     * This method is overridden to set custom isolation levels on the connection
     * @param entityManager
     * @param definition
     * @return
     * @throws PersistenceException
     * @throws SQLException
     * @throws TransactionException
     */
    @Override
    public Object beginTransaction(final EntityManager entityManager,
            final TransactionDefinition definition) throws PersistenceException,
            SQLException, TransactionException {
        Session session = (Session) entityManager.getDelegate();
        if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
            getSession(entityManager).getTransaction().setTimeout(definition.getTimeout());
        }
 
        entityManager.getTransaction().begin();
        logger.debug("Transaction started");
 
        session.doWork(new Work() {
 
            public void execute(Connection connection) throws SQLException {
                logger.debug("The connection instance is {}", connection);
                final String dialect = _applicationContext.getBean("jpaVendorAdapterDatabaseParam", String.class);
                if (dialect.equals("H2")) {
                    // ignore isolation and propogation for H2.
                    logger.debug("H2 doesn't deal well with isolation or propogation at the moment so we are ignoring them", connection);
                } else {
                    logger.debug("The isolation level of the connection is {} and the isolation level set on the transaction is {}",
                            connection.getTransactionIsolation(), definition.getIsolationLevel());
                    DataSourceUtils.prepareConnectionForTransaction(connection, definition);
                }
            }
        });
 
        return prepareTransaction(entityManager, definition.isReadOnly(), definition.getName());
    }
 
}
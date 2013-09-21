package org.fao.geonet.monitor.health;

import com.yammer.metrics.core.HealthCheck;
import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.repository.SettingRepository;

import javax.transaction.TransactionManager;

/**
 * Checks to ensure that the database is accessible and readable
 * <p/>
 * User: jeichar
 * Date: 3/26/12
 * Time: 9:01 AM
 */
public class DatabaseHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck("Database Connection") {
            @Override
            protected Result check() throws Exception {
                final TransactionManager transactionManager = context.getBean(TransactionManager.class);
                transactionManager.begin();
                try {
                    context.getBean(SettingRepository.class).count();
                    return Result.healthy();
                } catch (Throwable e) {
                    return Result.unhealthy(e);
                } finally {
                    if (transactionManager != null)
                        transactionManager.rollback();
                }

            }
        };
    }
}

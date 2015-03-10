package org.fao.geonet.monitor.health;

import com.yammer.metrics.core.HealthCheck;
import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Verifies that all metadata have been correctly indexed (without errors)
 * <p/>
 * User: jeichar
 * Date: 3/26/12
 * Time: 9:01 AM
 */
public class DeadlockedThreadsHealthCheck implements HealthCheckFactory {
    public HealthCheck create(final ServiceContext context) {
        return new HealthCheck("Deadlocked Threads") {
            @Override
            protected Result check() throws Exception {
                ThreadMXBean bean = ManagementFactory.getThreadMXBean();

                final long[] deadlockedThreads = bean.findDeadlockedThreads();
                if (deadlockedThreads != null && deadlockedThreads.length > 0) {
                    return Result.unhealthy("Found "+deadlockedThreads.length+" deadlocked threads");
                } else {
                    return Result.healthy();
                }
            }
        };
    }
}

package jeeves.monitor.healthcheck;

import com.sun.management.UnixOperatingSystemMXBean;
import com.yammer.metrics.core.HealthCheck;
import jeeves.monitor.HealthCheckFactory;
import jeeves.server.context.ServiceContext;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Checks the number of free and used file handles and ensures that 1% are free.
 * 
 * Only works on unix-based systems.  On windows it always returns a healthy result
 * 
 * @author jeichar
 */
public class FreeFileHandlesHealthCheck implements HealthCheckFactory {

    @Override
    public HealthCheck create(ServiceContext context) {
        return new HealthCheck("Free file handles") {
            @Override
            protected Result check() throws Exception {
                try {
                    OperatingSystemMXBean osMbean = ManagementFactory.getOperatingSystemMXBean();
                    if(osMbean instanceof UnixOperatingSystemMXBean) {
                        UnixOperatingSystemMXBean unixMXBean = (UnixOperatingSystemMXBean) osMbean;
                        long free = unixMXBean.getMaxFileDescriptorCount() - unixMXBean.getOpenFileDescriptorCount();
                        double fivePercent = Math.max(2.0, ((double) unixMXBean.getMaxFileDescriptorCount()) * 0.01);
                        if (free < fivePercent) {
                            Result.unhealthy("There are insufficient free file handles. Connections free:" + free);
                        }
                    }
                    return Result.healthy();
                } catch (Exception e) {
                    return Result.unhealthy(e);
                }
            }
        };
    }

}

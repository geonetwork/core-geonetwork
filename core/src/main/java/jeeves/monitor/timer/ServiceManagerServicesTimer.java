package jeeves.monitor.timer;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

import java.util.concurrent.TimeUnit;

/**
 * Tracks the time that services (minus output dispatch and guiServices) take to execute
 *
 * User: jeichar
 * Date: 4/3/12
 * Time: 8:50 AM
 */
public class ServiceManagerServicesTimer implements MetricsFactory<Timer> {
    public Timer create(MetricsRegistry metricsRegistry, ServiceContext context) {
        return metricsRegistry.newTimer(ServiceManager.class, "Service_Execution_Timer", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }
}

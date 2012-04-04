package jeeves.monitor.timer;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;

import java.util.concurrent.TimeUnit;

/**
 * Tracks the time that a resource is kept open
 *
 * User: jeichar
 * Date: 4/3/12
 * Time: 8:50 AM
 */
public class ResourceManagerResourceIsOpenTimer implements MetricsFactory<Timer> {
    public Timer create(MetricsRegistry metricsRegistry, ServiceContext context) {
        return metricsRegistry.newTimer(ResourceManager.class, "Resource_is_Open_Timer", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }
}

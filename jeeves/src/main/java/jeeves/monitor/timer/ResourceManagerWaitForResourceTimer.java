package jeeves.monitor.timer;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;

import java.util.concurrent.TimeUnit;

/**
 * Tracks the time it takes to perform an open of a resource.  Idea is that if this gets high there need to be
 * more database connections or there is a bug leaving connections open
 *
 * User: jeichar
 * Date: 4/3/12
 * Time: 8:50 AM
 */
public class ResourceManagerWaitForResourceTimer implements MetricsFactory<Timer> {
    public Timer create(MetricsRegistry metricsRegistry, ServiceContext context) {
        return metricsRegistry.newTimer(ResourceManager.class, "Wait_for_Resource_Timer", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }
}

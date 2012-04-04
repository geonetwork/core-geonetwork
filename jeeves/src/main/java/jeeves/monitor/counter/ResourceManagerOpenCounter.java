package jeeves.monitor.counter;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;

import java.util.concurrent.TimeUnit;

/**
 * Tracks the number of resources that have been opened by the ResourceManager and have yet to be closed
 *
 * User: jeichar
 * Date: 4/3/12
 * Time: 8:50 AM
 */
public class ResourceManagerOpenCounter implements MetricsFactory<Counter> {
    public Counter create(MetricsRegistry metricsRegistry, ServiceContext context) {
        return metricsRegistry.newCounter(ResourceManager.class, "Open_Resources");
    }
}

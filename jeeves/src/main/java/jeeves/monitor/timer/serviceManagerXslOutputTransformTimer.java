package jeeves.monitor.timer;

import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;

import java.util.concurrent.TimeUnit;

/**
 * Tracks the time spend performing output dispatch take to execute
 *
 * User: jeichar
 * Date: 4/3/12
 * Time: 8:50 AM
 */
public class ServiceManagerXslOutputTransformTimer implements MetricsFactory<Timer> {
    public Timer create(MetricsRegistry metricsRegistry, ServiceContext context) {
        return metricsRegistry.newTimer(ServiceManager.class, "Output_Xsl_Transform_Timer", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
    }
}

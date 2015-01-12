package jeeves.monitor;

import com.yammer.metrics.core.MetricsRegistry;
import jeeves.server.context.ServiceContext;

/**
 * Class for creating metrics objects (http://metrics.codahale.com/)
 * which only require a ServiceContext object for performing the check.
 * The health check object will be created and added after AppHandler
 * is created and started.
 *
 * It will create HealthCheck or Gauge objects most likely and will
 * be a plugin type model.  In the config.xml there should be a section
 *
 * <monitors></monitors>
 *
 * That declares what factories should be created.  See config-monitoring.xml
 * for examples and documentation.
 *
 * User: jeichar
 * Date: 3/29/12
 * Time: 3:29 PM
 */
public interface MetricsFactory<Type> {
    /**
     * Create a metrics object of type Type
     * @param context
     * @return
     */
    Type create(MetricsRegistry metricsRegistry, ServiceContext context);
}

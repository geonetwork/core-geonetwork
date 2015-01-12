package jeeves.monitor;

import com.yammer.metrics.core.HealthCheck;
import jeeves.server.context.ServiceContext;

/**
 * Class for creating HealthCheck objects (http://metrics.codahale.com/)
 * which only require a ServiceContext object for performing the check.
 * The health check object will be created and added after AppHandler
 * is created and started.
 *
 * The HealthCheck will defined in the config.xml in the monitors section
 *
 * That declares what factories should be created.  See config-monitoring.xml
 * for examples and documentation.
 *
 * User: jeichar
 * Date: 3/29/12
 * Time: 3:29 PM
 */
public interface HealthCheckFactory {
    /**
     * Create a HealthCheck object of type Type
     * @param context
     * @return
     */
    HealthCheck create(ServiceContext context);
}

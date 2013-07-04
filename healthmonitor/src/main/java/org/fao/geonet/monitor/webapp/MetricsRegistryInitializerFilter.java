package org.fao.geonet.monitor.webapp;

import com.yammer.metrics.core.HealthCheckRegistry;
import com.yammer.metrics.core.MetricsRegistry;
import jeeves.monitor.MonitorManager;

import javax.servlet.*;
import java.io.IOException;

/**
 * Sets the metrics registries earlier enough so all geonetwork and metrics will get and use the same instance.
 *
 * User: jeichar
 * Date: 4/17/12
 * Time: 5:32 PM
 */
public class MetricsRegistryInitializerFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext context = filterConfig.getServletContext();
        context.setAttribute(MonitorManager.HEALTH_CHECK_REGISTRY, new HealthCheckRegistry());
        context.setAttribute(MonitorManager.CRITICAL_HEALTH_CHECK_REGISTRY, new HealthCheckRegistry());
        context.setAttribute(MonitorManager.WARNING_HEALTH_CHECK_REGISTRY, new HealthCheckRegistry());
        context.setAttribute(MonitorManager.EXPENSIVE_HEALTH_CHECK_REGISTRY, new HealthCheckRegistry());

        MetricsRegistry metricsRegistry = new MetricsRegistry();
        context.setAttribute(MonitorManager.METRICS_REGISTRY, metricsRegistry);
        context.setAttribute(DefaultWebappMetricsFilter.REGISTRY_ATTRIBUTE, metricsRegistry);
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request,response);
    }

    public void destroy() {
    }
}

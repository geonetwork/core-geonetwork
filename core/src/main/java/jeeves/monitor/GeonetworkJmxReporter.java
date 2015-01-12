package jeeves.monitor;

import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.JmxReporter;

/**
 * A jmx reporter that appends the webapp name so multiple geonetwork instances can exist in same JVM
 * 
 * @author jeichar
 */
public class GeonetworkJmxReporter extends JmxReporter {

    private String webappName;

    public GeonetworkJmxReporter(MetricsRegistry registry, String webappName) {
        super(registry);
        this.webappName = webappName;
    }

    @Override
    public void onMetricAdded(MetricName name, Metric metric) {
        super.onMetricAdded(newMetricName(name), metric);
    }

    @Override
    public void onMetricRemoved(MetricName name) {
        super.onMetricRemoved(newMetricName(name));
    }

    private MetricName newMetricName(MetricName name) {
        return new MetricName(name.getGroup(), name.getType(), name.getName(), name.getScope(), webappName+"."+name.getMBeanName());
    }
}

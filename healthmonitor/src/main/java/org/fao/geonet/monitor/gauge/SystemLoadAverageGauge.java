package org.fao.geonet.monitor.gauge;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Abstract super class for all Gauges that use the Main database Stats.
 *
 * User: jeichar
 * Date: 4/5/12
 * Time: 10:09 AM
 */
public class SystemLoadAverageGauge implements MetricsFactory<Gauge<Double>> {

    public Gauge<Double> create(MetricsRegistry metricsRegistry, final ServiceContext context) {
        return metricsRegistry.newGauge(OperatingSystemMXBean.class, "systemLoadAverage", new Gauge<Double>() {
            @Override
            public Double value() {
                OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
                return operatingSystemMXBean.getSystemLoadAverage();
            }
        });
    }
}

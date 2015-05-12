package org.fao.geonet.monitor.gauge;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * @author Jesse on 5/8/2015.
 */
public abstract class AbstractOSMxBeanGauge<T> implements MetricsFactory<Gauge<T>> {

    private final String name;

    public AbstractOSMxBeanGauge(String name) {
        this.name = name;
    }

    @Override
    public Gauge<T> create(MetricsRegistry metricsRegistry, ServiceContext context) {
        return metricsRegistry.newGauge(OperatingSystemMXBean.class, name, new Gauge<T>() {

            @Override
            public T value() {
                OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
                if (operatingSystemMXBean instanceof com.sun.management.OperatingSystemMXBean) {
                    com.sun.management.OperatingSystemMXBean mxBean = (com.sun.management.OperatingSystemMXBean)
                            operatingSystemMXBean;

                    return getValue(mxBean);
                }
                return getDefaultValue();
            }

        });
    }

    protected abstract T getDefaultValue();
    protected abstract T getValue(com.sun.management.OperatingSystemMXBean operatingSystemMXBean);
}

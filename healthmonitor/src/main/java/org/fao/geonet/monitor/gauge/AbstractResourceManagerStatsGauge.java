package org.fao.geonet.monitor.gauge;

import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;
import jeeves.server.resources.ResourceManager;
import jeeves.server.resources.Stats;
import org.fao.geonet.constants.Geonet;

/**
 * Abstract super class for all Gauges that use the Main database Stats.
 *
 * User: jeichar
 * Date: 4/5/12
 * Time: 10:09 AM
 */
public abstract class AbstractResourceManagerStatsGauge<T> implements MetricsFactory<Gauge<T>> {
    private final String name;

    protected abstract T valueImpl(Stats stats);
    protected abstract T defaultValue();

    protected AbstractResourceManagerStatsGauge(String name) {
        this.name = name;
    }

    public Gauge<T> create(MetricsRegistry metricsRegistry, final ServiceContext context) {
        return metricsRegistry.newGauge(ResourceManager.class, name,new Gauge<T>() {
            @Override
            public T value() {
                T finalValue;
                try {
                    T val = valueImpl(context.getResourceManager().getStats(Geonet.Res.MAIN_DB));
                    if(val == null) {
                        finalValue = defaultValue();
                    } else {
                        finalValue = val;
                    }
                } catch (Exception e) {
                    finalValue = defaultValue();
                }
                return finalValue;
            }
        });
    }
}

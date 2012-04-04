package org.fao.geonet.monitor.harvest;

import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.MetricsRegistry;
import jeeves.monitor.MetricsFactory;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;

/**
 * Counts the number of errors raised by a Harvester
 *
 * User: jeichar
 * Date: 4/3/12
 * Time: 5:18 PM
 */
public class AbstractHarvesterErrorCounter implements MetricsFactory<Counter>{
    public Counter create(MetricsRegistry metricsRegistry, ServiceContext context) {
        return metricsRegistry.newCounter(AbstractHarvester.class, "HarvestingErrors");
    }
}

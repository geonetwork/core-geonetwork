package org.fao.geonet.monitor.gauge;

import jeeves.server.resources.Stats;

/**
 * Gauge that gets the maximum number possible connections as reported by the ResourceProvider.
 * If unable to access information or if the number is null (Like in case of JNDI) Integer.MIN_VALUE
 * will be reported.
 *
 * User: jeichar
 * Date: 4/5/12
 * Time: 4:29 PM
 */
public class ResourceManagerMaxConnectionsGauge extends AbstractResourceManagerStatsGauge<Integer> {
    public ResourceManagerMaxConnectionsGauge() {
        super("Max_Connections_By_ResourceProvider");
    }

    @Override
    protected Integer valueImpl(Stats stats) {
        return stats.maxActive;
    }

    @Override
    protected Integer defaultValue() {
        return Integer.MIN_VALUE;
    }
}

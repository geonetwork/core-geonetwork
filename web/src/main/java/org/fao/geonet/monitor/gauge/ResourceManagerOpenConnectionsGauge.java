package org.fao.geonet.monitor.gauge;

import jeeves.server.resources.Stats;

/**
 * Gauge that gets the number of connections that the ResourceProvider reports as open.  If unable to access
 * information or if the number is null (Like in case of JNDI) Integer.MIN_VALUE will be reported
 *
 * User: jeichar
 * Date: 4/5/12
 * Time: 4:29 PM
 */
public class ResourceManagerOpenConnectionsGauge extends AbstractResourceManagerStatsGauge<Integer> {
    public ResourceManagerOpenConnectionsGauge() {
        super("Open_Connections_By_ResourceProvider");
    }

    @Override
    protected Integer valueImpl(Stats stats) {
        return stats.numActive;
    }

    @Override
    protected Integer defaultValue() {
        return Integer.MIN_VALUE;
    }
}

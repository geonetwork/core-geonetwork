package org.fao.geonet.kernel.csw;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Executionservice to deal with asynchronous CSW HarvestResponse. It is shut down in org.fao.geonet.GeoNetwork.stop().
 *
 * @author heikki doeleman
 *
 */
public class CswHarvesterResponseExecutionService {

    private static ScheduledExecutorService executionService = Executors.newScheduledThreadPool(1);

    public static ScheduledExecutorService getExecutionService() {
        return executionService;
    }
}
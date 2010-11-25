package org.fao.geonet.notifier;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MetadataNotifierControl {
    private ServiceContext srvContext;
    private GeonetContext gc;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public MetadataNotifierControl(ServiceContext srvContext, GeonetContext gc) {
		this.srvContext = srvContext;
        this.gc = gc;
	}

    public void runOnce() {
    	System.out.println("MetadataNotifierControl runOnce start");
        
        Log.debug(Geonet.DATA_MANAGER, "getUnregisteredMetadata after dbms");
        final MetadataNotifierTask updateTask = new MetadataNotifierTask(srvContext, gc);
        
        @SuppressWarnings("unused")
		final ScheduledFuture<?> updateTaskHandle = scheduler.schedule(updateTask, 20, TimeUnit.SECONDS) ;
        System.out.println("MetadataNotifierControl runOnce finish");
    }
}

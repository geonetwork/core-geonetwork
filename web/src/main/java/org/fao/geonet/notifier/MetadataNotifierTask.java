package org.fao.geonet.notifier;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;

public class MetadataNotifierTask implements Runnable {
    private ServiceContext srvContext;
    private GeonetContext gc;

    public MetadataNotifierTask(ServiceContext srvContext, GeonetContext gc) {
		this.srvContext = srvContext;
        this.gc = gc;
	}


	public void run() {
		try {
            Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);
			gc.getMetadataNotifier().updateMetadataBatch(dbms, gc);
		}
		catch(Exception x) {
			System.out.println(x.getMessage());
			x.printStackTrace();
		}
	}
}

package org.fao.geonet.kernel.harvest.harvester;

import org.fao.geonet.monitor.harvest.AbstractHarvesterErrorCounter;

import com.yammer.metrics.core.Counter;

import jeeves.monitor.MonitorManager;
import jeeves.server.context.ServiceContext;

public class AbstractHarvestError {
	
	public AbstractHarvestError(ServiceContext context) {
		super();
		// we don't catch anything because this is not critical
		// if the following code is not executed, we just want to
		// avoid to raise an exception.
		try {
			MonitorManager mm = context.getBean(MonitorManager.class);
			Counter harvestError = mm.getCounter(AbstractHarvesterErrorCounter.class);
			harvestError.inc();
		} finally {}
		
	}
}

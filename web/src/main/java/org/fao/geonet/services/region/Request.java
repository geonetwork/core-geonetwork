package org.fao.geonet.services.region;

import java.util.Collection;

public abstract class Request {

    /**
     * 
     * @param labelParam
     * @return
     */
    public abstract Request label(String labelParam);

    public abstract Request categoryId(String categoryIdParam);

    public abstract Request maxRecords(int maxRecordsParam);

    public abstract Collection<Region> execute() throws Exception;

    public Region get() throws Exception {
        Collection<Region> regions = execute();
        if(regions.size() > 1) {
            throw new IllegalStateException("there is more than one region found");
        }
        if(regions.isEmpty()) {
            return null;
        } else {
            return regions.iterator().next();
        }
    }

    public abstract Request id(String regionId);


}

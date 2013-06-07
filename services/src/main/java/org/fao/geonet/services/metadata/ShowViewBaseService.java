package org.fao.geonet.services.metadata;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;

/**
 * @author heikki doeleman
 */
public abstract class ShowViewBaseService implements Service {
    /**
     *
     * @param appPath
     * @param params
     * @throws Exception
     */
    @Override
    public void init(String appPath, ServiceConfig params) throws Exception {
        String skip;

        skip = params.getValue("skipPopularity", "n");
        skipPopularity = skip.equals("y");

        skip = params.getValue("skipInfo", "n");
        skipInfo = skip.equals("y");

        skip = params.getValue("addRefs", "n");
        addRefs = skip.equals("y");
    }

    protected boolean skipPopularity;
    protected boolean skipInfo;
    protected boolean addRefs;

}
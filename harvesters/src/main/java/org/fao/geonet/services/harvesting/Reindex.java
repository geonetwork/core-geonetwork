package org.fao.geonet.services.harvesting;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.kernel.harvest.Common;
import org.fao.geonet.kernel.harvest.HarvestManager;
import org.jdom.Element;

import java.nio.file.Path;

public class Reindex implements Service {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        return Util.exec(params, context, new Util.Job() {
            public Common.OperResult execute(HarvestManager hm, String id) throws Exception {
                return hm.reindexBatch(id);
            }
        });
    }
}

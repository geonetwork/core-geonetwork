//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.jms.message.harvest;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.ClusterException;
import org.fao.geonet.jms.message.MessageHandler;
import org.fao.geonet.kernel.harvest.Common;
import org.fao.geonet.kernel.harvest.HarvestManager;

/**
 * @author heikki doeleman
 */
public class HarvestMessageHandler implements MessageHandler {

    private ServiceContext context;
    
    public HarvestMessageHandler(ServiceContext context) {
        this.context = context;
    }
    
    public void process(String msg) throws ClusterException{
        try {
            System.out.println("HarvestMessageHandler received message '" + msg + "'\nin node " + ClusterConfig.getClientID());
            Log.debug(Geonet.CLUSTER, "HarvestMessageHandler received message '" + msg + "'\nin node " + ClusterConfig.getClientID());

            HarvestMessage message = new HarvestMessage();
            message = message.decode(msg);        
            GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
            HarvestManager harvestManager = gc.getHarvestManager();
            String id = message.getId();
            Common.OperResult result =  harvestManager.invoke(context.getResourceManager(), id);

            Log.debug(Geonet.CLUSTER, "HarvestMessageHandler result: " + result.name());
        }
        catch (Exception x) {
            Log.error(Geonet.CLUSTER, "Error processing HarvestMessageHandler message: " + x.getMessage());
            x.printStackTrace();
            throw new ClusterException(x.getMessage(), x);
        }
    }
}
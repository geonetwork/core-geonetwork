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
package org.fao.geonet.jms.message.reindex;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.ClusterException;
import org.fao.geonet.jms.message.MessageHandler;
import org.fao.geonet.kernel.search.SearchManager;

/**
 * @author jose garcia
 */
public class OptimizeIndexMessageHandler implements MessageHandler {

    private ServiceContext context;

    public OptimizeIndexMessageHandler(ServiceContext context) {
        this.context = context;
    }
    
    public void process(String message) throws ClusterException {
        Log.debug(Geonet.CLUSTER, "OptimizeIndexMessageHandler processing message '" + message + "'");

        OptimizeIndexMessage optimizeIndexMessage = new OptimizeIndexMessage();
        optimizeIndexMessage = optimizeIndexMessage.decode(message);
        // message was sent by this GN instance itself; ignore
        if(optimizeIndexMessage.getSenderClientID().equals(ClusterConfig.getClientID())) {
            Log.debug(Geonet.CLUSTER, "OptimizeIndexMessageHandler ignoring message from self");
        }
        // message was sent by another GN instance
        else {
            try {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                SearchManager searchMan = gc.getSearchmanager();

                searchMan.optimizeIndexWithoutSendingTopic();
            }
            catch(Exception x) {
                Log.error(Geonet.CLUSTER, "Error processing optimize index message: " + x.getMessage());
                x.printStackTrace();
                throw new ClusterException(x.getMessage(), x);
            }
        }

    }
    
}
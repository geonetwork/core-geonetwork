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

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.ClusterException;
import org.fao.geonet.jms.message.MessageHandler;
import org.fao.geonet.kernel.DataManager;

/**
 * @author heikki doeleman
 */
public class ReIndexMessageHandler implements MessageHandler {
    
    private ServiceContext context;
    
    public ReIndexMessageHandler(ServiceContext context) {
        this.context = context;
    }
    
    public void process(String message) throws ClusterException {

        ReIndexMessage reIndexMessage = new ReIndexMessage();
        reIndexMessage = reIndexMessage.decode(message);
        // message was sent by this GN instance itself; ignore
        if(reIndexMessage.getSenderClientID().equals(ClusterConfig.getClientID())) {
            Log.debug(Geonet.CLUSTER, "ReIndexMessageHandler ignoring message from self");
        }
        // message was sent by another GN instance
        else {
            Log.debug(Geonet.CLUSTER, "ReIndexMessageHandler processing message '" + message + "'");
            Dbms dbms = null;

            try {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
                DataManager dataManager = gc.getDataManager();
                dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
                
                if (!reIndexMessage.isDeleteMetadata()) {
                    dataManager.indexMetadataWithoutSendingTopic(dbms, reIndexMessage.getId(),
                            Boolean.parseBoolean(reIndexMessage.getIndexGroup()), reIndexMessage.isWorkspace());

                }
                else {
                    dataManager.deleteMetadataWithoutSendingTopic(context, dbms, reIndexMessage.getId(), reIndexMessage.isWorkspace());
                }
            }
            catch(Exception x) {
                Log.error(Geonet.CLUSTER, "Error processing reindex message: " + x.getMessage());
                x.printStackTrace();
                throw new ClusterException(x.getMessage(), x);
            } finally {
                try {
                    // Close dbms connection
                    if (dbms != null) context.getResourceManager().close(Geonet.Res.MAIN_DB, dbms);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
    
}
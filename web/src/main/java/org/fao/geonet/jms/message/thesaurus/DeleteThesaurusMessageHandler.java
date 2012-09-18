//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.jms.message.thesaurus;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.ClusterException;
import org.fao.geonet.jms.message.MessageHandler;

/**
 * @author jose garcia
 */
public class DeleteThesaurusMessageHandler implements MessageHandler {

    private ServiceContext context;

    public DeleteThesaurusMessageHandler(ServiceContext context) {
        this.context = context;
    }

    public void process(String message) throws ClusterException {
        Log.debug(Geonet.CLUSTER, "DeleteThesaurusMessageHandler processing message '" + message + "'");

        DeleteThesaurusMessage deleteThesaurusMessage = new DeleteThesaurusMessage();
        deleteThesaurusMessage = deleteThesaurusMessage.decode(message);

        // message was sent by this GN instance itself; ignore
        if(deleteThesaurusMessage.getOriginatingClientID().equals(ClusterConfig.getClientID())) {
            Log.debug(Geonet.CLUSTER, "DeleteThesaurusMessageHandler ignoring message from self");
        }
        // message was sent by another GN instance
        else {
            try {
                GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

                gc.getThesaurusManager().removeWithoutSendingTopic(deleteThesaurusMessage.getName());
            }
            catch(Exception x) {
                Log.error(Geonet.CLUSTER, "Error processing delete thesaurus message: " + x.getMessage());
                x.printStackTrace();
                throw new ClusterException(x.getMessage(), x);
            }
        }

    }
    
}
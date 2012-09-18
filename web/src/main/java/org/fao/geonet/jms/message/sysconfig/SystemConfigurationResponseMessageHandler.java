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
package org.fao.geonet.jms.message.sysconfig;

import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.ClusterException;
import org.fao.geonet.jms.message.MessageHandler;
import org.jdom.JDOMException;

import java.io.IOException;

/**
 * @author heikki doeleman
 */
public class SystemConfigurationResponseMessageHandler implements MessageHandler {

    public SystemConfigurationResponseMessageHandler() {
    }
    
    public void process(String message) throws ClusterException {
        Log.debug(Geonet.CLUSTER, "SystemConfigurationResponseMessageHandler processing message '" + message + "'");
        SystemConfigurationResponseMessage responseMessage = new SystemConfigurationResponseMessage();
        responseMessage = responseMessage.decode(message);

        // original request message was not from this GN instance; ignore
        if(!responseMessage.getRecipientClientID().equals(ClusterConfig.getClientID())) {
            Log.debug(Geonet.CLUSTER, "ignoring sysconfig response message not initiated from self");
        }
        else {
            try {
                NodeConfigurations nodeConfigurations = NodeConfigurations.getInstance();
                nodeConfigurations.add(Xml.loadString(responseMessage.getInfo(), false));
            }
            catch (JDOMException x) {
                Log.error(Geonet.CLUSTER, x.getMessage());
                x.printStackTrace();
                throw new ClusterException(x.getMessage(), x);
            }
            catch (IOException x) {
                Log.error(Geonet.CLUSTER, x.getMessage());
                x.printStackTrace();
                throw new ClusterException(x.getMessage(), x);
            }
        }
    }
}
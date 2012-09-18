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

import jeeves.interfaces.Service;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.ClusterConfig;
import org.fao.geonet.jms.ClusterException;
import org.fao.geonet.jms.Producer;
import org.fao.geonet.jms.message.MessageHandler;
import org.fao.geonet.services.config.GetInfo;
import org.jdom.Element;

/**
 * @author heikki doeleman
 */
public class SystemConfigurationMessageHandler implements MessageHandler {

    private ServiceContext context;

    public SystemConfigurationMessageHandler(ServiceContext context) {
        this.context = context;
    }
    
    public void process(String message) throws ClusterException {
        Log.debug(Geonet.CLUSTER, "SystemConfigurationMessageHandler processing message '" + message + "'");
        SystemConfigurationMessage systemConfigurationMessage = new SystemConfigurationMessage();
        systemConfigurationMessage = systemConfigurationMessage.decode(message);

        try {
            Service getInfo = new GetInfo();
            Element param = new Element("request");
            Element clusterExec = new Element("clusterExecution").setText("false");
            param.setContent(clusterExec);
            Element info = getInfo.exec(param, context);
            SystemConfigurationResponseMessage responseMessage = new SystemConfigurationResponseMessage();
            responseMessage.setInfo(Xml.getString(info));
            responseMessage.setSenderClientID(ClusterConfig.getClientID());
            responseMessage.setRecipientClientID(systemConfigurationMessage.getSenderClientID());
            Producer systemConfigurationResponseProducer = ClusterConfig.get(Geonet.ClusterMessageTopic.SYSTEM_CONFIGURATION_RESPONSE);
            systemConfigurationResponseProducer.produce(responseMessage);
        }
        catch(Exception x) {
            Log.error(Geonet.CLUSTER, "Error processing system configuration message: " + x.getMessage());
            x.printStackTrace();
            throw new ClusterException(x.getMessage(), x);
        }
    }
    
}
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
package org.fao.geonet.jms;

import org.fao.geonet.jms.message.Encodable;

import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * JMS message producer.
 *
 * @author heikki doeleman
 */
public class Producer extends JMSActor {
    
    private String destinationName;

    public String getDestinationName() {
        return destinationName;
    }

    /**
     * 
     * @param destinationName
     * @param messagingDomain
     * @throws ClusterException hmm
     */
    public Producer(String destinationName, MessagingDomain messagingDomain) throws ClusterException {
        super();
        try {
            this.destinationName = destinationName;
            switch(messagingDomain) {
                case POINT_TO_POINT:
                    destination = session.createQueue(destinationName);
                    producer = session.createProducer(destination);
                    break;
                case PUBLISH_SUBSCRIBE:
                    destination = session.createTopic(destinationName);
                    producer = session.createProducer(destination);
                    break;
                default:
                    throw new ClusterException("Incorrect messaging domain: " + messagingDomain.name());
            }
        }
        catch (JMSException x) {
            System.err.println("Error initializing ReIndexTopicConsumer: " + x.getMessage());
            x.printStackTrace();
            throw new ClusterException(x.getMessage(), x);
        }
    }

    /**
     * 
     * @param message
     * @throws ClusterException hmm
     */
    public void produce(Encodable message) throws ClusterException {
        try {
            //Log.debug(Geonet.JMS,"producing message from class: " +  message.getClass().getName());
            //Log.debug(Geonet.JMS,"   --> message content: " +  message.encode());
            TextMessage textMessage = session.createTextMessage(message.encode());
            producer.send(textMessage);
        }
        catch (JMSException x) {
            System.err.println(x.getMessage());
            x.printStackTrace();
            throw new ClusterException(x.getMessage(), x);
        }
    }
}
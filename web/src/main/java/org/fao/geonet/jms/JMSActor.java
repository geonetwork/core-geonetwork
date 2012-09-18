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

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Common functionality of Producers and Consumers.
 *
 * @author heikki doeleman
 */
public abstract class JMSActor {
    
    protected MessageConsumer consumer;
    protected MessageProducer producer;
    protected Session session;
    protected Connection connection;
    protected Destination destination;

    /**
     * Sets up JMS connection and session.
     *
     * @throws ClusterException hmm
     */
    public JMSActor() throws ClusterException {
        try {
            if(!ClusterConfig.isEnabled()) {
                return;
            }
            // Getting JMS connection from the server
            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ClusterConfig.getBrokerURL());
            connection = connectionFactory.createConnection();
            connection.setClientID(ClusterConfig.getClientID());
            connection.start();

            // Creating session for sending and receiving messages
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        }
        catch(JMSException x) {
            System.err.println(x.getMessage());
            x.printStackTrace();
            throw new ClusterException(x.getMessage(), x);
        }
    }

    /**
     * Closes consumer, producer, session, and connection.
     *
     * @throws ClusterException hmm
     */
    public void shutdown() throws ClusterException {
        try {
            if(consumer != null) {
                consumer.close();
            }
            if(producer != null) {
                producer.close();
            }
            session.close();
            connection.close();
        }
        catch(JMSException x) {
            System.err.println(x.getMessage());
            x.printStackTrace();
            throw new ClusterException(x.getMessage(), x);
        }
    }
}
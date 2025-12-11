package org.geonetwork.messaging;

import org.apache.activemq.ActiveMQConnectionFactory;

import jakarta.jms.Connection;
import jakarta.jms.Session;
import jakarta.jms.Destination;
import jakarta.jms.DeliveryMode;
import jakarta.jms.ObjectMessage;

import jakarta.jms.MessageProducer;

import java.io.Serializable;

/**
 * Created by francois on 05/11/15.
 */
public class JMSMessager {
    private String jmsUrl;

    public String getJmsUrl() {
        return jmsUrl;
    }

    public void setJmsUrl(String jmsUrl) {
        this.jmsUrl = jmsUrl;
    }

    public void sendMessage(String queue, Serializable event) {
        try {
            // Create a ConnectionFactory
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(this.jmsUrl);

            // Create a Connection
            Connection connection = connectionFactory.createConnection();
            connection.start();
            try {
                // Create a Session
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                try {
                    // Create the destination (Topic or Queue)
                    Destination destination = session.createQueue(queue);

                    // Create a MessageProducer from the Session to the Topic or Queue
                    MessageProducer producer = session.createProducer(destination);
                    producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

                    // Create a messages
                    ObjectMessage message = session.createObjectMessage(event);

                    // Tell the producer to send the message
                    producer.send(message);
                } finally {
                    session.close();
                }
            } finally {
                connection.close();
            }
        } catch (Exception e) {
            // TODO : dedicated logger needed
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }
}

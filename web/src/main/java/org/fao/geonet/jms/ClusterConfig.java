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

package org.fao.geonet.jms;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.jms.message.harvest.HarvestMessageHandler;
import org.fao.geonet.jms.message.harvest.HarvesterMessageHandler;
import org.fao.geonet.jms.message.reindex.OptimizeIndexMessageHandler;
import org.fao.geonet.jms.message.reindex.ReIndexMessageHandler;
import org.fao.geonet.jms.message.reindex.ReloadLuceneConfigMessageHandler;
import org.fao.geonet.jms.message.settings.SettingsMessageHandler;
import org.fao.geonet.jms.message.sysconfig.SystemConfigurationMessageHandler;
import org.fao.geonet.jms.message.sysconfig.SystemConfigurationResponseMessageHandler;
import org.fao.geonet.jms.message.thesaurus.AddThesaurusElemMessageHandler;
import org.fao.geonet.jms.message.thesaurus.AddThesaurusMessageHandler;
import org.fao.geonet.jms.message.thesaurus.DeleteThesaurusElemMessageHandler;
import org.fao.geonet.jms.message.thesaurus.DeleteThesaurusMessageHandler;
import org.fao.geonet.jms.message.thesaurus.UpdateThesaurusElemMessageHandler;
import org.fao.geonet.jms.message.versioning.MetadataVersioningMessageHandler;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author heikki doeleman
 */
public class ClusterConfig {

    private static String clientID;
    /**
     * URL to ActiveMQ broker.
     * 
     * See http://activemq.apache.org/uri-protocols.html for options. Default value here is for ActiveMQ on same machine 
     * as this GeoNetwork node, but not in the same JVM (same as ActiveMQConnection.DEFAULT_BROKER_URL).
     */
    private static String brokerURL = "failover://tcp://localhost:61616";
    private static boolean enabled;

    private static SystemConfigurationResponseMessageHandler systemConfigurationResponseMessageHandler;
    public static SystemConfigurationResponseMessageHandler getSystemConfigurationResponseMessageHandler() {
        return systemConfigurationResponseMessageHandler;
    }

    /**
     * All producers and consumers, to close them all easily.
     */
    private static List<JMSActor> jmsActors = new ArrayList<JMSActor>();

    /**
     * Map from JMS destination to Producer, so code using the producer can retrieve it.
     */
    private static Map<String, Producer> producerMap = new HashMap<String, Producer>();

    /**
     * Closes all Producers and Consumers.
     *
     * @throws ClusterException hmm
     */
    public static void shutdown() throws ClusterException {
        for(JMSActor participant : jmsActors) {
            participant.shutdown();
        }
    }

    public static Producer get(String key) {
        return producerMap.get(key);
    }

    /**
     * Registers Producer with JMSACtors (so they're easily closed all together) and in producer map (so it can be
     * retrieved in code using the producer).
     *
     * @param producer producer
     */
    private static void register(Producer producer) {
        jmsActors.add(producer);
        producerMap.put(producer.getDestinationName(), producer);
    }

    /**
     * Registers Consumer with JMSACtors (so they're easily closed all together).
     *
     * @param consumer consumer
     */
    private static void register(Consumer consumer) {
        jmsActors.add(consumer);
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enabled) {
        ClusterConfig.enabled = enabled;
    }

    public static String getClientID() {
        return clientID;
    }

    public static String getBrokerURL() {
        return brokerURL;
    }

    /**
     *
     * @param context servicecontext everywhere
     * @throws ClusterException hmm
     */
    private static void initJMSActors(ServiceContext context) throws ClusterException {
        try {
            Log.info(Geonet.JMS, "initializing JMS actors");
            shutdown();
            jmsActors.clear();
            producerMap.clear();
            //
            // producers
            //
            register(new Producer(Geonet.ClusterMessageTopic.REINDEX, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.OPTIMIZEINDEX, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.RELOADINDEXCONF, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.SETTINGS, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.ADDTHESAURUS, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.DELETETHESAURUS, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.ADDTHESAURUS_ELEM, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.DELETETHESAURUS_ELEM, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.UPDATETHESAURUS_ELEM, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.MD_VERSIONING, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.HARVESTER, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.SYSTEM_CONFIGURATION, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageTopic.SYSTEM_CONFIGURATION_RESPONSE, MessagingDomain.PUBLISH_SUBSCRIBE));
            register(new Producer(Geonet.ClusterMessageQueue.HARVEST, MessagingDomain.POINT_TO_POINT));

            Log.info(Geonet.JMS, "registered # " + producerMap.size() + " producers");


            //
            // consumers
            //
            register(new Consumer(Geonet.ClusterMessageTopic.REINDEX, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new ReIndexMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.OPTIMIZEINDEX, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new OptimizeIndexMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.RELOADINDEXCONF, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new ReloadLuceneConfigMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.SETTINGS, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new SettingsMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.ADDTHESAURUS, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new AddThesaurusMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.DELETETHESAURUS, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new DeleteThesaurusMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.ADDTHESAURUS_ELEM, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new AddThesaurusElemMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.DELETETHESAURUS_ELEM, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new DeleteThesaurusElemMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.UPDATETHESAURUS_ELEM, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new UpdateThesaurusElemMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.MD_VERSIONING, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new MetadataVersioningMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.HARVESTER, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new HarvesterMessageHandler(context)));
            register(new Consumer(Geonet.ClusterMessageTopic.SYSTEM_CONFIGURATION, MessagingDomain.PUBLISH_SUBSCRIBE,
                    new SystemConfigurationMessageHandler(context)));
            systemConfigurationResponseMessageHandler = new SystemConfigurationResponseMessageHandler();
            register(new Consumer(Geonet.ClusterMessageTopic.SYSTEM_CONFIGURATION_RESPONSE, MessagingDomain.PUBLISH_SUBSCRIBE,
                    systemConfigurationResponseMessageHandler));
            register(new Consumer(Geonet.ClusterMessageQueue.HARVEST, MessagingDomain.POINT_TO_POINT,
                    new HarvestMessageHandler(context)));

            Log.info(Geonet.JMS, "registered # " + jmsActors.size() + " producers and consumers");
        }
        catch(Throwable x) {
            System.err.println(x.getMessage());
            x.printStackTrace();
            throw new ClusterException(x.getMessage());
        }

    }

    /**
     *
     * @param serviceConfig well..
     * @param serviceContext everywhere
     * @throws ClusterConfigurationException hmm
     * @throws ClusterException hmm
     */
    public static synchronized void initialize(ServiceConfig serviceConfig, SettingManager settingManager, ServiceContext serviceContext)
            throws ClusterConfigurationException, ClusterException {

        try {
            Log.info(Geonet.JMS,"initializing ClusterConfig");

            // cluster configuration
            String clusterConfigXmlFile = serviceConfig.getMandatoryValue(Geonet.Config.CLUSTER_CONFIG) +
                    File.separator + "config-cluster.xml";
            Log.info(Geonet.JMS,"loading cluster configuration from " + clusterConfigXmlFile);
            File config = new File(clusterConfigXmlFile);
            Element cConfig = Xml.loadStream(new FileInputStream(config));
            Element jms = cConfig.getChild("jms");
            boolean enabled = settingManager.getValueAsBool("system/clustering/enable", false);
            if(enabled) {
                Log.info(Geonet.JMS, "clustering enabled");
                ClusterConfig.enabled = true;
                clientID = jms.getChildText("clientID");

                // If node-id is not configured, create it
                if (StringUtils.isEmpty(clientID) || clientID.toUpperCase().equals("CHANGEME")) {
                    clientID = UUID.randomUUID().toString();
                    
                    jms.getChild("clientID").setText(clientID);

                    Xml.writeResponse(new Document((Element)cConfig.detach()), new BufferedOutputStream(
                            new FileOutputStream(new File(clusterConfigXmlFile))));
                }

                Log.info(Geonet.JMS, "clientID is: " + clientID);
                ClusterConfig.brokerURL = settingManager.getValue("system/clustering/jmsurl");
                Log.info(Geonet.JMS, "JMS broker URL is: " + brokerURL);

                initJMSActors(serviceContext);

                verifyClusterConfig();
            }
            else {
                ClusterConfig.enabled = false;
                Log.info(Geonet.JMS, "clustering disabled");
            }
        }
        catch (JDOMException x) {
            Log.error(Geonet.JMS, x.getMessage());
            x.printStackTrace();
            throw new ClusterConfigurationException(x.getMessage(), x);
        }
        catch (FileNotFoundException x) {
            Log.error(Geonet.JMS, x.getMessage());
            x.printStackTrace();
            throw new ClusterConfigurationException(x.getMessage(), x);
        }
        catch (IOException x) {
            Log.error(Geonet.JMS, x.getMessage());
            x.printStackTrace();
            throw new ClusterConfigurationException(x.getMessage(), x);
        }
    }

    /**
     * Checks if all the producers can be retrieved from the ClusterConfig.
     *
     * @throws ClusterConfigurationException hmm
     */
    public static void verifyClusterConfig() throws ClusterConfigurationException {
        Producer producer = ClusterConfig.get(Geonet.ClusterMessageTopic.ADDTHESAURUS);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.ADDTHESAURUS);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.ADDTHESAURUS_ELEM);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.ADDTHESAURUS_ELEM);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.DELETETHESAURUS);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.DELETETHESAURUS);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.DELETETHESAURUS_ELEM);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.DELETETHESAURUS_ELEM);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.HARVESTER);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.HARVESTER);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.MD_VERSIONING);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.MD_VERSIONING);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.OPTIMIZEINDEX);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.OPTIMIZEINDEX);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.REINDEX);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.REINDEX);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.RELOADINDEXCONF);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.RELOADINDEXCONF);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.SETTINGS);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.SETTINGS);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.SYSTEM_CONFIGURATION);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.SYSTEM_CONFIGURATION);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.SYSTEM_CONFIGURATION_RESPONSE);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.SYSTEM_CONFIGURATION_RESPONSE);
        }
        producer = ClusterConfig.get(Geonet.ClusterMessageTopic.UPDATETHESAURUS_ELEM);
        if(producer == null) {
            throw new ClusterConfigurationException("ClusterConfig verification failed: could not retrieve " + Geonet.ClusterMessageTopic.UPDATETHESAURUS_ELEM);
        }
        System.out.println("ClusterConfig verification successful");
    }
}
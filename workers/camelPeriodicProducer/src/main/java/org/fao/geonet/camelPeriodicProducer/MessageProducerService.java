/*
 * Copyright (C) 2001-2015 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.camelPeriodicProducer;

import org.apache.camel.CamelContext;
import org.fao.geonet.IndexedMetadataFetcher;
import org.fao.geonet.domain.MessageProducerEntity;
import org.fao.geonet.events.server.ServerStartup;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterExchangeState;
import org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterRouteBuilder;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.repository.MessageProducerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import static org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterRouteBuilder.MESSAGE_HARVEST_WFS_FEATURES;

@Component
public class MessageProducerService implements ApplicationListener<ServerStartup> {
    private static final String DEFAULT_CONSUMER_URI = "activemq://queue:" + MESSAGE_HARVEST_WFS_FEATURES + "?concurrentConsumers=5";
    private static Logger LOGGER = LoggerFactory.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);
    private static boolean isConfigured = false;
    protected String consumerUri = DEFAULT_CONSUMER_URI;
    @Autowired
    protected MessageProducerFactory messageProducerFactory;
    @Autowired
    protected MessageProducerRepository msgProducerRepository;
    @Autowired
    private EsSearchManager searchManager;
    @Autowired
    private CamelContext camelContext;
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ServerStartup serverStartup) {
        configure(); // wait for geonetworkWorkingDir
    }

    public synchronized void configure() {
        if (!isConfigured) {
            msgProducerRepository
                .findAll()
                .stream()
                .forEach(messageProducerEntity -> {
                    try {
                        messageProducerFactory.registerAndStart(buildWfsHarvesterParameterMessageProducer(messageProducerEntity));
                    } catch (Exception e) {
                        LOGGER.error("failed to initialise persisted quartz wfs harvester command messages producer, id: ({}).", messageProducerEntity.getId());
                    }
                });
            isConfigured = true;
        }
    }

    public void changeMessageAndReschedule(MessageProducerEntity messageProducerEntity) throws Exception {
        messageProducerFactory.changeMessageAndReschedule(buildWfsHarvesterParameterMessageProducer(messageProducerEntity));
    }

    public void registerAndStart(MessageProducerEntity messageProducerEntity) throws Exception {
        messageProducerFactory.registerAndStart(buildWfsHarvesterParameterMessageProducer(messageProducerEntity));
    }

    public void destroy(long id) throws Exception {
        messageProducerFactory.destroy(id);
    }

    private MessageProducer<WFSHarvesterExchangeState> buildWfsHarvesterParameterMessageProducer(MessageProducerEntity
                                                                                                     messageProducerEntity) {
        String metadataUuid = messageProducerEntity.getWfsHarvesterParam().getMetadataUuid();
        String typeName = messageProducerEntity.getWfsHarvesterParam().getTypeName();

        WFSHarvesterParameter wfsHarvesterParam = new WFSHarvesterParameter(
            messageProducerEntity.getWfsHarvesterParam().getUrl(),
            typeName,
            metadataUuid);

        try {
            IndexedMetadataFetcher indexedMetadataFetcher = new IndexedMetadataFetcher(searchManager);
            indexedMetadataFetcher.getApplicationProfileFromLuceneIndex(metadataUuid, typeName);
            wfsHarvesterParam.setTreeFields(indexedMetadataFetcher.getTreeField()); //optional
            wfsHarvesterParam.setTokenizedFields(indexedMetadataFetcher.getTokenizedField()); //optional
        } catch (Exception e) {
            LOGGER.info("could not fetch tree field and tokenized field for metadata uuid {} and typename {}", metadataUuid, typeName);
        }

        return (MessageProducer<WFSHarvesterExchangeState>) new MessageProducer()
            .setMessage(new WFSHarvesterExchangeState(wfsHarvesterParam))
            .setCronExpession(messageProducerEntity.getCronExpression())
            .setTarget(consumerUri)
            .setId(messageProducerEntity.getId());
    }
}

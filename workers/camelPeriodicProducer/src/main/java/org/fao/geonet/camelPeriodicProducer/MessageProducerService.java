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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.records.MetadataSavedQueryApi;
import org.fao.geonet.domain.MessageProducerEntity;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.events.server.ServerStartup;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterExchangeState;
import org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterRouteBuilder;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.repository.MessageProducerRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    MetadataRepository metadataRepository;
    @Autowired
    MetadataSavedQueryApi savedQueryApi;

    public MessageProducerService() {
    }

    public MessageProducerService(MetadataRepository metadataRepository,
                                  MetadataSavedQueryApi savedQueryApi) {
        this.metadataRepository = metadataRepository;
        this.savedQueryApi = savedQueryApi;
    }

    @Override
    public void onApplicationEvent(ServerStartup serverStartup) {
        configure(); // wait for geonetworkWorkingDir
    }

    public synchronized void configure() {
        if (!isConfigured) {
            msgProducerRepository
                .findAll()
                .stream()
                .filter(messageProducerEntity -> {
                    return StringUtils.isNotEmpty(messageProducerEntity.getCronExpression());
                })
                .forEach(messageProducerEntity -> {
                    try {
                        messageProducerFactory.registerAndStart(buildWfsHarvesterParameterMessageProducer(messageProducerEntity));
                    } catch (Exception e) {
                        LOGGER.error("Failed to initialise persisted quartz wfs harvester command messages producer, id: ({}).", messageProducerEntity.getId());
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
            Map<String, Object> applicationProfile = getApplicationProfile(metadataUuid, typeName);
            if(applicationProfile != null) {
                wfsHarvesterParam.setTreeFields(getTreeField(applicationProfile)); //optional
                wfsHarvesterParam.setTokenizedFields(getTokenizedField(applicationProfile)); //optional
            }
        } catch (Exception e) {
            LOGGER.info("Could not fetch tree field and tokenized field for metadata uuid {} and typename {}", metadataUuid, typeName);
        }

        return (MessageProducer<WFSHarvesterExchangeState>) new MessageProducer()
            .setMessage(new WFSHarvesterExchangeState(wfsHarvesterParam))
            .setCronExpession(messageProducerEntity.getCronExpression())
            .setTarget(consumerUri)
            .setId(messageProducerEntity.getId());
    }


    public List<String> getTreeField(Map<String, Object> map) {
        return (List<String>) map.get("treeFields");
    }

    public Map<String, String> getTokenizedField(Map<String, Object> map) {
        return (Map<String, String>) map.get("tokenizedFields");
    }

    private Map<String, Object> getApplicationProfile(String metadataUuid, String typeName) {
        try {
            Metadata metadata = metadataRepository.findOneByUuid(metadataUuid);
            if(metadata != null) {
                Map<String, String> params = new HashMap<>();
                params.put("protocol", "WFS");
                params.put("name", typeName);
                Map<String, String> wfsConfig = savedQueryApi.query(metadata,
                    "wfs-indexing-config", params
                );
                if (wfsConfig.size() > 0 && StringUtils.isNotEmpty(wfsConfig.get("0"))) {
                    ObjectReader reader = new ObjectMapper().readerFor(Map.class);
                    return reader.readValue(wfsConfig.get("0"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

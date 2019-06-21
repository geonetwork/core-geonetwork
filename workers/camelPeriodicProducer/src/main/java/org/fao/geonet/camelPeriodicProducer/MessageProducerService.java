package org.fao.geonet.camelPeriodicProducer;

import org.apache.camel.CamelContext;
import org.apache.camel.management.event.CamelContextStartedEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.fao.geonet.domain.MessageProducerEntity;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterExchangeState;
import org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterRouteBuilder;
import org.fao.geonet.repository.MessageProducerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.EventObject;

import static org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterRouteBuilder.MESSAGE_HARVEST_WFS_FEATURES;

@Component
public class MessageProducerService {
    private static Logger LOGGER = LoggerFactory.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);
    private static final String DEFAULT_CONSUMER_URI = "activemq://queue:" + MESSAGE_HARVEST_WFS_FEATURES + "?concurrentConsumers=5";

    protected String consumerUri = DEFAULT_CONSUMER_URI;

    @Autowired
    protected MessageProducerFactory messageProducerFactory;

    @Autowired
    protected MessageProducerRepository msgProducerRepository;

    @Autowired
    private CamelContext camelContext;

    @PostConstruct
    public void init() {
        camelContext.getManagementStrategy().addEventNotifier(new EventNotifierSupport() {
            @Override
            public void notify(EventObject eventObject) throws Exception {
                configure();
            }

            @Override
            public boolean isEnabled(EventObject eventObject) {
                return eventObject instanceof CamelContextStartedEvent;
            }
        });
    }

    public void configure() {
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
        WFSHarvesterParameter wfsHarvesterParam = new WFSHarvesterParameter(
                messageProducerEntity.getWfsHarvesterParam().getUrl(),
                messageProducerEntity.getWfsHarvesterParam().getTypeName(),
                messageProducerEntity.getWfsHarvesterParam().getMetadataUuid());
        return (MessageProducer<WFSHarvesterExchangeState>) new MessageProducer()
                .setMessage(new WFSHarvesterExchangeState(wfsHarvesterParam))
                .setCronExpession(messageProducerEntity.getCronExpression())
                .setTarget(consumerUri)
                .setId(messageProducerEntity.getId());
    }

}

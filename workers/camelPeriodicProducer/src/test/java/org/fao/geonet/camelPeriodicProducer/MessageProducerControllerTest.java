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

import org.apache.camel.Exchange;
import org.apache.camel.component.quartz2.QuartzComponent;
import org.fao.geonet.api.records.MetadataSavedQueryApi;
import org.fao.geonet.domain.MessageProducerEntity;
import org.fao.geonet.domain.WfsHarvesterParamEntity;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.fao.geonet.repository.MessageProducerRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "/camel-test-config.xml",
    "/domain-repository-test-context.xml",
    "/config-spring-geonetwork.xml"
})
public class MessageProducerControllerTest {
    private static final String EVERY_SECOND = "* * * ? * * *";

    @Autowired
    private MessageProducerRepository messageProducerRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TestCamelNetwork testCamelNetwork;

    @Autowired
    MetadataRepository metadataRepository;

    @Autowired
    MetadataSavedQueryApi savedQueryApi;

    @Autowired
    MessageProducerFactory messageProducerFactory;

    @Before
    public void init() throws Exception {
        testCamelNetwork.getContext().start();
        messageProducerFactory.routeBuilder = testCamelNetwork;
    }

    @After
    public void destroy() {
        messageProducerRepository.deleteAll();
    }

    @Test
    public void createAndDelete() throws Exception {
        MessageProducerController toTest = createToTest();
        MessageProducerEntity messageProducerEntity = createMessageProducerEntity();

        toTest.create(messageProducerEntity);

        List<WFSHarvesterParameter> received = testCamelNetwork.getWfsHarvesterParamConsumer().waitFiveMsg();
        assertEquals("uuid", received.get(0).getMetadataUuid());
        assertEquals("url", received.get(0).getUrl());
        assertEquals("typeName", received.get(0).getTypeName());

        List<MessageProducerEntity> persisted = toTest.msgProducerRepository.findAll();
        assertEquals(1, persisted.size());
        assertEquals(testCamelNetwork.getWfsHarvesterParamConsumer().getFromURI(), "quartz2://null-" + persisted.get(0).getId());

        toTest.delete(persisted.get(0).getId());
        testCamelNetwork.getWfsHarvesterParamConsumer().reset();
        Thread.sleep(2000);
        assertEquals(0, testCamelNetwork.getWfsHarvesterParamConsumer().receivedContent.size());
    }


    @Test
    public void updateMessage() throws Exception {
        MessageProducerController toTest = createToTest();
        MessageProducerEntity messageProducerEntity = createMessageProducerEntity();
        toTest.create(messageProducerEntity);
        WfsHarvesterParamEntity wfsHarvesterParamEntity = messageProducerEntity.getWfsHarvesterParam();
        wfsHarvesterParamEntity.setMetadataUuid("uuid-mod");
        wfsHarvesterParamEntity.setTypeName("typeName-mod");
        wfsHarvesterParamEntity.setUrl("url-mod");

        toTest.update(toTest.msgProducerRepository.findAll().get(0).getId(), messageProducerEntity, false);
        testCamelNetwork.getWfsHarvesterParamConsumer().reset();

        List<WFSHarvesterParameter> received = testCamelNetwork.getWfsHarvesterParamConsumer().waitFiveMsg();
        assertEquals("uuid-mod", received.get(0).getMetadataUuid());
        assertEquals("url-mod", received.get(0).getUrl());
        assertEquals("typeName-mod", received.get(0).getTypeName());
        MessageProducerEntity persisted = toTest.msgProducerRepository.findAll().get(0);
        WfsHarvesterParamEntity persistedWfsHarvesterParam = persisted.getWfsHarvesterParam();
        assertEquals("uuid-mod", persistedWfsHarvesterParam.getMetadataUuid());
        assertEquals("url-mod", persistedWfsHarvesterParam.getUrl());
        assertEquals("typeName-mod", persistedWfsHarvesterParam.getTypeName());
        assertEquals(1, toTest.msgProducerRepository.findAll().size());
        assertEquals(testCamelNetwork.getWfsHarvesterParamConsumer().getFromURI(), "quartz2://null-" + persisted.getId());

        toTest.delete(persisted.getId());
    }

    @Test
    public void rollbackWhenCreateTroubleAtCamelSide() {
        MessageProducerController toTest = createToTest();
        MessageProducerEntity messageProducerEntity = createMessageProducerEntity();
        messageProducerEntity.setCronExpression("i am not a cron expression");

        ResponseEntity<?> response = toTest.create(messageProducerEntity);
        String message = ((MessageProducerController.ErrorResponse) response.getBody()).getMessage();

        assertEquals("CronExpression 'i am not a cron expression' is invalid.", message);
        assertEquals(0, toTest.msgProducerRepository.findAll().size());
    }

    @Test
    public void rollbackWhenUpdateTroubleAtCamelSide() throws Exception {
        MessageProducerController toTest = createToTest();
        MessageProducerEntity messageProducerEntity = createMessageProducerEntity();
        messageProducerEntity = ((ResponseEntity<MessageProducerEntity>) toTest.create(messageProducerEntity)).getBody();
        messageProducerEntity.setCronExpression("i am not a cron expression");

        ResponseEntity<?> response = toTest.update(messageProducerEntity.getId(), messageProducerEntity, false);
        String message = ((MessageProducerController.ErrorResponse) response.getBody()).getMessage();

        assertEquals("CronExpression 'i am not a cron expression' is invalid.", message);
        assertEquals(EVERY_SECOND, toTest.msgProducerRepository.findById(messageProducerEntity.getId()).get().getCronExpression());
        toTest.delete(messageProducerEntity.getId());
    }

    @Test
    public void uniqueKeyErrorOnUrlAndTypenameWhenUpdate() throws Exception {
        MessageProducerController toTest = createToTest();
        MessageProducerEntity messageProducerEntity = createMessageProducerEntity();
        messageProducerEntity = ((ResponseEntity<MessageProducerEntity>) toTest.create(messageProducerEntity)).getBody();
        MessageProducerEntity messageProducerEntity2 = createMessageProducerEntity();
        messageProducerEntity2.getWfsHarvesterParam().setUrl("i am different");
        messageProducerEntity2 = ((ResponseEntity<MessageProducerEntity>) toTest.create(messageProducerEntity2)).getBody();
        messageProducerEntity2.getWfsHarvesterParam().setUrl("url");

        ResponseEntity<?> response = toTest.update(messageProducerEntity2.getId(), messageProducerEntity2, false);
        String message = ((MessageProducerController.ErrorResponse) response.getBody()).getMessage();

        assertTrue(message.contains("23505")); // H2 error code for duplicate unique or primary
        toTest.delete(messageProducerEntity.getId());
        toTest.delete(messageProducerEntity2.getId());
    }

    private MessageProducerController createToTest() {
        MessageProducerController toTest = new MessageProducerController(messageProducerRepository);
        toTest.entityManager = entityManager;
        toTest.messageProducerService = new MessageProducerService(metadataRepository, savedQueryApi);
        toTest.messageProducerService.messageProducerFactory = messageProducerFactory;
        toTest.messageProducerService.consumerUri = testCamelNetwork.getWfsHarvesterParamConsumer().getUri();
        return toTest;
    }

    private MessageProducerEntity createMessageProducerEntity() {
        MessageProducerEntity messageProducerEntity = new MessageProducerEntity();
        messageProducerEntity.setCronExpression(EVERY_SECOND);
        WfsHarvesterParamEntity wfsHarvesterParamEntity = new WfsHarvesterParamEntity();
        wfsHarvesterParamEntity.setMetadataUuid("uuid");
        wfsHarvesterParamEntity.setTypeName("typeName");
        wfsHarvesterParamEntity.setStrategy("strategy");
        wfsHarvesterParamEntity.setUrl("url");
        messageProducerEntity.setWfsHarvesterParam(wfsHarvesterParamEntity);
        return messageProducerEntity;
    }

    static public class MessageConsumer {

        private Integer count = 0;
        private CompletableFuture<List<WFSHarvesterParameter>> future = new CompletableFuture();
        private String uri;

        private List<WFSHarvesterParameter> receivedContent = new ArrayList();

        private String fromURI;

        public MessageConsumer(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }

        public String getFromURI() {
            return fromURI;
        }

        public void consume(Exchange exchange) {
            WFSHarvesterParameter configuration = (WFSHarvesterParameter) exchange.getProperty("configuration");
            receivedContent.add(configuration);
            fromURI = exchange.getFromEndpoint().getEndpointUri();
            count++;
            if (count > 4) {
                future.complete(receivedContent);
            }
        }

        public List<WFSHarvesterParameter> waitFiveMsg() throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(25, TimeUnit.SECONDS);
        }

        public void reset() {
            count = 0;
            receivedContent = new ArrayList();
            future = new CompletableFuture();
        }
    }
}

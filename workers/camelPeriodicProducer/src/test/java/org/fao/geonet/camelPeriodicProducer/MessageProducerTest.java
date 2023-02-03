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
import org.apache.camel.component.quartz2.QuartzEndpoint;
import org.fao.geonet.kernel.setting.SettingManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.CronTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "/camel-test-config.xml",
    "/domain-repository-test-context.xml",
    "/config-spring-geonetwork.xml"
})
public class MessageProducerTest extends AbstractJUnit4SpringContextTests {

    private static final String EVERY_SIX_SECOND = "/6 * * ? * * *";
    private static final String EVERY_THREE_SECOND = "/3 * * ? * * *";
    private static final String EVERY_SECOND = "* * * ? * * *";
    private static final String NEVER = "59 59 23 31 12 ? 2099";

    @Autowired
    private TestCamelNetwork testCamelNetwork;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    MessageProducerFactory toTest;

    @Test
    public void registerAndStart() throws Exception {
        testCamelNetwork.getContext().start();
        toTest.routeBuilder = testCamelNetwork;

        TestMessage testMessage = new TestMessage("testMsg1");
        MessageProducer<TestMessage> messageProducer1 = new MessageProducer<>();
        messageProducer1.setId(1L);
        messageProducer1.setTarget(testCamelNetwork.getMessageConsumer().getUri());
        messageProducer1.setMessage(testMessage);
        messageProducer1.setCronExpession(EVERY_THREE_SECOND);
        toTest.registerAndStart(messageProducer1);

        TestMessage testMessage2 = new TestMessage("testMsg2");
        MessageProducer<TestMessage> messageProducer2 = new MessageProducer<>();
        messageProducer2.setId(2L);
        messageProducer2.setTarget(testCamelNetwork.getMessageConsumer().getUri());
        messageProducer2.setMessage(testMessage2);
        messageProducer2.setCronExpession(EVERY_SIX_SECOND);
        toTest.registerAndStart(messageProducer2);

        List<String> received = testCamelNetwork.getMessageConsumer().waitFive();
        Map<String, Long> result = received.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(2, result.size());
        assertTrue(result.get("testMsg1") - result.get("testMsg2") > 0);

        messageProducer2.setCronExpession(EVERY_SECOND);
        toTest.reschedule(messageProducer2);
        testCamelNetwork.getMessageConsumer().reset();
        received = testCamelNetwork.getMessageConsumer().waitFive();

        result = received.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(2, result.size());
        assertTrue(result.get("testMsg2") - result.get("testMsg1") > 0);

        messageProducer1.setCronExpession(EVERY_SECOND);
        messageProducer1.setMessage(new TestMessage("testMsg3"));
        toTest.changeMessageAndReschedule(messageProducer1);
        toTest.reschedule(messageProducer1);

        testCamelNetwork.getMessageConsumer().reset();
        received = testCamelNetwork.getMessageConsumer().waitFive();
        result = received.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        assertEquals(2, result.size());
        assertTrue(result.containsKey("testMsg3"));

        toTest.destroy(1L);
        toTest.destroy(2L);

        testCamelNetwork.getMessageConsumer().reset();
        Thread.sleep(2000);
        assertEquals(0, testCamelNetwork.getMessageConsumer().receivedContent.size());
    }

    @Test
    public void registerAndStartWithoutCronExpression() throws Exception {
        testCamelNetwork.getContext().start();
        toTest.routeBuilder = testCamelNetwork;

        TestMessage testMessage = new TestMessage("testMsg1");
        MessageProducer<TestMessage> messageProducer = new MessageProducer<>();
        messageProducer.setId(3L);
        messageProducer.setTarget(testCamelNetwork.getMessageConsumer().getUri());
        messageProducer.setMessage(testMessage);
        messageProducer.setCronExpession(null);
        toTest.registerAndStart(messageProducer);

        QuartzEndpoint endpoint = (QuartzEndpoint) toTest.routeBuilder.getContext().getEndpoints().stream()
            .filter(x -> x.getEndpointKey().compareTo(
                "quartz2://" + settingManager.getSiteId() + "-" + messageProducer.getId()) == 0).findFirst().get();

        CronTrigger trigger = (CronTrigger) toTest.quartzComponent.getScheduler().getTrigger(endpoint.getTriggerKey());
        assertEquals(NEVER, trigger.getCronExpression());

        messageProducer.setCronExpession(EVERY_SECOND);
        toTest.changeMessageAndReschedule(messageProducer);

        trigger = (CronTrigger) toTest.quartzComponent.getScheduler().getTrigger(endpoint.getTriggerKey());
        assertEquals(EVERY_SECOND, trigger.getCronExpression());

        toTest.destroy(3L);
    }


    private class TestMessage implements Serializable {

        private String content;

        public TestMessage(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    static public class MessageConsumer {

        private Integer count = 0;
        private CompletableFuture<List<String>> future = new CompletableFuture();
        private String uri;

        private List<String> receivedContent = new ArrayList();

        public MessageConsumer(String uri) {
            this.uri = uri;
        }

        public String getUri() {
            return uri;
        }

        public void consume(Exchange exchange) {
            TestMessage msg = (TestMessage) exchange.getProperty("configuration");
            receivedContent.add(msg.getContent());
            count++;
            if (count > 4) {
                future.complete(receivedContent);
            }
        }

        public List<String> waitFive() throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(25, TimeUnit.SECONDS);
        }

        public void reset() {
            count = 0;
            receivedContent = new ArrayList();
            future = new CompletableFuture();
        }
    }
}

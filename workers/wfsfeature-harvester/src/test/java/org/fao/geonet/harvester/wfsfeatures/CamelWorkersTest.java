package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CamelWorkersTest extends CamelTestSupport {

    private String START_ENDPOINT_URI = "activemq:queue:test.a";

    @Test
    public void camelServiceReactWhenStarted() throws Exception {
        CamelWorkers toTest = new CamelWorkers();
        toTest.start();

        CompletableFuture<String> future = new CompletableFuture<>();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                template.sendBody(START_ENDPOINT_URI,"dummy");
                future.complete("camel is responding.");
            }
        });
        assertEquals("camel is responding.", future.get(2, TimeUnit.SECONDS));

        toTest.stop();
    }

    @Test
    public void nonStartedWorkersCanBeStopped() throws Exception {
        CamelWorkers toTest = new CamelWorkers();
        toTest.stop();
    }

    @Test
    public void camelWorkersAreReferencedInSpringContext() {
        ApplicationContext toTest = new ClassPathXmlApplicationContext("config-spring-geonetwork-parent.xml");
        assertNotNull(toTest.getBean(CamelWorkers.class));
    }
}
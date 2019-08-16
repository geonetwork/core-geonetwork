package org.fao.geonet.camelPeriodicProducer;

import org.apache.camel.builder.RouteBuilder;

public class TestCamelNetwork extends RouteBuilder {

    private MessageProducerTest.MessageConsumer messageConsumer;
    private MessageProducerControllerTest.MessageConsumer wfsHarvesterParamConsumer;


    public TestCamelNetwork()
    {
        messageConsumer = new MessageProducerTest.MessageConsumer("direct:consumer");
        wfsHarvesterParamConsumer = new MessageProducerControllerTest.MessageConsumer("direct:wfsHravesterParamConsumer");
    }

    public MessageProducerTest.MessageConsumer getMessageConsumer() {
        return messageConsumer;
    }

    public MessageProducerControllerTest.MessageConsumer getWfsHarvesterParamConsumer() {
        return wfsHarvesterParamConsumer;
    }

    @Override
    public void configure() throws Exception {

        from(messageConsumer.getUri())
                .id("test_route_id")
                .setProperty("configuration", simple("${body}"))
                .bean(messageConsumer, "consume");

        from(wfsHarvesterParamConsumer.getUri())
                .id("test_route_id_for_wfs_harvester_param")
                .setProperty("configuration", simple("${body.parameters}"))
                .bean(wfsHarvesterParamConsumer, "consume");

    }
}

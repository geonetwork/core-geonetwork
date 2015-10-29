package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.w3c.dom.Document;

/**
 * Created by francois on 28/10/15.
 */
public class HarvesterRouteBuilder extends RouteBuilder {
    private static final String LOGGER_NAME = "harvester.wfsfeature";

    @Override
    public void configure() throws Exception {

        from("spring-event:default")
                .filter(body().startsWith("http"))
                .log(LoggingLevel.DEBUG, LOGGER_NAME, "${body.url}")
                .setHeader("Exchange.HTTP_URI", simple("${body.url}"))
                .setBody(simple(""))
                .to("http4://temp")
                .convertBodyTo(Document.class)
                .split()
                    .xpath("//gml:featureMember/*|//wfs2:member/*", WfsNamespaces.wfsNamespaces.getNamespaces())
                    .parallelProcessing()
                    .bean(FeatureIndexer.class, "featureToIndexDocument");
    }
}


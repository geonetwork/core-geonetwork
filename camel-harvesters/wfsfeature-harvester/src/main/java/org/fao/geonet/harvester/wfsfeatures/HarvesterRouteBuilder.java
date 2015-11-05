package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.Exchange;
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
        final String url = "http://geoservices.brgm.fr/risques?SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&TYPENAME=BASIAS_LOCALISE&maxFeatures=1";
//        final String url = "http4://geoservices.brgm.fr/risques?SERVICE=WFS&VERSION=1.0.0&REQUEST=GetFeature&TYPENAME=BASIAS_LOCALISE&maxFeatures=10000";
        //http://visi-sextant.ifremer.fr/cgi-bin/sextant/wfs/bgmb?REQUEST=GetFeature&SERVICE=WFS&VERSION=1.1.0&TypeName=SISMER_prelevements&maxFeatures=100 [

        from("timer://start?repeatCount=1").autoStartup(false)
                .log(LoggingLevel.DEBUG, LOGGER_NAME, "Harvesting ${body.url} one time.")
                .setHeader(Exchange.HTTP_URI, simple(url))
                .setProperty("mduuid", simple(""))
                .setProperty("linkage", simple("test"))
                .to("activemq:queue:harvest-wfs-features");

        /**
         * This route get `uuid` `wfsUrl` and `featureType` properties from JSM message.
         * It creates a bean FeatureTypeBean to store these properties, the attribute
         * types and the WFSDatastore.
         * This bean will be pass to next Route.
         */
        from("activemq:queue:harvest-wfs-features")
                .log(LoggingLevel.DEBUG, LOGGER_NAME, "### ActiveMQ message received.")
                .setProperty("mduuid", simple("${body.uuid}"))

                .setProperty("wfsUrl", simple("${body.wfsUrl}"))
                .setProperty("featureType", simple("${body.featureType}"))

/*
                .setProperty("featureType", simple("menhirs"))
                .setProperty("wfsUrl", simple("http://sdi.georchestra.org/geoserver/geor/wfs"))
*/

                .bean(FeatureTypeBean.class, "initialize")
                .to("direct:index-wfs");

/*
        from("direct:describe")
                .log(LoggingLevel.DEBUG, LOGGER_NAME, "### Direct describeFeatureType.")
                .setProperty("featureType", simple("menhirs"))
                .setProperty("linkage", simple("http://sdi.georchestra.org/geoserver/geor/wfs"))
                .bean(FeatureTypeBean.class, "initialize");
*/

/*
        .log(LoggingLevel.DEBUG, LOGGER_NAME, "${body}")
                .setHeader("Exchange.HTTP_URI", simple("${body.url}"))
                .setProperty("mduuid", simple("${body.uuid}"))
                .setProperty("linkage", simple("${body.linkage}"))
                .setBody(simple(""))
                .to("direct:index-wfs");
*/



//        from("spring-event:default")
//                .filter(body().startsWith("http"))
//                .log(LoggingLevel.DEBUG, LOGGER_NAME, "${body.url}")
//                .setHeader("Exchange.HTTP_URI", simple("${body.url}"))
//                .setProperty("mduuid", simple("${body.uuid}"))
//                .setProperty("linkage", simple("${body.linkage}"))
//                .setBody(simple(""))
//                .to("direct:index-wfs");

        // TODO drop feature before adding new one ?
        from("direct:index-wfs")
            .bean(FeatureIndexer.class, "featureToIndexDocument");
    }
}


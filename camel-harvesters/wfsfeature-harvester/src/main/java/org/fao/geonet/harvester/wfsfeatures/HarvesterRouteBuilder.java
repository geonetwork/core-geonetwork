package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by francois on 28/10/15.
 */
public class HarvesterRouteBuilder extends RouteBuilder {
    private static final String LOGGER_NAME = "harvester.wfsfeature";

    @Override
    public void configure() throws Exception {
        /**
         * For testing, turn autoStartup to true.
         */
        from("timer://start?repeatCount=1").autoStartup(false)
                .log(LoggingLevel.DEBUG, LOGGER_NAME, "Harvesting ${body.url} one time.")
                .setProperty("wfsUrl", simple("http://geoservices.brgm.fr/risques"))
                .setProperty("featureType", simple("BASIAS_LOCALISE"))
//                .setProperty("wfsUrl", simple("http://sdi.georchestra.org/geoserver/geor/wfs"))
//                .setProperty("featureType", simple("menhirs"))
                .bean(FeatureTypeBean.class, "initialize")
                .to("direct:delete-wfs-featuretype-features")
                .to("direct:index-wfs");

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
                .bean(FeatureTypeBean.class, "initialize")
                .to("direct:delete-wfs-featuretype-features")
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


        from("direct:delete-wfs-featuretype-features")
                .beanRef("featureIndexer", "deleteFeatures");


        from("direct:index-wfs")
                .beanRef("featureIndexer", "featureToIndexDocument");
    }
}


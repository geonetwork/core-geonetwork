package org.fao.geonet.harvester.wfsfeatures;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;
import org.w3c.dom.Document;

/**
 * Created by francois on 28/10/15.
 */
public class HarvesterRouteBuilder extends RouteBuilder {
    public static final String LOGGER_NAME = "harvest.wfs.features";
    public static final String MESSAGE_HARVEST_WFS_FEATURES = "harvest-wfs-features";
    public static final String MESSAGE_DELETE_WFS_FEATURES = "delete-wfs-features";

    private static boolean isStartingFromXMLConfigurationFile = false;

    public static boolean isStartingFromXMLConfigurationFile() {
        return isStartingFromXMLConfigurationFile;
    }

    public static void setIsStartingFromXMLConfigurationFile(boolean isStartingFromXMLConfigurationFile) {
        HarvesterRouteBuilder.isStartingFromXMLConfigurationFile = isStartingFromXMLConfigurationFile;
    }


    @Override
    public void configure() throws Exception {
        onException(Exception.class)
                .handled(true)
                .log(LoggingLevel.ERROR, LOGGER_NAME, "Exception occured: ${exception.message}")
                .log(LoggingLevel.ERROR, LOGGER_NAME, "Harvesting task terminated due to previous exception (Exchange ${exchangeId}).");

        /**
         * Start the indexing from the XML configuration file.
         * Used for testing mainly.
         */
        XPathBuilder xPathWfsConfigBuilder = new XPathBuilder("//wfs");
        from("file:src/test/resources/?fileName=wfs.xml&noop=true")
                .id("harvest-wfs-start-from-file")
                .autoStartup(isStartingFromXMLConfigurationFile)
                .log(LoggingLevel.INFO, LOGGER_NAME, "Harvest features from XML configuration file.")
                .convertBodyTo(Document.class)
                .log(LoggingLevel.DEBUG, LOGGER_NAME, "Content is: ${body}")
                .split(xPathWfsConfigBuilder)
                    .parallelProcessing()
                    .executorServiceRef("harvest-wfs-thread-pool")
                    // Will not stop on exception if on of the splitted task fails
                        .setProperty("wfsUrl", xpath("wfs/@url", String.class))
                        .setProperty("featureType", xpath("wfs/@featureType", String.class))
                        .log(LoggingLevel.INFO, LOGGER_NAME, "#${property.CamelSplitIndex}. Harvesting ${property.wfsUrl} - start (Exchange ${exchangeId}).")
                        .bean(FeatureTypeBean.class, "initialize(*, true)")
                        .to("direct:delete-wfs-featuretype-features")
                        .to("direct:index-wfs")
                        .log(LoggingLevel.INFO, LOGGER_NAME, "#${property.CamelSplitIndex}. Harvesting ${property.wfsUrl} - end (Exchange ${exchangeId}).")
                .end()
                .log(LoggingLevel.DEBUG, LOGGER_NAME, "All WFS harvested.");

        /**
         * This route get `uuid` `wfsUrl` and `featureType` properties from JSM message.
         * It creates a bean FeatureTypeBean to store these properties, the attribute
         * types and the WFSDatastore.
         * This bean will be pass to next Route.
         */
        from("activemq:queue:" + MESSAGE_HARVEST_WFS_FEATURES)
                .id("harvest-wfs-start-from-message")
                .log(LoggingLevel.INFO, LOGGER_NAME, "Harvest features message received.")
                .setProperty("mduuid", simple("${body.uuid}"))
                .setProperty("wfsUrl", simple("${body.wfsUrl}"))
                .setProperty("featureType", simple("${body.featureType}"))
                .bean(FeatureTypeBean.class, "initialize(*, true)")
                .to("direct:delete-wfs-featuretype-features")
                .to("direct:index-wfs");

        from("activemq:queue:" + MESSAGE_DELETE_WFS_FEATURES)
                .id("harvest-wfs-delete-features-from-message")
                .log(LoggingLevel.INFO, LOGGER_NAME, "Delete features message received.")
                .setProperty("wfsUrl", simple("${body.wfsUrl}"))
                .setProperty("featureType", simple("${body.featureType}"))
                .bean(FeatureTypeBean.class, "initialize(*, false)")
                .to("direct:delete-wfs-featuretype-features");

        from("direct:delete-wfs-featuretype-features")
                .id("harvest-wfs-delete-features")
                .log(LoggingLevel.INFO, "Removing features ...")
                .beanRef("featureIndexer", "deleteFeatures")
                .log(LoggingLevel.INFO, "Features removed.");

        from("direct:index-wfs")
                .id("harvest-wfs-features")
                .log(LoggingLevel.INFO, "Indexing features ...")
                .beanRef("featureIndexer", "featureToIndexDocument")
                .log(LoggingLevel.INFO, "Features indexed.");
    }
}


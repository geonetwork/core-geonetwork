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

package org.fao.geonet.harvester.wfsfeatures.worker;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.XPathBuilder;
import org.apache.camel.component.exec.ExecResult;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;
import org.springframework.util.StreamUtils;
import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import java.nio.charset.StandardCharsets;

import static org.apache.camel.component.exec.ExecBinding.EXEC_COMMAND_ARGS;
import static org.apache.camel.component.exec.ExecBinding.EXEC_COMMAND_WORKING_DIR;

/**
 * Created by francois on 28/10/15.
 */
public class WFSHarvesterRouteBuilder extends RouteBuilder {
    public static final String LOGGER_NAME = "geonetwork.harvest.wfs.features";
    public static final String MESSAGE_HARVEST_WFS_FEATURES = "harvest-wfs-features";
    public static final String MESSAGE_DELETE_WFS_FEATURES = "delete-wfs-features";
    public static final String HARVEST_WFS_FEATURES_QUEUE_URI = "activemq:queue:" + MESSAGE_HARVEST_WFS_FEATURES + "?concurrentConsumers=5";

    public static final String MESSAGE_HARVEST_OGR_FEATURES = "harvest-ogr-features";
    public static final String MESSAGE_DELETE_OGR_FEATURES = "delete-ogr-features";
    public static final String HARVEST_OGR_FEATURES_QUEUE_URI = "activemq:queue:" + MESSAGE_HARVEST_OGR_FEATURES + "?concurrentConsumers=5";

    private boolean startsFromXMLConfigurationFile = false;

    public boolean isStartsFromXMLConfigurationFile() {
        return startsFromXMLConfigurationFile;
    }

    public void setStartsFromXMLConfigurationFile(boolean isStartingFromXMLConfigurationFile) {
        this.startsFromXMLConfigurationFile = isStartingFromXMLConfigurationFile;
    }


    @Override
    public void configure() {
        onException(Exception.class)
                .handled(true)
                .logStackTrace(true)
                .log(LoggingLevel.ERROR, LOGGER_NAME,
                        "Exception occured: ${exception.message}")
                .log(LoggingLevel.ERROR, LOGGER_NAME,
                        "Exception occured: ${exception.stacktrace}")
                .log(LoggingLevel.ERROR, LOGGER_NAME,
                        "Harvesting task terminated due to previous exception (Exchange ${exchangeId}).");
        JaxbDataFormat jaxb = new JaxbDataFormat(false);
        jaxb.setContextPath(WFSHarvesterParameter.class.getPackage().getName());

        /**
         * Start the indexing from the XML configuration file.
         * Used for testing mainly.
         */
        XPathBuilder xPathWfsConfigBuilder = new XPathBuilder("//wfs");
        from("file:src/test/resources/?fileName=wfs.xml&noop=true")
                .id("harvest-wfs-start-from-file")
                .autoStartup(startsFromXMLConfigurationFile)
                .log(LoggingLevel.INFO, LOGGER_NAME, "Harvest features from XML configuration file.")
                .convertBodyTo(Document.class)
                .log(LoggingLevel.DEBUG, LOGGER_NAME, "Content is: ${body}")
                .split(xPathWfsConfigBuilder)
                    .parallelProcessing()
                    // Will not stop on exception if one of the splitted task fails
                    .executorServiceRef("harvest-wfs-thread-pool")
                        .unmarshal(jaxb)
                        .setProperty("configuration", simple("${body}"))
//                        .setProperty("url", xpath("wfs/@url", String.class))
//                        .setProperty("typeName", xpath("wfs/@typeName", String.class))
                        .log(LoggingLevel.INFO, LOGGER_NAME, "#${property.CamelSplitIndex}. Harvesting ${property.configuration.url} - start (Exchange ${exchangeId}).")
                        .beanRef("WFSFeatureIndexer", "initialize(*, true)")
                        .to("direct:delete-wfs-featuretype-features")
                        .to("direct:index-wfs")
                        .log(LoggingLevel.INFO, LOGGER_NAME, "#${property.CamelSplitIndex}. Harvesting ${property.configuration.url} - end (Exchange ${exchangeId}).")
                .end()
                .log(LoggingLevel.DEBUG, LOGGER_NAME, "All WFS harvested.");

        /**
         * This route get `uuid` `wfsUrl` and `featureType` properties from JSM message.
         * It creates a bean FeatureTypeBean to store these properties, the attribute
         * types and the WFSDatastore.
         * This bean will be pass to next Route.
         */
        from(HARVEST_WFS_FEATURES_QUEUE_URI)
                .id("harvest-wfs-start-from-message")
                .log(LoggingLevel.INFO, LOGGER_NAME, "Harvest features message received.")
                .log(LoggingLevel.INFO, LOGGER_NAME, "${body}")
                .setProperty("configuration", simple("${body.parameters}"))
                .beanRef("WFSFeatureIndexer", "initialize(*, true)")
                .to("direct:delete-wfs-featuretype-features")
                .to("direct:index-wfs");

        from("activemq:queue:" + MESSAGE_DELETE_WFS_FEATURES + "?concurrentConsumers=5")
                .id("harvest-wfs-delete-features-from-message")
                .log(LoggingLevel.INFO, LOGGER_NAME, "Delete features message received.")
                .setProperty("url", simple("${body.parameters.url}"))
                .setProperty("typeName", simple("${body.parameters.typeName}"))
                .to("direct:delete-wfs-featuretype-features");

        from("direct:delete-wfs-featuretype-features")
                .id("harvest-wfs-delete-features")
                .log(LoggingLevel.INFO, "Removing features from ${property.url}#${property.typeName} ...")
                .beanRef("WFSFeatureIndexer", "deleteFeatures")
                .log(LoggingLevel.INFO, "All features from ${property.url}#${property.typeName} removed.");

        from("direct:index-wfs")
                .id("harvest-wfs-features")
                .log(LoggingLevel.INFO, "Indexing features from ${property.url}#${property.typeName} ...")
                .beanRef("WFSFeatureIndexer", "indexFeatures", false)
                .log(LoggingLevel.INFO, "All features from ${property.url}#${property.typeName} indexed.");



        from(HARVEST_OGR_FEATURES_QUEUE_URI)
                .id("harvest-ogr-start-from-message")
                .log(LoggingLevel.INFO, LOGGER_NAME, "Harvest features message received.")
                .log(LoggingLevel.INFO, LOGGER_NAME, "${body}")
                .setProperty("configuration", simple("${body.parameters}"))
//                .beanRef("OGRFeatureIndexer", "initialize(*, true)")
                .to("direct:delete-ogr-featuretype-features")
                .to("direct:index-ogr");

        from("activemq:queue:" + MESSAGE_DELETE_OGR_FEATURES + "?concurrentConsumers=5")
                .id("harvest-ogr-delete-features-from-message")
                .log(LoggingLevel.INFO, LOGGER_NAME, "Delete features message received.")
                .setProperty("url", simple("${body.parameters.url}"))
                .setProperty("typeName", simple("${body.parameters.typeName}"))
                .to("direct:delete-ogr-featuretype-features");

        from("direct:delete-ogr-featuretype-features")
                .id("harvest-ogr-delete-features")
                .log(LoggingLevel.INFO, "Removing features from ${property.url}#${property.typeName} ...")
                .beanRef("OGRFeatureIndexer", "deleteFeatures")
                .log(LoggingLevel.INFO, "All features from ${property.url}#${property.typeName} removed.");

        from("direct:index-ogr")
                .id("harvest-ogr-features")
                .log(LoggingLevel.INFO, "Indexing features from ${property.url}#${property.typeName} ...")
                .setHeader(EXEC_COMMAND_ARGS)
                    .method("OGRFeatureIndexer", "getCommandArgs")
                .setHeader(EXEC_COMMAND_WORKING_DIR)
                    .method("OGRFeatureIndexer", "getWorkingDir")
                .to("exec:docker")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        System.out.println(exchange.getIn().getHeader(EXEC_COMMAND_ARGS));
                        ExecResult execResult = exchange.getIn().getBody(ExecResult.class);
                        System.out.println(execResult.getExitValue());
                        System.out.println(StreamUtils.copyToString(execResult.getStdout(), StandardCharsets.UTF_8));
                        System.out.println(StreamUtils.copyToString(execResult.getStderr(), StandardCharsets.UTF_8));
                    }
                })
                .log(LoggingLevel.INFO, "All features from ${property.url}#${property.typeName} indexed.");
    }
}

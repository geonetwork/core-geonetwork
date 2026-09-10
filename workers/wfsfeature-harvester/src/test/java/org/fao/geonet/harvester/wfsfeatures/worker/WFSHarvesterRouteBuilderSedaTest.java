/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;

/**
 * Verifies that messages produced to the SEDA endpoints defined by
 * {@link WFSHarvesterRouteBuilder} are actually consumed by the matching routes.
 *
 * <p>This guards the endpoint URIs used by producers (the REST API and the
 * periodic quartz producer) against drifting away from the consumer {@code from(...)}
 * URIs after the migration from ActiveMQ to Camel SEDA. In particular it protects
 * against reintroducing the ActiveMQ-style {@code queue:} prefix on the delete
 * route, which would silently make producers and consumer reference different
 * SEDA queues.</p>
 *
 * <p>The consumer routes are advised so that the exchange is short-circuited to a
 * mock endpoint right after {@code from(...)}, so the test does not depend on the
 * downstream {@code WFSFeatureIndexer}/Elasticsearch stack.</p>
 */
public class WFSHarvesterRouteBuilderSedaTest extends CamelTestSupport {

    private static final String DELETE_CONSUMER_URI =
        "seda:" + WFSHarvesterRouteBuilder.MESSAGE_DELETE_WFS_FEATURES + "?concurrentConsumers=5";

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new WFSHarvesterRouteBuilder();
    }

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry registry = super.createRegistry();
        // The routes reference these by name; Camel resolves them when the routes
        // are created, so they must exist even though the advised routes stop()
        // before the indexer is ever invoked.
        registry.bind("WFSFeatureIndexer", mock(EsWFSFeatureIndexer.class));
        registry.bind("harvest-wfs-thread-pool", Executors.newSingleThreadExecutor());
        return registry;
    }

    @Override
    public boolean isUseAdviceWith() {
        // Let the test advise the routes before the context is started.
        return true;
    }

    /**
     * Finds a route by its consumer (from) URI. Route ids are not yet materialized
     * before the context is started, so we cannot look routes up by id here.
     */
    private RouteDefinition routeConsuming(String fromUri) {
        return context.getRouteDefinitions().stream()
            .filter(r -> !r.getInputs().isEmpty() && fromUri.equals(r.getInputs().get(0).getUri()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No route consumes from " + fromUri));
    }

    @Test
    public void harvestMessageReachesSedaConsumer() throws Exception {
        // Asserts a consumer exists on exactly HARVEST_WFS_FEATURES_SEDA_URI.
        routeConsuming(WFSHarvesterRouteBuilder.HARVEST_WFS_FEATURES_SEDA_URI)
            .adviceWith(context, new AdviceWithRouteBuilder() {
                @Override
                public void configure() {
                    weaveAddFirst().to("mock:harvestReceived").stop();
                }
            });
        context.start();

        MockEndpoint mock = getMockEndpoint("mock:harvestReceived");
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("harvest-payload");

        // Producers must use the exact same URI as the consumer from(...).
        template.sendBody(WFSHarvesterRouteBuilder.HARVEST_WFS_FEATURES_SEDA_URI, "harvest-payload");

        mock.assertIsSatisfied();
    }

    @Test
    public void deleteMessageReachesSedaConsumer() throws Exception {
        // Asserts the delete consumer uses the plain SEDA name, without the
        // ActiveMQ-style "queue:" prefix that the migration removed.
        routeConsuming(DELETE_CONSUMER_URI)
            .adviceWith(context, new AdviceWithRouteBuilder() {
                @Override
                public void configure() {
                    weaveAddFirst().to("mock:deleteReceived").stop();
                }
            });
        context.start();

        MockEndpoint mock = getMockEndpoint("mock:deleteReceived");
        mock.expectedMessageCount(1);
        mock.expectedBodiesReceived("delete-payload");

        // Producing to the plain name must reach the consumer (SEDA shares the
        // queue by name regardless of endpoint options).
        template.sendBody("seda:" + WFSHarvesterRouteBuilder.MESSAGE_DELETE_WFS_FEATURES, "delete-payload");

        mock.assertIsSatisfied();
    }
}

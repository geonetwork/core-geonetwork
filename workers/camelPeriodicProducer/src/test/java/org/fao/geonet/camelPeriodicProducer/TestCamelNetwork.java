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

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.quartz2.QuartzComponent;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;

public class TestCamelNetwork extends RouteBuilder {

    private MessageProducerTest.MessageConsumer messageConsumer;
    private MessageProducerControllerTest.MessageConsumer wfsHarvesterParamConsumer;

    @Autowired
    public QuartzComponent quartzComponent;

    public TestCamelNetwork() {
        messageConsumer = new MessageProducerTest.MessageConsumer("direct:consumer");
        wfsHarvesterParamConsumer = new MessageProducerControllerTest.MessageConsumer("direct:wfsHravesterParamConsumer");
    }

    @PostConstruct
    public void init() throws Exception {
        quartzComponent.start();
        quartzComponent.setCamelContext(this.getContext());
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

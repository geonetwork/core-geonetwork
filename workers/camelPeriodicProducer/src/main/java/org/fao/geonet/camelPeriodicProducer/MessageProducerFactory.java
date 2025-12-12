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

import org.apache.camel.CamelContext;
import org.apache.camel.ExtendedCamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.quartz.QuartzComponent;
import org.apache.camel.component.quartz.QuartzEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.fao.geonet.harvester.wfsfeatures.worker.WFSHarvesterRouteBuilder;
import org.fao.geonet.kernel.setting.SettingManager;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

public class MessageProducerFactory {

    private static final String NEVER = "59 59 23 31 12 ? 2099";
    @Autowired
    protected RouteBuilder routeBuilder;
    @Autowired
    protected QuartzComponent quartzComponent;
    @Autowired
    protected SettingManager settingManager;

    private static Logger LOGGER = LoggerFactory.getLogger(WFSHarvesterRouteBuilder.LOGGER_NAME);

    @PostConstruct
    public void init() throws Exception {
        try {
            quartzComponent.start();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void registerAndStart(MessageProducer messageProducer) throws Exception {
        quartzComponent.createEndpoint(buildFrom(messageProducer.getId()));
        writeRoute(messageProducer);
        reschedule(messageProducer);
    }

    public void reschedule(MessageProducer messageProducer) throws Exception {
        String id = buildFrom(messageProducer.getId());
        QuartzEndpoint toReschedule = (QuartzEndpoint) routeBuilder.getContext().getEndpoints().stream()
            .filter(x -> x.getEndpointKey().compareTo(id) == 0).findFirst().get();

        String msgCronExpression = messageProducer.getCronExpession() == null ? NEVER : messageProducer.getCronExpession();
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(toReschedule.getTriggerKey()).withSchedule(CronScheduleBuilder.cronSchedule(msgCronExpression)).build();

        quartzComponent.getScheduler().interrupt(toReschedule.getId());
        quartzComponent.getScheduler().rescheduleJob(toReschedule.getTriggerKey(), trigger);
        Optional<RouteDefinition> route = findRoute(messageProducer.getId());
        if (route.isPresent()) {
            routeBuilder.getContext().getRouteController().startRoute(route.get().getId());
        }
    }

    public void changeMessageAndReschedule(MessageProducer messageProducer) throws Exception {
        Optional<RouteDefinition> routeDefinition = findRoute(messageProducer.getId());
        if (routeDefinition.isPresent()) {
            removeRoute(routeDefinition.get());
        }
        writeRoute(messageProducer);
        reschedule(messageProducer);
    }
    private void removeRoute(RouteDefinition routeDefinition) throws Exception {
        boolean removed = false;

        CamelContext context = routeBuilder.getContext();
        String routeId = routeDefinition.getId();
        try {
            context.getRouteController().stopRoute(routeId);
            context.removeRoute(routeId);
        }
        finally {
            if (removed) {
                LOGGER.trace("Route {} was successfully stopped and removed.", routeId);
            } else {
                LOGGER.warn("Route {} could not be removed (perhaps it was already stopped).", routeId);
            }
        }
    }

    public void destroy(Long id) throws Exception {
        Optional<RouteDefinition> route = findRoute(id);
        if (route.isPresent()) {
            removeRoute(route.get());
        }
        routeBuilder.getContext().removeEndpoints(buildFrom(id));
    }

    /** RouteID for Quartz identifier.
     * @param id Numeric quartz identifier
     * @return RouteID text
     */
    private String buildFrom(Long id) {
        return String.format("quartz2://%s-%s",
            settingManager.getSiteId(),
            id);
    }

    public void shutdown() {
        try {
            quartzComponent.shutdown();
        } catch (Exception e) {
            LOGGER.error("Error while trying to shutdown quartz", e);
        }
    }

    private void writeRoute(MessageProducer messageProducer) throws Exception {
        RouteDefinition routeDefinition = routeBuilder
            .from(buildFrom(messageProducer.getId()))
            .noAutoStartup()
            .setBody(routeBuilder.constant(messageProducer.getMessage()))
            .to(messageProducer.getTargetUri());

        ExtendedCamelContext extendedContext = routeBuilder.getContext().getCamelContextExtension();
        ModelCamelContext camelContext = extendedContext.getContextPlugin(ModelCamelContext.class);

        camelContext.addRouteDefinition(routeDefinition);
    }

    private Optional<RouteDefinition> findRoute(Long id) {
        ExtendedCamelContext extendedContext = routeBuilder.getContext().getCamelContextExtension();
        ModelCamelContext camelContext = extendedContext.getContextPlugin(ModelCamelContext.class);

        return routeBuilder.getRouteCollection().getRoutes()
            .stream()
            .filter(route -> routeInputHasQuart2RouteIdUrl(route, id))
            .map(Optional::ofNullable)
            .findFirst()
            .orElse(Optional.empty());
    }

    private boolean routeInputHasQuart2RouteIdUrl(RouteDefinition route, Long id) {
        if( route.getInput() != null ) {
            return route.getInput().getUri().equalsIgnoreCase( buildFrom(id));
        }
        return false;
    }
}

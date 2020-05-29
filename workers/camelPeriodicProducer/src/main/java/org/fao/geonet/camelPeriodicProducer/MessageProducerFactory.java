package org.fao.geonet.camelPeriodicProducer;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.quartz2.QuartzComponent;
import org.apache.camel.component.quartz2.QuartzEndpoint;
import org.apache.camel.model.RouteDefinition;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

public class MessageProducerFactory {

    @Autowired
    protected RouteBuilder routeBuilder;

    @Autowired
    protected QuartzComponent quartzComponent;

    private static final String NEVER = "59 59 23 31 12 ? 2099";

    @PostConstruct
    public void init() throws Exception {
        quartzComponent.start();
    }

    public void registerAndStart(MessageProducer messageProducer) throws Exception {
        quartzComponent.createEndpoint("quartz2://" + messageProducer.getId());
        writeRoute(messageProducer);
        reschedule(messageProducer);
    }

    public void reschedule(MessageProducer messageProducer) throws Exception {
        QuartzEndpoint toReschedule = (QuartzEndpoint) routeBuilder.getContext().getEndpoints().stream()
                .filter(x -> x.getEndpointKey().compareTo("quartz2://" + messageProducer.getId()) == 0).findFirst().get();

        String msgCronExpression = messageProducer.getCronExpession() == null ? NEVER : messageProducer.getCronExpession();
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(toReschedule.getTriggerKey()).withSchedule(CronScheduleBuilder.cronSchedule(msgCronExpression)).build();

        quartzComponent.getScheduler().rescheduleJob(toReschedule.getTriggerKey(), trigger);
        routeBuilder.getContext().startRoute(findRoute(messageProducer.getId()).getId());
    }

    public void changeMessageAndReschedule(MessageProducer messageProducer) throws Exception {
        routeBuilder.getContext().removeRouteDefinition(findRoute(messageProducer.getId()));
        writeRoute(messageProducer);
        reschedule(messageProducer);
    }

    public void destroy(Long id) throws Exception {
        routeBuilder.getContext().removeRouteDefinition(findRoute(id));
        routeBuilder.getContext().removeEndpoints("quartz2://" + id);
    }

    private void writeRoute(MessageProducer messageProducer) throws Exception {
        RouteDefinition routeDefinition = routeBuilder
                .from("quartz2://" + messageProducer.getId())
                .noAutoStartup()
                .setBody(routeBuilder.constant(messageProducer.getMessage()))
                .to(messageProducer.getTargetUri());
        routeBuilder.getContext().addRouteDefinition(routeDefinition);
    }

    private RouteDefinition findRoute(Long id) {
        return routeBuilder.getContext().getRouteDefinitions()
                .stream()
                .filter(route -> routeInputHasQuart2RouteIdUrl(route, id))
                .findFirst()
                .get();
    }

    private boolean routeInputHasQuart2RouteIdUrl(RouteDefinition route, Long id) {
        return route.getInputs().size() == 1 && route.getInputs().get(0).getUri().equalsIgnoreCase("quartz2://" + id);
    }
}

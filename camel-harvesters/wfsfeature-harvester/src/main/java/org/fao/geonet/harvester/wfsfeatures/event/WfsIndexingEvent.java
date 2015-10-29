package org.fao.geonet.harvester.wfsfeatures.event;

/**
 * Created by fgravin on 10/29/15.
 */
import org.springframework.context.ApplicationEvent;

public class WfsIndexingEvent extends ApplicationEvent{

    final String url;

    public String getUrl() {
        return url;
    }

    public WfsIndexingEvent(Object source, final String url) {
        super(source);
        this.url = url;
        System.out.println("Created a Custom event with url = " + this.url);
    }

    @Override
    public String toString() {
        return this.url;
    }}
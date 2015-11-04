package org.fao.geonet.harvester.wfsfeatures.event;

/**
 * Created by fgravin on 10/29/15.
 */
import org.springframework.context.ApplicationEvent;

public class WfsIndexingEvent extends ApplicationEvent{

    final String url;
    final String linkage;
    final String uuid;

    public String getUrl() {
        return url;
    }

    public String getLinkage() {
        return linkage;
    }

    public String getUuid() {
        return uuid;
    }

    public WfsIndexingEvent(Object source, final String uuid, final String linkage, final String url ) {
        super(source);
        this.url = url;
        this.uuid = uuid;
        this.linkage = linkage;
        System.out.println("Created a Custom event with url = " + this.url);
    }

    @Override
    public String toString() {
        return this.url;
    }}
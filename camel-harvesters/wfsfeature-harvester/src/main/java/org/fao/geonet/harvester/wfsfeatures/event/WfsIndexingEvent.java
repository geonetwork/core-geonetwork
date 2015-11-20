package org.fao.geonet.harvester.wfsfeatures.event;

/**
 * Created by fgravin on 10/29/15.
 */
import org.springframework.context.ApplicationEvent;

public class WfsIndexingEvent extends ApplicationEvent{

    final String featureType;
    final String wfsUrl;
    final String uuid;

    public String getFeatureType() {
        return featureType;
    }

    public String getWfsUrl() {
        return wfsUrl;
    }

    public String getUuid() {
        return uuid;
    }

    public WfsIndexingEvent(Object source, final String uuid, final String wfsUrl, final String featureType ) {
        super(source);
        this.featureType = featureType;
        this.uuid = uuid;
        this.wfsUrl = wfsUrl;
        System.out.println("Created a Custom event with url = " + this.featureType);
    }

    @Override
    public String toString() {
        return this.featureType;
    }}
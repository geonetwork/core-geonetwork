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
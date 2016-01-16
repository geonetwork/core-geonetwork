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

import org.fao.geonet.harvester.wfsfeatures.model.WFSHarvesterParameter;

import org.springframework.context.ApplicationEvent;

public class WFSHarvesterEvent extends ApplicationEvent{


    private WFSHarvesterParameter parameters;

    public WFSHarvesterEvent setParameters(WFSHarvesterParameter parameters) {
        this.parameters = parameters;
        return this;
    }

    public WFSHarvesterParameter getParameters() {
        return parameters;
    }

    public WFSHarvesterEvent(Object source, WFSHarvesterParameter parameters) {
        super(source);
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
        sb.append("\n").append(parameters.toString());
        return sb.toString();
    }
}
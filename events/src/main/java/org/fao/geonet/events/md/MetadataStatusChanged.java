/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
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

package org.fao.geonet.events.md;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.StatusValue;

/**
 * Event launched when a record changes its status
 *
 * @author delawen
 */
public class MetadataStatusChanged extends MetadataEvent {

    private static final long serialVersionUID = 324534556246220509L;
    private StatusValue status;
    private String message;
    private Integer user;

    public MetadataStatusChanged(AbstractMetadata abstractMetadata, StatusValue status, String message, Integer user) {
        super(abstractMetadata);
        if (status == null) {
            throw new IllegalArgumentException("Status can't be null");
        }
        this.status = status;
        this.message = message;
        this.user = user;
    }

    public StatusValue getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Integer getUser() {
        return user;
    }

}

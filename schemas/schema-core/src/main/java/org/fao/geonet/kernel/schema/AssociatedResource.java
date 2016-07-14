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

package org.fao.geonet.kernel.schema;

/**
 * Created by francois on 8/19/14.
 */
public class AssociatedResource {
    private String uuid;
    private String initiativeType;
    private String associationType;

    public AssociatedResource(String uuid, String initiativeType, String associationType) {
        this.uuid = uuid;
        this.initiativeType = initiativeType;
        this.associationType = associationType;
    }

    public String getUuid() {
        return uuid;
    }

    public AssociatedResource setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getInitiativeType() {
        if (initiativeType == null) {
            return "";
        } else {
            return initiativeType;
        }
    }

    public AssociatedResource setInitiativeType(String initiativeType) {
        this.initiativeType = initiativeType;
        return this;
    }

    public String getAssociationType() {
        if (associationType == null) {
            return "";
        } else {
            return associationType;
        }
    }

    public AssociatedResource setAssociationType(String associationType) {
        this.associationType = associationType;
        return this;
    }
}

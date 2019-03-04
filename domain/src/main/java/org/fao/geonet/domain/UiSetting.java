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
package org.fao.geonet.domain;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * The Class corresponding to the UI JSON configuration.
 */
@Entity(name = "Settings_ui")
@Table(name = "Settings_ui")
public class UiSetting extends GeonetEntity implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -6757685605075101098L;

    /**
     * The identifier.
     */
    private String id;

    /**
     * The JSON configuration for the interface.
     */
    private String configuration;

    /**
     * Instantiates a new css style settings model.
     */
    public UiSetting() {
    }

    public UiSetting(String id, String configuration) {
        this.id = id;
        this.configuration = configuration;
    }

    @Id
    @Column(nullable = false)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Lob
    @Column(nullable = true)
    @Type(type = "org.hibernate.type.StringClobType")
    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

}

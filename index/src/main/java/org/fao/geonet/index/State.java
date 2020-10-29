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
package org.fao.geonet.index;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * The index status
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum State {
    RED("red", "Red", "danger", 1000),
    UNINITIALIZED("uninitialized", "uninitialized", "spinner", 900),
    YELLOW("yellow", "Yellow", "warning", 800),
    GREEN("green", "Green", "success", 0),
    DISABLED("disabled", "Disabled", "toggle-off", -1);

    private final String id;
    private final String title;
    private final String icon;
    private final int severity;

    State(String id, String title, String icon, int severity) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public int getSeverity() {
        return severity;
    }

    public String getId() {
        return id;
    }
}

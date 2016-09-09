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

package org.fao.geonet.api.records.formatters.groovy;

/**
 * A mode is a way of grouping {@link org.fao.geonet.api.records.formatters.groovy.Handler} and
 * {@link org.fao.geonet.api.records.formatters.groovy.Sorter} so that groups of handlers and
 * sorters can be partitioned.
 *
 * @author Jesse on 10/22/2014.
 */
public class Mode {
    public static final String DEFAULT = "";
    private String id;
    private String fallback;

    public Mode(String modeId, String fallback) {
        this.id = modeId;
        this.fallback = fallback;
    }

    public Mode(String modeId) {
        this(modeId, null);
    }

    /**
     * The id of this mode
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The id of another mode which will be used if the object (Handler or Sorter) was not found in
     * this mode.  This can be null if not fallback is desired.
     */
    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }
}

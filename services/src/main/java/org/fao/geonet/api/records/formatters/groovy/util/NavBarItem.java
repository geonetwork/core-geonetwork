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

package org.fao.geonet.api.records.formatters.groovy.util;

/**
 * An Nav Bar item.
 *
 * @author Jesse on 12/1/2014.
 */
public class NavBarItem {
    private String abbrName;
    private String name, rel, href;

    public NavBarItem() {
        // no op
    }

    /**
     * Constructor.
     *
     * @param name     the translated full name of the group represented by this item.  This will be
     *                 displayed as a tool tip (see view-header.html)
     * @param abbrName a shorter translated name to use in the display to keep the size of the items
     *                 reasonable.
     * @param rel      the value of the rel attribute
     */
    public NavBarItem(String name, String abbrName, String rel) {
        this(name, abbrName, rel, "");
    }

    /**
     * Constructor.
     *
     * @param name     the translated full name of the group represented by this item.  This will be
     *                 displayed as a tool tip (see view-header.html)
     * @param abbrName a shorter translated name to use in the display to keep the size of the items
     *                 reasonable.
     * @param rel      the value of the rel attribute
     * @param href     the value of the href attribute
     */
    public NavBarItem(String name, String abbrName, String rel, String href) {
        this.name = name;
        this.abbrName = abbrName;
        if (abbrName == null || abbrName.isEmpty()) {
            this.abbrName = name;
        }
        this.rel = rel;
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getAbbrName() {
        return abbrName;
    }

    public void setAbbrName(String abbrName) {
        this.abbrName = abbrName;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}

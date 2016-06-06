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

import org.fao.geonet.entitylistener.CustomElementSetEntityListenerManager;

import javax.persistence.*;

/**
 * Csw custom element set. This is part of the CSW specification related to what elements are
 * returned by GetRecords and GetRecordById.
 *
 * @author Jesse
 */
@Entity
@Access(AccessType.PROPERTY)
@Table(name = "CustomElementSet")
@EntityListeners(CustomElementSetEntityListenerManager.class)
public class CustomElementSet extends GeonetEntity {
    private static final int XPATH_COLUMN_LENGTH = 1000;
    private String _xpath;
    private int _xpathHashcode;

    /**
     * The hashcode of the xpath.  This has to be the id because mysql + JPA have a problem with a
     * Ids longer than 255 characters.
     *
     * @return the hashcode of the xpath.
     */
    @Id
    public int getXpathHashcode() {
        return _xpathHashcode;
    }

    /**
     * Set the xpath hashcode. Method is protected because it is set when calling setXPath.
     *
     * @param xpathHashcode the hashcode.
     */
    protected void setXpathHashcode(int xpathHashcode) {
        this._xpathHashcode = xpathHashcode;
    }

    /**
     * Get the xpath of the element to include in the element set. Each included element is
     * described by a full xpath relative to the document root. <br/> This is a required element.
     */
    @Column(length = XPATH_COLUMN_LENGTH, nullable = false)
    public String getXpath() {
        return _xpath;
    }

    /**
     * Get the xpath of the element to include in the element set. Each included element is
     * described by a full xpath relative to the document root. <br/> This is a required element.
     *
     * @param xpath the xpath relative to document root.
     * @return this object
     */
    public CustomElementSet setXpath(final String xpath) {
        this._xpath = xpath;
        setXpathHashcode(xpath.hashCode());
        return this;
    }
}

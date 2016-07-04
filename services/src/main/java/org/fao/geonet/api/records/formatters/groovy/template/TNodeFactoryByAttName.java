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

package org.fao.geonet.api.records.formatters.groovy.template;

import org.fao.geonet.SystemInfo;
import org.xml.sax.Attributes;

/**
 * Implementation where the applicable method is based on analyzing the element's attributes and
 * checking if the attribute is one of the "applicable" names.
 *
 * @author Jesse on 11/29/2014.
 */
public abstract class TNodeFactoryByAttName extends TNodeFactory {
    protected final SystemInfo testingInfo;
    private final String applicableAtt;

    protected TNodeFactoryByAttName(String applicableAtt, SystemInfo testingInfo) {
        this.applicableAtt = applicableAtt;
        this.testingInfo = testingInfo;
    }

    @Override
    public final boolean applicable(String localName, String qName, Attributes attributes) {
        return hasAttribute(attributes, applicableAtt);
    }
}

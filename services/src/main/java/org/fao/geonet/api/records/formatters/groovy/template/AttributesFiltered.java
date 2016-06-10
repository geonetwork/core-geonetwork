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

import com.google.common.collect.Sets;

import org.apache.xalan.xsltc.runtime.AttributeList;
import org.xml.sax.Attributes;

import java.util.Set;

/**
 * Represents a subset of an element's attributes
 *
 * @author Jesse on 11/29/2014.
 */
public class AttributesFiltered implements Attributes {
    private final AttributeList filtered = new AttributeList();

    public AttributesFiltered(Attributes unfiltered, String... excludeNames) {
        Set<String> allExcludes = Sets.newHashSet();
        for (String excludeName : excludeNames) {
            for (String prefix : TNodeFactory.ATTRIBUTE_NAME_PREFIXES) {
                allExcludes.add(prefix + excludeName);
            }
        }
        for (int i = 0; i < unfiltered.getLength(); i++) {
            String attName = unfiltered.getQName(i);
            if (!allExcludes.contains(attName)) {
                filtered.add(unfiltered.getQName(i), unfiltered.getValue(i));
            }
        }
    }

    @Override
    public String getValue(String uri, String localName) {
        return filtered.getValue(uri, localName);
    }

    @Override
    public int getLength() {
        return filtered.getLength();
    }

    @Override
    public String getURI(int index) {
        return filtered.getURI(index);
    }

    @Override
    public String getLocalName(int index) {
        return filtered.getLocalName(index);
    }

    @Override
    public String getQName(int pos) {
        return filtered.getQName(pos);
    }

    @Override
    public String getType(int index) {
        return filtered.getType(index);
    }

    @Override
    public int getIndex(String namespaceURI, String localPart) {
        return filtered.getIndex(namespaceURI, localPart);
    }

    @Override
    public int getIndex(String qname) {
        return filtered.getIndex(qname);
    }

    @Override
    public String getType(String uri, String localName) {
        return filtered.getType(uri, localName);
    }

    @Override
    public String getType(String qname) {
        return filtered.getType(qname);
    }

    @Override
    public String getValue(int pos) {
        return filtered.getValue(pos);
    }

    @Override
    public String getValue(String qname) {
        return filtered.getValue(qname);
    }
}

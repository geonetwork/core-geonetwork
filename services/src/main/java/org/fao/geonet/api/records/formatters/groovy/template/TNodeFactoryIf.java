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
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;

import java.io.IOException;

/**
 * Creates fmt-if nodes.
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TNodeFactoryIf extends TNodeFactoryByAttName {

    public static final String IF = "if";
    public static final String ONLY_CHILDREN = "only-children";

    public TNodeFactoryIf(SystemInfo info, TextContentParser contentParser) {
        super(IF, info);
        this.textContentParser = contentParser;
    }

    public TNodeFactoryIf() {
        super(IF, null);
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        final String value = getValue(attributes, IF);
        final boolean onlyChildren = getBooleanAttribute(attributes, ONLY_CHILDREN, false);

        final AttributesFiltered attributesFiltered = new AttributesFiltered(attributes, IF, ONLY_CHILDREN);
        return new TNodeIf(SystemInfo.getInfo(this.testingInfo), textContentParser, qName, attributesFiltered, value, onlyChildren);
    }
}

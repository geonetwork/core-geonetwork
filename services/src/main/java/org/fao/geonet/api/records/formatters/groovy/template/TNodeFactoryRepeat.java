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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Creates if/non-empty/etc... nodes.
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TNodeFactoryRepeat extends TNodeFactoryByAttName {
    public static final String REPEAT = "repeat";
    public static final String ONLY_CHILDREN = REPEAT + "-only-children";
    private static final Pattern MAP_PATTERN = Pattern.compile("\\s*\\(\\s*(\\S+)\\s*,\\s*(\\S+)\\s*\\)");

    protected TNodeFactoryRepeat() {
        super(REPEAT, null);
    }

    public TNodeFactoryRepeat(SystemInfo info, TextContentParser contentParser) {
        super(REPEAT, info);
        this.textContentParser = contentParser;
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        String value = getValue(attributes, REPEAT);
        final boolean onlyChildren = getBooleanAttribute(attributes, ONLY_CHILDREN, false);
        final String[] parts = value.split("\\s+in\\s+");
        final AttributesFiltered attributesFiltered = new AttributesFiltered(attributes, REPEAT, ONLY_CHILDREN);

        String key = parts[1];
        Objects.requireNonNull(key);
        String contextName = parts[0];
        Objects.requireNonNull(contextName);

        Matcher mapMatcher = MAP_PATTERN.matcher(contextName);

        SystemInfo info = SystemInfo.getInfo(this.testingInfo);
        if (mapMatcher.matches()) {
            return new TNodeRepeatMap(info, textContentParser, onlyChildren, qName, attributesFiltered, key, mapMatcher.group(1), mapMatcher.group(2));
        } else {
            return new TNodeRepeatIter(info, textContentParser, onlyChildren, qName, attributesFiltered, key, contextName);
        }
    }
}

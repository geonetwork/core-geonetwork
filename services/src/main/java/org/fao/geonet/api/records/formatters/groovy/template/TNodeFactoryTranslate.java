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
 * Creates fmt-translate nodes.
 *
 * A translate node take the text content element and find a translation.  depending on the value of
 * the fmt-translate attribute the translation is resolved using the {@link
 * org.fao.geonet.api.records.formatters.groovy.Functions} translate methods.
 *
 * Examples:
 * <pre><code>
 * &lt;div fmt-translate="">testString&lt;/div>
 * &lt;div fmt-translate="default">testString&lt;/div>
 * &lt;div fmt-translate=">file">testString&lt;/div>
 * &lt;div fmt-translate="default>file">testString&lt;/div>
 * &lt;div fmt-translate="codelist>name">testString&lt;/div>
 * &lt;div fmt-translate="codelist>desc>context1">testString&lt;/div>
 * &lt;div fmt-translate="codelist>desc>context2">testString&lt;/div>
 * &lt;div fmt-translate="node>name>context">testString&lt;/div>
 * &lt;div fmt-translate="node>desc">testString&lt;/div>
 * </code></pre>
 *
 * @author Jesse on 11/29/2014.
 */
@Component
public class TNodeFactoryTranslate extends TNodeFactoryByAttName {
    public static final String TRANSLATE = "translate";

    public TNodeFactoryTranslate() {
        super(TRANSLATE, null);
    }

    public TNodeFactoryTranslate(SystemInfo info, TextContentParser textContentParser) {
        super(TRANSLATE, info);
        this.textContentParser = textContentParser;
    }

    @Override
    public TNode create(String localName, String qName, Attributes attributes) throws IOException {
        final String value = getValue(attributes, TRANSLATE);
        final AttributesFiltered attributesFiltered = new AttributesFiltered(attributes, TRANSLATE);
        return new TNodeTranslate(SystemInfo.getInfo(this.testingInfo), textContentParser, qName, attributesFiltered, value);
    }

}

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

import com.google.common.escape.Escaper;
import com.google.common.xml.XmlEscapers;
import org.springframework.stereotype.Component;

/**
 * Escape the string for xml attributes.
 *
 * @author Jesse on 12/19/2014.
 */
@Component("escapeXmlAttrs")
public class FilterEscapeXmlAttrs implements TextContentFilter {
    private static final Escaper escaper = XmlEscapers.xmlAttributeEscaper();

    @Override
    public String process(TRenderContext context, String rawValue) {
        return escaper.escape(rawValue);
    }
}

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

/**
 * Filters to apply to the result of text replacement when parsing text.  For example the text
 * replacement of:
 *
 * <code>{{text | filter}}</code>
 *
 * Will result in the model value "text" being retrieved from the model and then the filter with the
 * name "filter" will process the value to give the final value.
 *
 * @author Jesse on 12/19/2014.
 */
public interface TextContentFilter {
    /**
     * Process the input (raw) value.
     *
     * @param context  the render context this filter is operating withing
     * @param rawValue the pre-filter value
     * @return the filtered value.
     */
    String process(TRenderContext context, String rawValue);
}

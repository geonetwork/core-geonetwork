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

import org.fao.geonet.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * One of the results the closure of a file handler can return.  It will load a file and replace all
 * ${key} values (where key is in the substitutions map) with the value from the substitutions map.
 * Keys may not contain { or }.
 *
 * @author Jesse on 10/16/2014.
 */
public class FileResult {
    private final TNode template;
    private final Map<String, Object> substitutions;

    public FileResult(TNode template, Map<String, Object> substitutions) {
        this.template = template;
        this.substitutions = substitutions;
    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TRenderContext context = new TRenderContext(out, substitutions);
        try {
            template.render(context);
            return out.toString(Constants.ENCODING);
        } catch (IOException e) {
            throw new TemplateException(e);
        }
    }
}

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

package org.fao.geonet.api.records.formatters.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

import java.util.regex.Pattern;

/**
 * A Handler that is applied when the path of the element matches the path pattern of this handler.
 *
 * The path is separated by > characters
 *
 * @author Jesse on 10/15/2014.
 */
public class HandlerPathSelect extends Handler {
    private final Pattern pathPattern;

    public HandlerPathSelect(Pattern pathPattern, int priority, Closure handler) {
        super(priority, handler);
        this.pathPattern = pathPattern;
    }

    @Override
    public boolean select(TransformationContext context, GPathResult element) {
        StringBuilder path = new StringBuilder(context.getRootPath());
        if (path.length() > 0) {
            path.append(">");
        }
        createPath(element, path);
        return this.pathPattern.matcher(path.toString()).matches();
    }

    @Override
    protected String handlerExtraToString() {
        return ", pathPattern ~= /" + pathPattern + "/";
    }

}

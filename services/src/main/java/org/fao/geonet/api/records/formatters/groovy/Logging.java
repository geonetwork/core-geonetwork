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

import groovy.util.slurpersupport.GPathResult;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.util.Collection;
import java.util.Iterator;

/**
 * Methods for logging the handling of elements in this formatter.
 *
 * @author Jesse on 10/24/2014.
 */
public class Logging {

    static void debug(String message, GPathResult elem, Object... objects) {
        if (isDebugMode()) {
            Object[] objs = new Object[objects.length + 1];
            System.arraycopy(objects, 0, objs, 1, objects.length);
            objs[0] = "'" + createPath(elem) + "'";
            debug(String.format(message, objs));
        }
    }

    static void debug(String message, Collection<GPathResult> elem, Object... objects) {
        if (isDebugMode()) {
            Object[] objs = new Object[objects.length + 1];
            System.arraycopy(objects, 0, objs, 1, objects.length);
            StringBuilder builder = new StringBuilder();
            if (elem.isEmpty()) {
                builder.append("No Paths");
            } else {
                final Iterator<GPathResult> iterator = elem.iterator();
                GPathResult first = iterator.next();
                builder.append(createPath(first));
                while (iterator.hasNext()) {
                    GPathResult next = iterator.next();
                    builder.append(",").append(next.name());
                }
            }
            objs[0] = "'" + builder + "'";
            debug(String.format(message, objs));
        }
    }

    public static boolean isDebugMode() {
        return Log.isDebugEnabled(Geonet.FORMATTER);
    }

    public static String createPath(GPathResult element) {
        if (element == null) {
            return "null";
        }
        StringBuilder pathBuilder = new StringBuilder();
        Handler.createPath(element, pathBuilder);
        String rootPath = "";
        TransformationContext context = TransformationContext.getContext();
        if (context != null) {
            rootPath = context.getRootPath() + ">";
        }
        return rootPath + pathBuilder.toString();
    }

    static void debug(String message) {
        if (isDebugMode()) {
            Log.debug(Geonet.FORMATTER, message);
        }
    }
}

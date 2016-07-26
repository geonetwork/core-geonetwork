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

import java.io.IOException;
import java.util.List;

/**
 * Represents a Handler for handling Xml elements and returning the resulting data.
 *
 * @author Jesse on 10/15/2014.
 */
public abstract class Handler extends Selectable implements Comparable<Handler> {
    private Closure handlerFunction;
    private boolean group = false;

    public Handler(int priority, Closure handlerFunction) {
        super(priority);
        this.handlerFunction = handlerFunction;
    }

    /**
     * Process the result of the handler function.
     */
    public static void processResult(Object result, StringBuilder resultantXml) throws IOException {
        if (result == null) {
            return;
        }

        resultantXml.append(result);
    }

    protected static void createPath(GPathResult element, StringBuilder path) {
        createPath(element, path, 0);
    }

    protected static void createPath(GPathResult element, StringBuilder path, int depth) {
        if (element != null) {
            if (depth > 10) {
                path.append("... >");
            } else {
                if (element.parent() != element) {
                    createPath(element.parent(), path, depth + 1);
                    path.append(">");
                }
            }
            path.append(element.name());
        }
    }

    @Override
    public int compareTo(Handler o) {
        return Integer.compare(o.priority, this.priority);
    }

    public HandlerResult handle(TransformationContext context, List<GPathResult> elem, StringBuilder resultantXml)
        throws IOException {
        Logging.debug("Executing handler '%2$s' on element %1$s.", elem, this);
        Object elParam = null;
        if (group) {
            elParam = elem;
        } else if (!elem.isEmpty()) {
            elParam = elem.get(0);
        }

        final int maximumNumberOfParameters = this.handlerFunction.getMaximumNumberOfParameters();
        Object result;
        switch (maximumNumberOfParameters) {
            case 0:
                result = this.handlerFunction.call();
                break;
            case 1:
                result = this.handlerFunction.call(elParam);
                break;
            case 2:
                result = this.handlerFunction.call(elParam, context);
                break;
            default:
                throw new IllegalStateException("Too many arguments in handler '" + this + "' there are: " +
                    maximumNumberOfParameters);
        }

        processResult(result, resultantXml);
        return new HandlerResult();
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    @Override
    public final String extraToString() {
        return handlerExtraToString();
    }

    protected abstract String handlerExtraToString();
}

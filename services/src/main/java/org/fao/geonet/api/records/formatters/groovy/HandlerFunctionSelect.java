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

/**
 * @author Jesse on 10/16/2014.
 */
public class HandlerFunctionSelect extends Handler {
    private final Closure selectFunction;

    public HandlerFunctionSelect(Closure select, int priority, Closure handlerFunction) {
        super(priority, handlerFunction);
        this.selectFunction = select;
    }

    @Override
    public boolean select(TransformationContext rootPath, GPathResult elem) {
        final int maximumNumberOfParameters = this.selectFunction.getMaximumNumberOfParameters();
        boolean matches;
        switch (maximumNumberOfParameters) {
            case 0:
                matches = (boolean) this.selectFunction.call();
                break;
            case 1:
                matches = (boolean) this.selectFunction.call(elem);
                break;
            case 2:
                StringBuilder path = new StringBuilder();
                createPath(elem, path);
                matches = (boolean) this.selectFunction.call(elem, path.toString());
                break;
            default:
                throw new IllegalStateException("Too many arguments in handler '" + this + "' there are: " + maximumNumberOfParameters);
        }

        return matches;
    }

    @Override
    protected String handlerExtraToString() {
        return "";
    }

}

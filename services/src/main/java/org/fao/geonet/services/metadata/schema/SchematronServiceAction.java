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

package org.fao.geonet.services.metadata.schema;

import jeeves.server.ServiceConfig;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.exceptions.MissingParameterEx;
import org.fao.geonet.services.thesaurus.EditElement;
import org.jdom.Element;

/**
 * Parse the action parameter of one of the schematron services.
 *
 * Created by Jesse on 2/13/14.
 */
public enum SchematronServiceAction {
    LIST, ADD, EXISTS, DELETE, EDIT;

    public static SchematronServiceAction lookup(ServiceConfig params) {
        String action = params.getValue(Params.ACTION);
        for (SchematronServiceAction serviceAction : values()) {
            if (serviceAction.name().equalsIgnoreCase(action)) {
                return serviceAction;
            }
        }

        throw new BadParameterEx(Params.ACTION, action);
    }
}

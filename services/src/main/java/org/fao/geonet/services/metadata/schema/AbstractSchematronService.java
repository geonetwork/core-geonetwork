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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.jdom.Element;

import java.nio.file.Path;

/**
 * Provides the Distpatch API for the different actions available to all the services.
 *
 * Created by Jesse on 2/13/14.
 */
public abstract class AbstractSchematronService implements Service {
    private SchematronServiceAction _action = SchematronServiceAction.LIST;

    @Override
    public void init(Path appPath, ServiceConfig params) throws Exception {
        this._action = SchematronServiceAction.lookup(params);
    }

    @Override
    public final Element exec(Element params, ServiceContext context) throws Exception {
        switch (_action) {
            case ADD:
                return add(params, context);
            case DELETE:
                return delete(params, context);
            case EDIT:
                return edit(params, context);
            case EXISTS:
                Element response = new Element(Jeeves.Elem.RESPONSE);
                return response.setText(Boolean.toString(exists(params, context)));
            default:
                return list(params, context);
        }

    }

    protected abstract Element list(Element params, ServiceContext context) throws Exception;

    protected abstract boolean exists(Element params, ServiceContext context) throws Exception;

    protected abstract Element edit(Element params, ServiceContext context) throws Exception;

    protected abstract Element delete(Element params, ServiceContext context) throws Exception;

    protected abstract Element add(Element params, ServiceContext context) throws Exception;
}

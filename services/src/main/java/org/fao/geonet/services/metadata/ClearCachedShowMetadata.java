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

package org.fao.geonet.services.metadata;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Params;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Clear the context of a cached metadata as placed by Show
 *
 * @author jeichar
 */
public class ClearCachedShowMetadata implements Service {

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        Element info = params.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
        String mdId;
        if (info == null) {
            mdId = Utils.getIdentifierFromParameters(params, context);
        } else {
            mdId = info.getChildText(Params.ID);
        }
        Show.unCache(context.getUserSession(), mdId);
        return new Element("ok");
    }

}

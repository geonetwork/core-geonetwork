//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.metadata;

import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.api.records.editing.AjaxEditUtils;
import org.fao.geonet.constants.Params;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * For editing : removes an attribute from a metadata. Access is restricted Metadata must be in
 * current user session in edit mode.
 */
@Deprecated
public class DeleteAttribute extends NotInReadOnlyModeService {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    /**
     *
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
    public Element serviceSpecificExec(Element params, ServiceContext context)
        throws Exception {
        UserSession session = context.getUserSession();
        String id = Util.getParam(params, Params.ID);
        String ref = Util.getParam(params, Params.REF);

        Element child = new AjaxEditUtils(context).deleteAttributeEmbedded(session, id, ref);

        return child;
    }
}

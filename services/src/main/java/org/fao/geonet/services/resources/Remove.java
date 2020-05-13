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

package org.fao.geonet.services.resources;

import org.fao.geonet.exceptions.ObjectNotFoundEx;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.Update;
import org.fao.geonet.services.resources.handlers.IResourceRemoveHandler;
import org.jdom.Element;

import java.nio.file.Path;

/**
 * Deletes an uploaded file from the database when a record is in editing mode.
 */
@Deprecated
public class Remove extends NotInReadOnlyModeService {
    private Update update = new Update();

    //-----------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //-----------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        update.init(appPath, params);
    }

    //-----------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //-----------------------------------------------------------------------------

    public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dataMan = gc.getBean(DataManager.class);

        String id = Utils.getIdentifierFromParameters(params, context);
        String ref = Util.getParam(params, Params.REF);
        String access = Util.getParam(params, Params.ACCESS);

        Lib.resource.checkEditPrivilege(context, id);

        // get online resource name
        boolean forEditing = true, withValidationErrors = false, keepXlinkAttributes = false;
        Element metadata = dataMan.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

        Element elem = dataMan.getElementByRef(metadata, ref);

        if (elem == null)
            throw new ObjectNotFoundEx("element with ref='" + ref + "'");

        String fname = elem.getText();

        // Remove the file and update the file upload/downloads tables
        IResourceRemoveHandler removeHook =
            context.getBean("resourceRemoveHandler",IResourceRemoveHandler.class);
        removeHook.onDelete(context, params, Integer.parseInt(id), fname, access);

        // update the metadata
        params.addContent(new Element("_" + ref));
        Element version = params.getChild("version");
        version.setText((Integer.parseInt(version.getText()) + 1) + "");
        return update.exec(params, context);
    }
}

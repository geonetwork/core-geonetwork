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

import java.nio.file.Path;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.resources.handlers.IResourceUploadHandler;
import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

//=============================================================================

/**
 * Handles the file upload when a record is in editing mode.
 */
@Deprecated
public class Upload implements Service {

    // ----------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // ----------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // ----------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // ----------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context)
        throws Exception {

        String id = Utils.getIdentifierFromParameters(params, context);
        String ref = Util.getParam(params, Params.REF);

        Lib.resource.checkEditPrivilege(context, id);

        // get info to log the upload

        UserSession session = context.getUserSession();
        String username = session.getUsername();
        if (username == null)
            username = "unknown (this shouldn't happen?)";


        final AbstractMetadata metadata = context.getBean(IMetadataUtils.class).findOne(id);

        String mdUuid = metadata.getUuid();

        // Jeeves will place the uploaded file name in the f_{ref} element
        // we do it this way because Jeeves will sanitize the name to remove
        // characters that may cause problems
        Element fnameElem = params.getChild("f_" + ref);
        String fname = fnameElem.getText();
        String fsize = fnameElem.getAttributeValue("size");
        if (fsize == null)
            fsize = "0";


        IResourceUploadHandler uploadHook = (IResourceUploadHandler) context.getApplicationContext().getBean("resourceUploadHandler");
        uploadHook.onUpload(context, params, Integer.parseInt(id), fname, Double.parseDouble(fsize));

        context.info("UPLOADED:" + fname + "," + id + "," + mdUuid + ","
            + context.getIpAddress() + "," + username);

        // update the metadata
        Element elem = new Element("_" + ref);
        params.addContent(elem);
        elem.setText(fname);
        return new Element("response").addContent(
            new Element("fname").setText(fname)).addContent(
            new Element("fsize").setText(fsize));
    }
}

// =============================================================================


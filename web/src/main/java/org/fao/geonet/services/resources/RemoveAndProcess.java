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

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import jeeves.constants.Jeeves;
import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.XslProcessing;
import org.jdom.Element;

/**
 * Delete an uploaded file from the data directory and remote its
 * reference in the metadata record.
 */
public class RemoveAndProcess extends NotInReadOnlyModeService {
    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    public Element serviceSpecificExec(Element params, ServiceContext context)
            throws Exception {
        String id = Utils.getIdentifierFromParameters(params, context);
        String url = Util.getParam(params, Params.URL);
        
        Lib.resource.checkEditPrivilege(context, id);
        
        // Analyze the URL to extract file name and private/public folder
        URL resourceURL = new URL(url);
        String[] parameters = resourceURL.getQuery().split("&");
        String filename = "";
        String access = "";
        
        for (String param : parameters) {
            if (param.startsWith("fname")) {
                filename = param.split("=")[1];
            } else if (param.startsWith("access")) {
                access = param.split("=")[1];
            }
        }

        if ("".equals(filename))
            throw new OperationAbortedEx("Empty filename. Unable to delete resource.");

        // delete the file
        File dir = new File(Lib.resource.getDir(context, access, id));
        File file = new File(dir, filename);

        if (file.exists() && !file.delete())
            throw new OperationAbortedEx("unable to delete resource");

        // Set parameter and process metadata to remove reference to the uploaded file
        params.addContent(new Element("name").setText(filename));
        params.addContent(new Element("protocol")
                .setText("WWW:DOWNLOAD-1.0-http--download"));

        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dataMan = gc.getDataManager();

        Set<Integer> metadata = new HashSet<Integer>();
        Set<Integer> notFound = new HashSet<Integer>();
        Set<Integer> notOwner = new HashSet<Integer>();
        Set<Integer> notProcessFound = new HashSet<Integer>();

        Element processedMetadata;
        try {
            processedMetadata = XslProcessing.process(id, "onlinesrc-remove",
                    true, context.getAppPath(), params, context, metadata,
                    notFound, notOwner, notProcessFound, true,
                    dataMan.getSiteURL(context));
            if (processedMetadata == null) {
                throw new BadParameterEx("Processing failed", "Not found:"
                        + notFound.size() + ", Not owner:" + notOwner.size()
                        + ", No process found:" + notProcessFound.size() + ".");
            }
        } catch (Exception e) {
            throw e;
        }

        Element response = new Element(Jeeves.Elem.RESPONSE)
                .addContent(new Element(Geonet.Elem.ID).setText(id));
        return response;
    }
}
//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
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

import jeeves.constants.Jeeves;
import org.fao.geonet.exceptions.BadParameterEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.XslProcessing;
import org.fao.geonet.services.metadata.XslProcessingReport;
import org.jdom.Element;

/**
 * Handles the file upload and attach the uploaded service to the metadata record
 * using the onlinesrc-add XSL process.
 * 
 * Return a simple JSON response in case of success.
 */
public class UploadAndProcess implements Service {
    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
            throws Exception {
        String uploadDir = context.getUploadDir();

        String id = Utils.getIdentifierFromParameters(params, context);
        String filename = Util.getParam(params, Params.FILENAME);
        String access = Util.getParam(params, Params.ACCESS, "private");
        String overwrite = Util.getParam(params, Params.OVERWRITE, "no");
        String description = Util.getParam(params, Params.TITLE, "");

        Lib.resource.checkEditPrivilege(context, id);

        // get info to log the upload

        UserSession session = context.getUserSession();
        String username = session.getUsername();
        if (username == null)
            username = "unknown (this shouldn't happen?)";

        Element fnameElem = params.getChild("filename");
        String fname = fnameElem.getText();
        
        File dir = new File(Lib.resource.getDir(context, access, id));
        Upload.moveFile(context, uploadDir, fname, dir, overwrite);

        context.info("UPLOADED:" + fname + "," + id + ","
                + context.getIpAddress() + "," + username);

        // Set parameter and process metadata to reference the uploaded file
        params.addContent(new Element("url").setText(filename));
        params.addContent(new Element("name").setText(filename));
        params.addContent(new Element("desc").setText(description));
        params.addContent(new Element("protocol")
                .setText("WWW:DOWNLOAD-1.0-http--download"));

        String process = "onlinesrc-add";
        XslProcessingReport report = new XslProcessingReport(process);
        
        Element processedMetadata;
        try {
            final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
            processedMetadata = XslProcessing.process(id, process,
                    true, context.getAppPath(), params, context, report, true, siteURL);
            if (processedMetadata == null) {
                throw new BadParameterEx("Processing failed", "Not found:"
                        + report.getNotFoundMetadataCount() + ", Not owner:" + report.getNotEditableMetadataCount()
                        + ", No process found:" + report.getNoProcessFoundCount() + ".");
            }
        } catch (Exception e) {
            throw e;
        }
        // -- return the processed metadata id
        Element response = new Element(Jeeves.Elem.RESPONSE)
                .addContent(new Element(Geonet.Elem.ID).setText(id));

        return response;
    }
}
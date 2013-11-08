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

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.utils.IO;
import org.fao.geonet.Util;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import java.io.File;
import java.util.List;

//=============================================================================

/**
 * Handles the file upload when a record is in editing mode.
 */

public class Upload implements Service {

    // ----------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // ----------------------------------------------------------------------------

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    // ----------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // ----------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context)
            throws Exception {
        String uploadDir = context.getUploadDir();

        String id = Utils.getIdentifierFromParameters(params, context);
        String ref = Util.getParam(params, Params.REF);
        String access = Util.getParam(params, Params.ACCESS);
        String overwrite = Util.getParam(params, Params.OVERWRITE, "no");

        Lib.resource.checkEditPrivilege(context, id);

        // get info to log the upload

        UserSession session = context.getUserSession();
        String username = session.getUsername();
        if (username == null)
            username = "unknown (this shouldn't happen?)";


        final Metadata metadata = context.getBean(MetadataRepository.class).findOne(id);

        String mdUuid =  metadata.getUuid();

        // Jeeves will place the uploaded file name in the f_{ref} element
        // we do it this way because Jeeves will sanitize the name to remove
        // characters that may cause problems
        Element fnameElem = params.getChild("f_" + ref);
        String fname = fnameElem.getText();
        String fsize = fnameElem.getAttributeValue("size");
        if (fsize == null)
            fsize = "0";

        File dir = new File(Lib.resource.getDir(context, access, id));
        moveFile(context, uploadDir, fname, dir, overwrite);

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

    protected static void moveFile(ServiceContext context, String sourceDir,
            String filename, File targetDir, String overwrite) throws Exception {
        // move uploaded file to destination directory
        // note: uploadDir and rootDir must be in the same volume
        IO.mkdirs(targetDir, "directory to move a file to");

        // get ready to move uploaded file to destination directory
        File oldFile = new File(sourceDir, filename);
        File newFile = new File(targetDir, filename);

        context.info("Source : " + oldFile.getAbsolutePath());
        context.info("Destin : " + newFile.getAbsolutePath());

        if (!oldFile.exists()) {
            throw new Exception("File upload unsuccessful "
                    + oldFile.getAbsolutePath() + " does not exist");
        }

        // check if file already exists and do whatever overwrite wants
        if (newFile.exists() && overwrite.equals("no")) {
            throw new Exception("File upload unsuccessful because "
                    + newFile.getName()
                    + " already exists and overwrite was not permitted");
        }

        // move uploaded file to destination directory - have two goes
        try {
            FileUtils.moveFile(oldFile, newFile);
        } catch (Exception e) {
            context.warning("Cannot move uploaded file");
            context.warning(" (C) Source : " + oldFile.getAbsolutePath());
            context.warning(" (C) Destin : " + newFile.getAbsolutePath());
            IO.delete(oldFile, false, context);
            throw new Exception(
                    "Unable to move uploaded file to destination directory");
        }
    }
}

// =============================================================================


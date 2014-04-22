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

package org.fao.geonet.services.resources.handlers;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.io.File;


/**
 * Class that implements OMNR upload custom behavior.
 *
 *   - Stores upload information for reporting.
 *
 *  @author josegar74
 */
public class DefaultResourceUploadHandler implements IResourceUploadHandler {

    public void onUpload(ServiceContext context, Element params,
                         int metadataId, String fileName, double fileSize) throws ResourceHandlerException {
        
        try {
            String uploadDir = context.getUploadDir();
            String access = Util.getParam(params, Params.ACCESS, "private");
            String overwrite = Util.getParam(params, Params.OVERWRITE, "no");

            File dir = new File(Lib.resource.getDir(context, access, metadataId));
            moveFile(context, uploadDir, fileName, dir, overwrite);

            storeFileUploadRequest(context, metadataId, fileName, fileSize);

        } catch (Exception ex) {
            Log.error(Geonet.RESOURCES, "DefaultResourceUploadHandler (onUpload): " + ex.getMessage());
            ex.printStackTrace();
            throw new ResourceHandlerException(ex);
        }
    }

    /**
     * Stores a file upload request in the MetadataFileUploads table.
     *
     * @param context
     * @param metadataId
     * @param fileName
     */
    private void storeFileUploadRequest(ServiceContext context, int metadataId, String fileName, double fileSize) {
        MetadataFileUploadRepository repo = context.getBean(MetadataFileUploadRepository.class);

        MetadataFileUpload metadataFileUpload = new MetadataFileUpload();

        metadataFileUpload.setMetadataId(metadataId);
        metadataFileUpload.setFileName(fileName);
        metadataFileUpload.setFileSize(fileSize);
        metadataFileUpload.setUploadDate(new ISODate().toString());
        metadataFileUpload.setUserName(context.getUserSession().getUsername());

        repo.save(metadataFileUpload);
    }


    private static void moveFile(ServiceContext context, String sourceDir,
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

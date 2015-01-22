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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Class that implements OMNR upload custom behavior.
 *
 *   - Stores upload information for reporting.
 *
 *  @author josegar74
 */
public class DefaultResourceUploadHandler implements IResourceUploadHandler {

	@Override
    public void onUpload(ServiceContext context, Element params,
                         int metadataId, String fileName, double fileSize) throws ResourceHandlerException {
        
        try {
            Path uploadDir = context.getUploadDir();
            String access = Util.getParam(params, Params.ACCESS, "private");
            String overwrite = Util.getParam(params, Params.OVERWRITE, "no");

            Path dir = Lib.resource.getDir(context, access, metadataId);
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


    @Override
    public void onUpload(InputStream is, ServiceContext context, String access, String overwrite,
            int metadataId, String fileName, double fileSize) throws ResourceHandlerException {

		try {
			Path dir = Lib.resource.getDir(context, access, metadataId);
			moveFile(is, fileName, dir, overwrite);
			
			storeFileUploadRequest(context, metadataId, fileName, fileSize);
		
		} catch (Exception ex) {
			Log.error(Geonet.RESOURCES, "DefaultResourceUploadHandler (onUpload): " + ex.getMessage());
			ex.printStackTrace();
			throw new ResourceHandlerException(ex);
		}
	}
    
	private static void moveFile(InputStream is, String filename,
			Path targetDir, String overwrite) throws Exception {
		File f = new File(targetDir.toFile(), filename);
		
		if(!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
		}
		
		// check if file already exists and do whatever overwrite wants
		if (Files.exists(f.toPath())) {
			if (overwrite.equals("no")) {
				throw new Exception("File upload unsuccessful because "
						+ f.getAbsolutePath()
						+ " already exists and overwrite was not permitted");
			} else {
				f.delete();
			}
		}
		
		f.createNewFile();
		
		OutputStream outputStream = null;

		try {
			// write the inputStream to a FileOutputStream
			outputStream = new FileOutputStream(f);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = is.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

		} catch (IOException e) {
			throw new Exception("Unable to read uploaded file.", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if (outputStream != null) {
				try {
					outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					throw new Exception("Problem writing the uploaded file on destination.", e);
				}

			}
		}
	}

	private static void moveFile(ServiceContext context, Path sourceDir,
			String filename, Path targetDir, String overwrite) throws Exception {
		// move uploaded file to destination directory
		// note: uploadDir and rootDir must be in the same volume
		Files.createDirectories(targetDir);

		// get ready to move uploaded file to destination directory
		Path oldFile = sourceDir.resolve(filename);
		Path newFile = targetDir.resolve(filename);

		context.info("Source : " + oldFile.toAbsolutePath().normalize());
		context.info("Destin : " + newFile.toAbsolutePath().normalize());

		if (!Files.exists(oldFile)) {
			throw new Exception("File upload unsuccessful " + oldFile
					+ " does not exist");
		}

		// check if file already exists and do whatever overwrite wants
		if (Files.exists(newFile) && overwrite.equals("no")) {
			throw new Exception("File upload unsuccessful because "
					+ newFile.getFileName()
					+ " already exists and overwrite was not permitted");
		}

		// move uploaded file to destination directory - have two goes
		try {
			IO.moveDirectoryOrFile(oldFile, newFile, false);
		} catch (Exception e) {
			context.warning("Cannot move uploaded file");
			context.warning(" (C) Source : " + oldFile);
			context.warning(" (C) Destin : " + newFile);
			IO.deleteFile(oldFile, false, context);
			throw new Exception(
					"Unable to move uploaded file to destination directory");
		}
	}

}

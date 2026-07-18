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
import org.fao.geonet.Util;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.core.io.PathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Class that implements OMNR upload custom behavior.
 * <p>
 * - Stores upload information for reporting.
 *
 * @author josegar74
 */
public class DefaultResourceUploadHandler implements IResourceUploadHandler {

    private static void moveFile(final ServiceContext context, final int metadataId, final String fileName, final InputStream is,
                                 final String access, final String overwrite) throws Exception {
        final Store store = context.getBean("resourceStore", Store.class);
        final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        final String uuid = metadataUtils.getMetadataUuid(Integer.toString(metadataId));
        MetadataResourceVisibility visibility = MetadataResourceVisibility.parse(access);
        if (overwrite.equals("no") && store.getResourceDescription(context, uuid, visibility, fileName, true) != null) {
            throw new Exception("File upload unsuccessful because "
                + fileName + " already exists and overwrite was not permitted");
        }
        store.putResource(context, uuid, fileName, is, null, visibility, true);
    }


    private static void moveFile(final ServiceContext context, final int metadataId, final String fileName, final Path uploadDir,
                                 final String access, final String overwrite) throws Exception {
        final Store store = context.getBean("resourceStore", Store.class);
        final IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        final String uuid = metadataUtils.getMetadataUuid(Integer.toString(metadataId));
        Path source = uploadDir.resolve(fileName);
        try {
            MetadataResourceVisibility visibility = MetadataResourceVisibility.parse(access);
            if (overwrite.equals("no") && store.getResourceDescription(context, uuid, visibility, fileName, true) != null) {
                throw new Exception("File upload unsuccessful because "
                    + fileName + " already exists and overwrite was not permitted");
            }
            store.putResource(context, uuid, new PathResource(source), visibility);
        } finally {
            Files.delete(source);
        }
    }

    @Override
    public void onUpload(ServiceContext context, Element params,
                         int metadataId, String fileName, double fileSize) throws ResourceHandlerException {

        try {
            Path uploadDir = context.getUploadDir();
            String access = Util.getParam(params, Params.ACCESS, "private");
            String overwrite = Util.getParam(params, Params.OVERWRITE, "no");

            moveFile(context, metadataId, fileName, uploadDir, access, overwrite);

            storeFileUploadRequest(context, metadataId, fileName, fileSize);

        } catch (Exception ex) {
            Log.error(Geonet.RESOURCES, "DefaultResourceUploadHandler (onUpload): " + ex.getMessage(), ex);
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
            moveFile(context, metadataId, fileName, is, access, overwrite);

            storeFileUploadRequest(context, metadataId, fileName, fileSize);

        } catch (Exception ex) {
            Log.error(Geonet.RESOURCES, "DefaultResourceUploadHandler (onUpload): " + ex.getMessage(), ex);
            throw new ResourceHandlerException(ex);
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
    }

}

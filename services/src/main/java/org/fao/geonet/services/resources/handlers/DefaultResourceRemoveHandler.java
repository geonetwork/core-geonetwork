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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataFileUpload;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataFileUploadRepository;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.http.HttpServletRequest;


/**
 * Class that implements resource upload custom behavior.
 *
 * - Deletes the resource. - Stores delete resource information for reporting.
 *
 * @author josegar74
 */
public class DefaultResourceRemoveHandler implements IResourceRemoveHandler {

    public void onDelete(ServiceContext context, Element params, int metadataId,
                         String fileName, String access) throws ResourceHandlerException {

        try {
            FilePathChecker.verify(fileName);

            // delete online resource
            Path dir = Lib.resource.getDir(context, access, metadataId);
            Path file = dir.resolve(fileName);

            Files.deleteIfExists(file);

            storeFileUploadDeleteRequest(context, metadataId, fileName);

        } catch (Exception ex) {
            Log.error(Geonet.RESOURCES, "DefaultResourceRemoveHandler (onDelete): " + ex.getMessage());
            ex.printStackTrace();
            throw new ResourceHandlerException(ex);
        }
    }

    /**
     * Stores a file upload delete request in the MetadataFileUploads table.
     */
    private void storeFileUploadDeleteRequest(ServiceContext context, int metadataId, String fileName) {
        MetadataFileUploadRepository repo = context.getBean(MetadataFileUploadRepository.class);

        try {
            MetadataFileUpload metadataFileUpload = repo.findByMetadataIdAndFileNameNotDeleted(metadataId, fileName);
            metadataFileUpload.setDeletedDate(new ISODate().toString());

            repo.save(metadataFileUpload);

        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            Log.warning(Geonet.RESOURCES, "Delete file upload request: No upload request for (metadataid, file): (" + metadataId + "," + fileName + ")");
        }
    }

    @Override
    public void onDelete(ServiceContext context, HttpServletRequest request,
                         int metadataId, String fileName, String access)
        throws ResourceHandlerException {

        try {
            FilePathChecker.verify(fileName);

            // delete online resource
            Path dir = Lib.resource.getDir(context, access, metadataId);
            Path file = dir.resolve(fileName);

            Files.deleteIfExists(file);

            storeFileUploadDeleteRequest(context, metadataId, fileName);

        } catch (Exception ex) {
            Log.error(Geonet.RESOURCES, "DefaultResourceRemoveHandler (onDelete): " + ex.getMessage());
            ex.printStackTrace();
            throw new ResourceHandlerException(ex);
        }

    }
}

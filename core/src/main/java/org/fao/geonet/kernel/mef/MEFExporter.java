//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.mef;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.Constants;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.api.records.attachments.StoreUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.mef.MEFLib.Format;
import org.fao.geonet.kernel.mef.MEFLib.Version;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.fao.geonet.kernel.mef.MEFConstants.FILE_INFO;
import static org.fao.geonet.kernel.mef.MEFConstants.FILE_METADATA;


/**
 * Export MEF file
 */
class MEFExporter {
    /**
     * Create a metadata folder according to MEF {@link Version} 1 specification and
     * return file path.
     * <p>
     * Template or subtemplate could not be exported in MEF format. Use XML export
     * instead.
     *
     * @param uuid   UUID of the metadata record to export.
     * @param format {@link org.fao.geonet.kernel.mef.MEFLib.Format}
     * @return the path of the generated MEF file.
     */
    public static Path doExport(ServiceContext context, String uuid, Format format, boolean skipUUID,
                                boolean resolveXlink, boolean removeXlinkAttribute, boolean addSchemaLocation,
                                boolean approved) throws Exception {

        //Search by ID, not by UUID
        final int id;
        //Here we just care if we need the approved version explicitly.
        //IMetadataUtils already filtered draft for non editors.
        if (approved) {
            id = Integer.parseInt(context.getBean(IMetadataUtils.class).getMetadataId(uuid));
        } else {
            id = context.getBean(IMetadataUtils.class).findOneByUuid(uuid).getId();
        }
        Pair<AbstractMetadata, String> recordAndMetadata = MEFLib.retrieveMetadata(context, id, resolveXlink,
                removeXlinkAttribute, addSchemaLocation);
        return export(context, approved, format, skipUUID, recordAndMetadata);
    }

    /**
     * Create a metadata folder according to MEF {@link Version} 1 specification and
     * return file path.
     * <p>
     * Template or subtemplate could not be exported in MEF format. Use XML export
     * instead.
     *
     * @param id     unique ID of the metadata record to export.
     * @param format {@link org.fao.geonet.kernel.mef.MEFLib.Format}
     * @return the path of the generated MEF file.
     */
    public static Path doExport(ServiceContext context, Integer id, Format format, boolean skipUUID,
                                boolean resolveXlink, boolean removeXlinkAttribute, boolean addSchemaLocation) throws Exception {
        Pair<AbstractMetadata, String> recordAndMetadata = MEFLib.retrieveMetadata(context, id, resolveXlink,
            removeXlinkAttribute, addSchemaLocation);
        return export(context, true/* TODO: not sure*/, format, skipUUID, recordAndMetadata);
    }

    private static Path export(ServiceContext context, boolean approved, Format format, boolean skipUUID,
                               Pair<AbstractMetadata, String> recordAndMetadata)
        throws Exception {
        AbstractMetadata record = recordAndMetadata.one();
        String xmlDocumentAsString = recordAndMetadata.two();

        if (record.getDataInfo().getType() == MetadataType.SUB_TEMPLATE
            || record.getDataInfo().getType() == MetadataType.TEMPLATE_OF_SUB_TEMPLATE) {
            throw new Exception("Cannot export sub template");
        }

        Path file = Files.createTempFile("mef-", ".mef");
        final Store store = context.getBean("resourceStore", Store.class);

        try (FileSystem zipFs = ZipUtil.createZipFs(file)) {
            // --- save metadata
            byte[] binData = xmlDocumentAsString.getBytes(Constants.ENCODING);
            Files.write(zipFs.getPath(FILE_METADATA), binData);

            final List<MetadataResource> publicResources = store.getResources(context, record.getUuid(), MetadataResourceVisibility.PUBLIC, null,
                    approved);
            final List<MetadataResource> privateResources = store.getResources(context, record.getUuid(), MetadataResourceVisibility.PRIVATE, null,
                    approved);

            // --- save info file
            binData = MEFLib.buildInfoFile(context, record, format, publicResources, privateResources,
                skipUUID).getBytes(Constants.ENCODING);
            Files.write(zipFs.getPath(FILE_INFO), binData);


            if (format == Format.PARTIAL || format == Format.FULL && !publicResources.isEmpty()) {
                StoreUtils.extract(context, record.getUuid(), publicResources, zipFs.getPath("public"), approved);
            }

            if (format == Format.FULL && !privateResources.isEmpty()) {
                try {
                    Lib.resource.checkPrivilege(context, "" + record.getId(), ReservedOperation.download);
                    StoreUtils.extract(context, record.getUuid(), privateResources, zipFs.getPath("private"), approved);
                } catch (Exception e) {
                    // Current user could not download private data
                    Log.warning(Geonet.MEF,
                        "Error encountered while trying to import private resources of MEF file. MEF ID: " + record.getId(), e);

                }
            }
        } catch (Exception e) {
            FileUtils.deleteQuietly(file.toFile());
            throw e;
        }
        return file;
    }
}

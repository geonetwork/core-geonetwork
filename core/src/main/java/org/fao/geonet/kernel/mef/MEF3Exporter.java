//=============================================================================
//===	Copyright (C) 2001-2022 Food and Agriculture Organization of the
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
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.kernel.mef.MEFLib.Format;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Writes a MEF {@link MEFLib.Version#V3} archive: the same one-folder-per-record container as
 * {@link MEF2Exporter} (inherited {@link #export} is identical), but every resource - public and
 * private alike - is written into a single flat {@code store} directory instead of being split
 * across {@code public}/{@code private}. Visibility is recorded per-file in info.xml's unified
 * {@code <store>} element (see {@link MEFLib#buildInfoFiles}), not by physical location.
 */
class MEF3Exporter extends MEF2Exporter {

    /**
     * Create a MEF3 file in ZIP format.
     *
     * @param uuids  List of records to export.
     * @param format {@link Format} to export.
     * @param includeAttachments If true, include attachments according to the export format and permissions.
     *                        If false, no attachments are included.
     * @return MEF3 File
     */
    public static Path doExport(ServiceContext context, Set<String> uuids,
                                Format format, boolean skipUUID, Path stylePath, boolean resolveXlink,
                                boolean removeXlinkAttribute, boolean skipError, boolean addSchemaLocation, boolean includeAttachments) throws Exception {
        return doExport(context, uuids, format, skipUUID, stylePath, resolveXlink, removeXlinkAttribute, skipError, addSchemaLocation, false, includeAttachments);
    }

    /**
     * Create a MEF3 file in ZIP format.
     *
     * @param uuids  List of records to export.
     * @param format {@link Format} to export.
     * @param includeAttachments If true, include attachments according to the export format and permissions.
     *                        If false, no attachments are included.
     * @return MEF3 File
     */
    public static Path doExport(ServiceContext context, Set<String> uuids,
                                Format format, boolean skipUUID, Path stylePath, boolean resolveXlink,
                                boolean removeXlinkAttribute, boolean skipError, boolean addSchemaLocation,
                                boolean approved, boolean includeAttachments) throws Exception {
        return new MEF3Exporter().export(context, uuids, format, skipUUID, stylePath, resolveXlink,
            removeXlinkAttribute, skipError, addSchemaLocation, approved, includeAttachments);
    }

    @Override
    protected Path getResourcesPath(Path metadataRootDir, MetadataResourceVisibility visibility) throws IOException {
        Path path = metadataRootDir.resolve(MEFConstants.DIR_STORE);
        Files.createDirectories(path);
        return path;
    }

    @Override
    protected boolean isUnifiedStoreLayout() {
        return true;
    }
}

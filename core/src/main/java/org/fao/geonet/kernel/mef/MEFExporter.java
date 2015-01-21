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

package org.fao.geonet.kernel.mef;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.ZipUtil;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.mef.MEFLib.Format;
import org.fao.geonet.kernel.mef.MEFLib.Version;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.fao.geonet.kernel.mef.MEFConstants.FILE_INFO;
import static org.fao.geonet.kernel.mef.MEFConstants.FILE_METADATA;

/**
 * Export MEF file
 * 
 */
class MEFExporter {
	/**
	 * Create a metadata folder according to MEF {@link Version} 1 specification
	 * and return file path.
	 * <p>
	 * Template or subtemplate could not be exported in MEF format. Use XML
	 * export instead.
	 * 
	 * @param context
	 * @param uuid
	 *            UUID of the metadata record to export.
	 * @param format
	 *            {@link org.fao.geonet.kernel.mef.MEFLib.Format}
	 * @param skipUUID
	 * @return the path of the generated MEF file.
	 * @throws Exception
	 */
	public static Path doExport(ServiceContext context, String uuid,
			Format format, boolean skipUUID, boolean resolveXlink, boolean removeXlinkAttribute) throws Exception {
		Pair<Metadata, String> recordAndMetadata =
				MEFLib.retrieveMetadata(context, uuid, resolveXlink, removeXlinkAttribute);
		Metadata record = recordAndMetadata.one();
		String xmlDocumentAsString = recordAndMetadata.two();

		if (record.getDataInfo().getType() == MetadataType.SUB_TEMPLATE) {
			throw new Exception("Cannot export sub template");
        }

		Path file = Files.createTempFile("mef-", ".mef");
		Path pubDir = Lib.resource.getDir(context, "public", record.getId());
		Path priDir = Lib.resource.getDir(context, "private", record.getId());

        try (FileSystem zipFs = ZipUtil.createZipFs(file)) {
            // --- save metadata
            byte[] binData = xmlDocumentAsString.getBytes(Constants.ENCODING);
            Files.write(zipFs.getPath(FILE_METADATA), binData);

            // --- save info file
            binData = MEFLib.buildInfoFile(context, record, format, pubDir, priDir,
                    skipUUID).getBytes(Constants.ENCODING);
            Files.write(zipFs.getPath(FILE_INFO), binData);


            if (format == Format.PARTIAL || format == Format.FULL) {
                if (Files.exists(pubDir) && !IO.isEmptyDir(pubDir)) {
                    IO.copyDirectoryOrFile(pubDir, zipFs.getPath(pubDir.getFileName().toString()), false);
                }
            }

            if (format == Format.FULL) {
                try {
                    Lib.resource.checkPrivilege(context, "" + record.getId(), ReservedOperation.download);
                    if (Files.exists(priDir) && !IO.isEmptyDir(priDir)) {
                        IO.copyDirectoryOrFile(priDir, zipFs.getPath(priDir.getFileName().toString()), false);
                    }

                } catch (Exception e) {
                    // Current user could not download private data
                    Log.warning(Geonet.MEF, "Error encounteres while trying to import private resources of MEF file. MEF UUID: " + uuid, e);

                }
            }
        }
		return file;
	}
}

// =============================================================================


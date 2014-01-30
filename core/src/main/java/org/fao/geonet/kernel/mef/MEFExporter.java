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
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.mef.MEFLib.Format;
import org.fao.geonet.kernel.mef.MEFLib.Version;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.zip.ZipOutputStream;

import static org.fao.geonet.kernel.mef.MEFConstants.DIR_PRIVATE;
import static org.fao.geonet.kernel.mef.MEFConstants.DIR_PUBLIC;
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
	 *            {@link Format}
	 * @param skipUUID
	 * @return the path of the generated MEF file.
	 * @throws Exception
	 */
	public static String doExport(ServiceContext context, String uuid,
			Format format, boolean skipUUID, boolean resolveXlink, boolean removeXlinkAttribute) throws Exception {
		Metadata record = MEFLib.retrieveMetadata(context, uuid, resolveXlink, removeXlinkAttribute);

		if (record.getDataInfo().getType() == MetadataType.SUB_TEMPLATE) {
			throw new Exception("Cannot export sub template");
        }

		File file = File.createTempFile("mef-", ".mef");
		String pubDir = Lib.resource.getDir(context, "public", record.getId());
		String priDir = Lib.resource.getDir(context, "private", record.getId());

		FileOutputStream fos = new FileOutputStream(file);
		ZipOutputStream zos = new ZipOutputStream(fos);

		// --- create folders

		MEFLib.createDir(zos, DIR_PUBLIC);
		MEFLib.createDir(zos, DIR_PRIVATE);

		// --- save metadata

		if (!record.getData().startsWith("<?xml")) {
			record.setData("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n" + record.getData());
        }

		byte[] binData = record.getData().getBytes(Constants.ENCODING);

		MEFLib.addFile(zos, FILE_METADATA, new ByteArrayInputStream(binData));

		// --- save info file

		binData = MEFLib.buildInfoFile(context, record, format, pubDir, priDir,
				skipUUID).getBytes(Constants.ENCODING);

		MEFLib.addFile(zos, FILE_INFO, new ByteArrayInputStream(binData));

		// --- save thumbnails and maps

		if (format == Format.PARTIAL || format == Format.FULL)
			MEFLib.savePublic(zos, pubDir, null);

		if (format == Format.FULL) {
			try {
                Lib.resource.checkPrivilege(context, "" + record.getId(), ReservedOperation.download);
				MEFLib.savePrivate(zos, priDir, null);
			} catch (Exception e) {
				// Current user could not download private data
                Log.warning(Geonet.MEF, "Error encounteres while trying to import private resources of MEF file. MEF UUID: "+uuid, e);
			}
		}
		// --- cleanup and exit

		zos.close();

		return file.getAbsolutePath();
	}
}

// =============================================================================


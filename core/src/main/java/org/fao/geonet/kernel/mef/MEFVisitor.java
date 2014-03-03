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

import org.fao.geonet.exceptions.BadFormatEx;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.fao.geonet.kernel.mef.MEFConstants.DIR_PRIVATE;
import static org.fao.geonet.kernel.mef.MEFConstants.DIR_PUBLIC;
import static org.fao.geonet.kernel.mef.MEFConstants.FILE_INFO;
import static org.fao.geonet.kernel.mef.MEFConstants.FILE_METADATA;

/**
 * MEF version 1 visitor to process and load MEF files.
 */
public class MEFVisitor implements IVisitor {
	public void visit(File mefFile, IMEFVisitor v) throws Exception {
		Element info = handleXml(mefFile, v);
		handleBin(mefFile, v, info, 0);
	}

	// --------------------------------------------------------------------------
	/**
	 * Read the input MEF file and check structure for metadata.xml and info.xml
	 * files.
	 */
	public Element handleXml(File mefFile, IMEFVisitor v) throws Exception {
		ZipInputStream zis = new ZipInputStream(new FileInputStream(mefFile));
		InputStreamBridge isb = new InputStreamBridge(zis);

		ZipEntry entry;

		Element md = null;
		Element info = null;

		try {
			while ((entry = zis.getNextEntry()) != null) {
				String name = entry.getName();

				if (name.equals(FILE_METADATA))
					md = Xml.loadStream(isb);
				else if (name.equals(FILE_INFO))
					info = Xml.loadStream(isb);

				zis.closeEntry();
			}
		} finally {
			safeClose(zis);
		}

		if (md == null)
			throw new BadFormatEx("Missing metadata file : " + FILE_METADATA);

		if (info == null)
			throw new BadFormatEx("Missing info file : " + FILE_INFO);

		v.handleMetadata(md, 0);
		v.handleInfo(info, 0);

		return info;
	}

	/**
	 * Check binary files structure. Binary files are stored in the public or
	 * private folders. All binary files MUST be registered in the information
	 * document (ie. info.xml).
	 */
	public void handleBin(File mefFile, IMEFVisitor v, Element info, int index)
			throws Exception {
		ZipInputStream zis = new ZipInputStream(new FileInputStream(mefFile));
		InputStreamBridge isb = new InputStreamBridge(zis);

		// yes they must be registered but make sure we don't crash if the 
		// public/private elements don't exist
		List<Element> pubFiles;
		if (info.getChild("public") != null) {
			@SuppressWarnings("unchecked")
            List<Element> tmp = info.getChild("public").getChildren();
			pubFiles = tmp;
		} else {
			pubFiles = new ArrayList<Element>();
		}
		List<Element> prvFiles;
		if (info.getChild("private") != null) {
			@SuppressWarnings("unchecked")
            List<Element> tmp = info.getChild("private").getChildren();
			prvFiles = tmp;
		} else {
			prvFiles = new ArrayList<Element>();
		}

		ZipEntry entry;

		try {
			while ((entry = zis.getNextEntry()) != null) {
				String fullName = entry.getName();
				String simpleName = new File(fullName).getName();

				if (fullName.equals(DIR_PUBLIC) || fullName.equals(DIR_PRIVATE))
					continue;

				if (fullName.startsWith(DIR_PUBLIC) || fullName.startsWith("/" + DIR_PUBLIC))
					v.handlePublicFile(simpleName, MEFLib.getChangeDate(
							pubFiles, simpleName), isb, 0);

				else if (fullName.startsWith(DIR_PRIVATE) || fullName.startsWith("/" + DIR_PRIVATE))
					v.handlePrivateFile(simpleName, MEFLib.getChangeDate(
							prvFiles, simpleName), isb, 0);

				zis.closeEntry();
			}
		} finally {
			safeClose(zis);
		}
	}

	private static void safeClose(ZipInputStream zis) {
		try {
			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

// =============================================================================

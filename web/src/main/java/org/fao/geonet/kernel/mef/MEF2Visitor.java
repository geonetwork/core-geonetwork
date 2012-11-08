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

import jeeves.exceptions.BadFormatEx;
import jeeves.interfaces.Logger;
import jeeves.utils.Log;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.util.ZipUtil;
import org.jdom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.zip.ZipFile;

import static org.fao.geonet.kernel.mef.MEFConstants.FILE_INFO;

/**
 * MEF version 2 visitor
 */
public class MEF2Visitor implements IVisitor {

	public void visit(File mefFile, IMEFVisitor v) throws Exception {
		handleXml(mefFile, v);
	}

	/**
	 * Read the input MEF file and for each metadata found, check structure for
	 * metadata.xml, info.xml and optional feature catalogue files.
	 */
	public Element handleXml(File mefFile, IMEFVisitor v) throws Exception {

		Logger log = Log.createLogger(Geonet.MEF);

		int nbMetadata = 0;
		Element fc;

		Element info = new Element("info");

		File unzipDir = new File(mefFile.getParentFile(), "unzipping");

		if (unzipDir.exists())
			ZipUtil.deleteAllFiles(unzipDir);

		ZipUtil.extract(new ZipFile(mefFile), unzipDir);

		// Get the metadata depth
		File metadata = getMetadataDirectory(unzipDir);

		if (metadata.getParentFile().equals(unzipDir)) {
			log
					.debug("Metadata folder is directly under the unzip temporary folder.");
		} else {
			File[] lstmdDir = metadata.getParentFile().getParentFile()
					.listFiles();
            for (File file : lstmdDir) {
                if (file != null && file.isDirectory()) {
                    // Handle metadata file
                    File metadataDir = new File(file, "metadata");

                    File[] xmlFiles = metadataDir.listFiles();

                    if (xmlFiles == null || xmlFiles.length < 1) {
                        throw new BadFormatEx(
                                "Missing XML document in metadata folder " + file.getName() + "/metadata in MEF file "
                                        + mefFile.getName() + ".");
                    }

                    // Handle feature catalog
                    File fcFile = getFeatureCalalogFile(file);
                    if (fcFile != null) {
                        fc = Xml.loadFile(fcFile);
                    }
                    else {
                        fc = null;
                    }

                    // Handle info file
                    File fileInfo = new File(file, FILE_INFO);
                    if (fileInfo.exists()) {
                        info = Xml.loadFile(fileInfo);
                    }

                    v.handleMetadataFiles(xmlFiles, info, nbMetadata);
                    v.handleFeatureCat(fc, nbMetadata);
                    v.handleInfo(info, nbMetadata);

                    // Handle binaries
                    handleBin(file, v, info, nbMetadata);

                    nbMetadata++;
                }
            }
		}

		ZipUtil.deleteAllFiles(unzipDir);

		return info;
	}

	/**
	 * Check binary files to import.
	 */
	public void handleBin(File file, IMEFVisitor v, Element info, int index)
			throws Exception {

		List<Element> pubFiles = null;
		List<Element> prvFiles = null;

		if (info.getChildren().size() != 0) {
			pubFiles = info.getChild("public").getChildren();
			prvFiles = info.getChild("private").getChildren();
		}

		File publicFile = new File(file, MEFConstants.DIR_PUBLIC);
		File privateFile = new File(file, MEFConstants.DIR_PRIVATE);

		String fname;

		// Handle public binaries files
		if (publicFile.exists() && pubFiles != null && pubFiles.size() != 0) {
			File[] files = publicFile.listFiles();
			for (File f : files) {
				fname = f.getName();
				v.handlePublicFile(fname,
						MEFLib.getChangeDate(pubFiles, fname),
						new FileInputStream(f), index);
			}
		}

		// Handle private binaries files
		if (privateFile.exists() && prvFiles != null && prvFiles.size() != 0) {
			File[] files = privateFile.listFiles();
			for (File f : files) {
				fname = f.getName();
				v.handlePrivateFile(fname, MEFLib
						.getChangeDate(prvFiles, fname),
						new FileInputStream(f), index);
			}
		}
	}

	// --------------------------------------------------------------------------

	/**
	 * getFeatureCalalogFile method return feature catalog xml file if exists
	 * 
	 * @param file
	 * @return File
	 */
	private File getFeatureCalalogFile(File file) {
		File tmp = null;
		File fcRepo = new File(file, MEFConstants.SCHEMA);

		if (fcRepo.exists()) {
			File fc = new File(fcRepo, MEFConstants.FILE_METADATA);
			if (fc.exists())
				tmp = fc;
			else {
				File[] files = fcRepo.listFiles();
				// Retrieve first files into applschema directory, without any
				// tests.
				if (files.length != 0)
					tmp = files[0];
			}
		}
		return tmp;
	}

	// --------------------------------------------------------------------------

	public File getMetadataDirectory(File dir) {
		File metadata = new File(dir, "metadata");

		if (!(metadata.exists() && metadata.isDirectory())) {
			File[] list = dir.listFiles();
            for (File file : list) {
                if (file.isDirectory()) {
                    metadata = getMetadataDirectory(file);
                }
            }
		}
		return metadata;
	}
}

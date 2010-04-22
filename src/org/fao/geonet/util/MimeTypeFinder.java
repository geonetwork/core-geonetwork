//==============================================================================
//===
//===   MimeTypeFinder
//===
//==============================================================================
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

package org.fao.geonet.util;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.MimeUtil2;

//==============================================================================

public class MimeTypeFinder
{
	private static MimeUtil mu;
	private static boolean isWindows = System.getProperty("os.name").startsWith("Windows");
	private static String blank = "";

	/*
	 * register mime detectors in order
	 */
	private static void registerMimeDetectors(boolean notLocal) {

		// unregister anything previously registered
		mu.unregisterMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
		mu.unregisterMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
		mu.unregisterMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		mu.unregisterMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");

		// register anything required
		if (isWindows) {
			mu.registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
		}
		if (!notLocal) {
			mu.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
			mu.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		}
		mu.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
	}

	/* 
	 * read file name and get mime type
	 */
	public static String detectMimeTypeFile(String dir, String fName) {

		if (fName != null && dir != null) { 
			if (fName.equals("")) return blank;
			registerMimeDetectors(false);
			try {
				File theFile = new File(dir, fName);
				Collection types = mu.getMimeTypes(theFile);
				MimeType mt = mu.getMostSpecificMimeType(types);
				return mt.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return MimeUtil2.UNKNOWN_MIME_TYPE.toString();
			}
		} else return blank;
	}

	/* 
	 * read URL and get mime type
	 */
	public static String detectMimeTypeUrl(String url) {

		if (url != null) {
			registerMimeDetectors(true);
			try {
				URL theUrl = new URL(url);
				MimeType mt = mu.getMostSpecificMimeType(mu.getMimeTypes(theUrl));
				return mt.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return MimeUtil2.UNKNOWN_MIME_TYPE.toString();
			}
		} else return blank;
	}
}
//==============================================================================





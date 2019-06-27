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

import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import eu.medsea.mimeutil.MimeUtil2;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.io.File;
import java.net.URL;
import java.util.Collection;

//==============================================================================

public class MimeTypeFinder {
    private static boolean isWindows = System.getProperty("os.name").startsWith("Windows");
    private static String blank = "";

    /*
     * register mime detectors in order
     */
    private static void registerMimeDetectors(boolean notLocal) {

        // unregister anything previously registered
        MimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
        MimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
        MimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        MimeUtil.unregisterMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");

        // register anything required
        if (isWindows) {
            MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
        }
        if (!notLocal) {
            MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
            MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        }
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
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
                @SuppressWarnings("unchecked")
                Collection<MimeType> types = MimeUtil.getMimeTypes(theFile);
                boolean specific = false;
                for (MimeType mt : types) {
                    if (mt.getSpecificity() > 1) specific = true;
                }
                if (specific) {
                    return MimeUtil.getMostSpecificMimeType(types).toString();
                } else {
                    return types.iterator().next().toString();
                }
            } catch (Exception e) {
                Log.error(Geonet.GEONETWORK, "Detect file mime type error: " + e.getMessage(), e);
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
                MimeType mt = MimeUtil.getMostSpecificMimeType(MimeUtil.getMimeTypes(theUrl));
                return mt.toString();
            } catch (Exception e) {
                return MimeUtil2.UNKNOWN_MIME_TYPE.toString();
            }
        } else return blank;
    }
}
//==============================================================================





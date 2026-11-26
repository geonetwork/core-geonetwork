/*
 * =============================================================================
 * ===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */
package org.fao.geonet.api.records.attachments;

import org.apache.tika.Tika;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Detects the mime type of a metadata resource, using content sniffing when the file content is
 * available and falling back to filename/extension-based detection otherwise.
 */
public final class MimeTypeDetector {
    private static final Tika TIKA = new Tika();

    private MimeTypeDetector() {
    }

    /**
     * Detect the mime type by reading the file content, with the filename as an additional hint.
     */
    public static String detect(Path path, String filename) {
        try (InputStream is = Files.newInputStream(path)) {
            return TIKA.detect(is, filename);
        } catch (IOException e) {
            Log.warning(Geonet.RESOURCES, String.format(
                "Unable to read '%s' to detect its mime type from content. Falling back to filename-based detection. %s",
                path, e.getMessage()));
            return detect(filename);
        }
    }

    /**
     * Detect the mime type from the filename/extension only, for stores where the content is not
     * locally available (e.g. a remote object store) at the point the mime type is needed.
     */
    public static String detect(String filename) {
        return TIKA.detect(filename);
    }
}

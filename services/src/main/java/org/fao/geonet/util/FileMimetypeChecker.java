//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.util;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Component
public class FileMimetypeChecker {
    private static final String[] validImagesMimeTypes = {"image/gif", "image/png", "image/jpeg"};
    // application/xml --> SDMX files, application/rdf+xml --> RDF, OWL files
    private static final String[] validThesaurusMimeTypes = {"application/xml", "application/rdf+xml"};

    private static final String[] validCsvMimeTypes = {"text/plain"};

    public void checkValidImageMimeType(MultipartFile file) throws IOException {
        checkValidMimeType(file, validImagesMimeTypes);
    }

    public void checkValidThesaurusMimeType(MultipartFile file) throws IOException {
        checkValidMimeType(file, validThesaurusMimeTypes);
    }

    public void checkValidCsvMimeType(MultipartFile file) throws IOException {
        checkValidMimeType(file, validCsvMimeTypes);
    }

    public void checkValidMimeType(MultipartFile file, String[] validMimeTypes) throws IOException {
        Tika tika = new Tika();
        String mimeType = tika.detect(file.getInputStream());

        if (Arrays.stream(validMimeTypes).noneMatch(m -> m.equals(mimeType))) {
            throw new IllegalArgumentException(String.format("File '%s' with type '%s' is not supported. To allow this file type, configure it in System Settings > Allowed file mime types to attach to a metadata record.", file.getOriginalFilename(), mimeType));
        }
    }

}

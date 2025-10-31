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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileMimetypeChecker {
    private static final String[] validImagesMimeTypes = {"image/gif", "image/png", "image/jpeg"};
    // application/xml --> SDMX files, application/rdf+xml --> RDF, OWL files
    private static final String[] validThesaurusMimeTypes = {"application/xml", "application/rdf+xml"};

    private static final String[] validCsvMimeTypes = {"text/plain", "text/csv"};

    public void checkValidImageMimeType(MultipartFile file) throws IOException {
        checkValidMimeType(file, validImagesMimeTypes);
    }

    public void checkValidThesaurusMimeType(MultipartFile file) throws IOException {
        checkValidMimeType(file, validThesaurusMimeTypes);
    }

    public void checkValidCsvMimeType(MultipartFile file) throws IOException {
        checkValidMimeType(file, validCsvMimeTypes);
    }

    /**
     * Validates that the MIME type of an uploaded file is allowed based on a list of valid MIME types.
     * <p>
     * This method first attempts to match the file's detected MIME type exactly against the list of allowed types.
     * If no exact match is found, it then checks for compatibility using wildcard MIME type patterns
     * (e.g., "image/*" matches "image/png").
     * </p>
     *
     * <p>
     * The MIME type is detected using Apache Tika, which infers the content type based on file content
     * and optionally the original filename.
     * </p>
     *
     * <p>
     * If the MIME type is not allowed or cannot be parsed as a valid {@link org.springframework.http.MediaType},
     * an {@link IllegalArgumentException} is thrown with a descriptive message to assist configuration.
     * </p>
     *
     * @param file            the {@link org.springframework.web.multipart.MultipartFile} to validate
     * @param validMimeTypes  an array of MIME type strings that are considered valid (e.g., {"image/png", "image/*"})
     * @throws IOException                if the file's input stream cannot be read
     * @throws IllegalArgumentException   if no valid types are configured, the MIME type is invalid, or the file type is not allowed
     */
    public void checkValidMimeType(MultipartFile file, String[] validMimeTypes) throws IOException {
        // Check if no types are valid
        if (validMimeTypes == null || validMimeTypes.length == 0) {
            throw new IllegalArgumentException("No allowed MIME types are configured. Please configure them in System Settings. To allow this file type, configure it in System Settings > Allowed file mime types to attach to a metadata record.");
        }

        // Detect the mime type
        Tika tika = new Tika();
        String mimeType = tika.detect(file.getInputStream(), file.getOriginalFilename());

        // Check for exact matches
        for (String validMimeType : validMimeTypes) {
            if (validMimeType.equals(mimeType)) {
                return;
            }
        }

        // Convert to MediaType for wildcard and compatibility checking
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(mimeType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format(
                "The uploaded file '%s' has an invalid or unrecognized MIME type ('%s'). "
                    + "Ensure the file is not corrupted and has a valid extension or content type.",
                file.getOriginalFilename(), mimeType
            ), e);
        }

        // Check if the detected MIME type matches any configured wildcard MIME types
        for (String validMimeType : validMimeTypes) {
            MediaType validMediaType;
            try {
                validMediaType = MediaType.parseMediaType(validMimeType);
                if (validMediaType.includes(mediaType)) {
                    return;
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.format(
                    "Invalid configured MIME type '%s' in System Settings. "
                        + "Please correct or remove this value from the allowed MIME types list.",
                    validMimeType
                ), e);
            }
        }

        throw new IllegalArgumentException(String.format("File '%s' with type '%s' is not supported. To allow this file type, configure it in System Settings > Allowed file mime types to attach to a metadata record.", file.getOriginalFilename(), mimeType));
    }

}

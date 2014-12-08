package org.fao.geonet.services.metadata.format.groovy.template;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents the template types supported byt the template parser.
 *
 * @author Jesse on 11/30/2014.
 */
public enum TemplateType {
    XML, TEXT;

    public static TemplateType fromContentType(String type) {
        if (type.contains("xml") || type.contains("html") || type.contains("xsd") || type.contains("xsl")) {
            return XML;
        } else {
            return TEXT;
        }
    }
    public static TemplateType fromPath(Path path) throws IOException {
        return fromContentType(Files.probeContentType(path));
    }
}

package org.fao.geonet.services.metadata.format;

import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;

/**
* @author Jesse on 10/15/2014.
*/
public class SchemaLocalization {
    final String schema;
    final Element strings;
    final Element codelists;
    final Element labels;

    SchemaLocalization(String schema, String schemaLocDir) throws IOException, JDOMException {
        this.schema = schema;
        this.strings = loadLocalizations(schemaLocDir, "strings");
        this.codelists = loadLocalizations(schemaLocDir, "codelists");
        this.labels = loadLocalizations(schemaLocDir, "labels");
    }

    private Element loadLocalizations(String schemaLocDir, String type) throws IOException, JDOMException {
        final File file = new File(schemaLocDir, type + ".xml");
        if (file.exists()) {
            return Xml.loadFile(file).setName(type);
        } else {
            return new Element(type);
        }
    }

}

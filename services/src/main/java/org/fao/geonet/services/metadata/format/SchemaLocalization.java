package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlFile;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.util.Map;

/**
 * Contains all the translation/localization files for a particular schema.
 *
 * @author Jesse on 10/15/2014.
 */
public class SchemaLocalization {
    public final String schema;
    private final Map<String, XmlFile> schemaInfo;
    private final ServiceContext context;


    public SchemaLocalization(ServiceContext context, String schema, Map<String, XmlFile> schemaInfo) {
        this.schema = schema;
        this.schemaInfo = schemaInfo;
        this.context = context;
    }

    public Element getLabels() throws Exception {
        return getXml("labels.xml");
    }

    public Element getCodelists() throws Exception {
        return getXml("codelists.xml");
    }

    public Element getStrings() throws Exception {
        return getXml("strings.xml");
    }

    private Element getXml(String key) throws JDOMException, IOException {
        return schemaInfo.get(key).getXml(context, context.getLanguage(), false);
    }
}

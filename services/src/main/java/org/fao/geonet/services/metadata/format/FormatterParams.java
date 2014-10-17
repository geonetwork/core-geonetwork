package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Params;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author Jesse on 10/15/2014.
 */
public class FormatterParams {
    public Format format;
    public Element params;
    public ServiceContext context;
    public File formatDir;
    public File viewFile;
    public Element metadata;
    public String schema;
    public ConfigFile config;
    public String url;

    public String getResourceUrl() {
        String xslid = Util.getParam(params, "xsl", null);
        String resourceUrl = getLocUrl() + "/metadata.formatter.resource?" + Params.SCHEMA + "=" + schema + "&" +
                             Params.ID + "=" + xslid + "&" + Params.FNAME + "=";

        return resourceUrl;
    }

    public String getLocUrl() {
        return url + "/" + context.getNodeId() + "/" + context.getLanguage() + "/";
    }

    public Map<String, SchemaLocalization> getSchemaLocalizations() throws IOException, JDOMException {
        return this.format.getSchemaLocalizations(this.context);
    }

    public Element getPluginLocResources(String language) throws Exception {
        return this.format.getPluginLocResources(this.context, this.formatDir, language);
    }
    public boolean isDevMode() {
        return this.format.isDevMode(this.context);
    }
}

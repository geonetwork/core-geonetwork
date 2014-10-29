package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
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
    public  Map<String,String[]> params;
    public ServiceContext context;
    public File formatDir;
    public File viewFile;
    public Element metadata;
    public String schema;
    public ConfigFile config;
    public String url;
    public Metadata metadataInfo;
    public FormatType formatType;

    public String param(String paramName, String defaultVal) {
        String[] values = this.params.get(paramName);
        if (values == null) {
            return defaultVal;
        }
        return values[0];
    }
    public String getResourceUrl() {
        String xslid = param("xsl", null);
        String resourceUrl = getLocUrl() + "/md.formatter.resource?" + Params.SCHEMA + "=" + schema + "&" +
                             Params.ID + "=" + xslid + "&" + Params.FNAME + "=";

        return resourceUrl;
    }

    public String getLocUrl() {
        return url;
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

    public FormatterParams copy() {
        FormatterParams formatterParams = new FormatterParams();
        formatterParams.config = this.config;
        formatterParams.params = this.params;
        formatterParams.context = this.context;
        formatterParams.format = this.format;
        formatterParams.schema = this.schema;
        formatterParams.metadata = this.metadata;
        formatterParams.formatDir = this.formatDir;
        formatterParams.formatType = this.formatType;
        formatterParams.url = this.url;
        formatterParams.viewFile = this.viewFile;
        formatterParams.metadataInfo = this.metadataInfo;

        return formatterParams;
    }
}

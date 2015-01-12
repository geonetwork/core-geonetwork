package org.fao.geonet.services.metadata.format;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author Jesse on 10/15/2014.
 */
public class FormatterParams {
    public Format format;
    public Map<String,String[]> params;
    public ServiceContext context;
    public Path formatDir;
    public Path viewFile;
    public Element metadata;
    public String schema;
    public Path schemaDir;
    public ConfigFile config;
    public String url;
    public Metadata metadataInfo;
    public FormatType formatType;
    public boolean formatterInSchemaPlugin;

    public String param(String paramName, String defaultVal) {
        String[] values = this.params.get(paramName);
        if (values == null) {
            return defaultVal;
        }
        return values[0];
    }
    public String getResourceUrl() {
        String xslid = param("xsl", null);
        String schemaParam = "";

        if (formatterInSchemaPlugin) {
            schemaParam = Params.SCHEMA + "=" + schema + "&";
        }

        return getLocUrl() + "/md.formatter.resource?" + schemaParam +
                             Params.ID + "=" + xslid + "&" + Params.FNAME + "=";
    }

    public String getLocUrl() {
        return url;
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
        formatterParams.schemaDir = this.schemaDir;
        formatterParams.formatterInSchemaPlugin = this.formatterInSchemaPlugin;

        return formatterParams;
    }
}

/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.formatters;

import java.nio.file.Path;

import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.jdom.Element;
import org.springframework.web.context.request.WebRequest;

import jeeves.server.context.ServiceContext;

/**
 * @author Jesse on 10/15/2014.
 */
public class FormatterParams {
    public FormatterApi format;
    public WebRequest webRequest;
    public ServiceContext context;
    public Path formatDir;
    public Path viewFile;
    public Element metadata;
    public String schema;
    public Path schemaDir;
    public ConfigFile config;
    public String url;
    public AbstractMetadata metadataInfo;
    public FormatType formatType;
    public boolean formatterInSchemaPlugin;
    public FormatterWidth width;

    public String param(String paramName, String defaultVal) {
        if (webRequest == null) return defaultVal;
        String[] values = webRequest.getParameterMap().get(paramName);
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
        formatterParams.webRequest = this.webRequest;
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

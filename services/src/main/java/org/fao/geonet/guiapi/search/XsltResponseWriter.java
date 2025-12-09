/*
 * =============================================================================
 * ===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.guiapi.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.utils.XML11Char;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to mimic what Jeeves was doing
 */
public class XsltResponseWriter {
    public static final String TRANSLATIONS = "translations";
    Element xml;
    Path xsl;
    Map<String, Object> xslParams = new HashMap<>();

    public XsltResponseWriter(String envTagName, String serviceName) {
        this(envTagName, serviceName, "eng");
    }

    public XsltResponseWriter(String envTagName, String serviceName, String lang) {
        SettingManager settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        String url = settingManager.getBaseURL();
        Element gui = new Element("gui");
        gui.addContent(new Element("url").setText(
            url.substring(0, url.length() - 1)
        ));
        gui.addContent(new Element("nodeUrl").setText(settingManager.getNodeURL()));
        gui.addContent(new Element("baseUrl").setText(settingManager.getBaseURL()));
        gui.addContent(new Element("serverUrl").setText(settingManager.getServerURL()));
        gui.addContent(new Element("nodeId").setText(settingManager.getNodeId()));
        gui.addContent(new Element("language").setText(lang));


        Element settings = settingManager.getAllAsXML(true);
        settings.setName(StringUtils.isNotEmpty(envTagName) ? envTagName : "systemConfig");
        gui.addContent(settings);
        gui.addContent(new Element("reqService").setText(serviceName));

        Element translations = new Element(TRANSLATIONS);

        this.xml = new Element("root")
            .addContent(gui)
            .addContent(translations);
    }

    public XsltResponseWriter withXml(Element xml) {
        this.xml.addContent(xml);
        return this;
    }

    public XsltResponseWriter withXsl(String xsl) {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = applicationContext.getBean(GeonetworkDataDirectory.class);
        this.xsl = dataDirectory.getWebappDir().resolve(xsl);
        return this;
    }

    public XsltResponseWriter withParam(String k, Object v) {
        this.xslParams.put(k, v);
        return this;
    }

    public XsltResponseWriter withParams(Map<String, Object> params) {
        this.xslParams.putAll(params);
        return this;
    }

    public Element asElement() throws Exception {
        return Xml.transform(xml, xsl, xslParams);
    }

    public String asHtml() throws Exception {
        return Xml.getString(asElement());
    }

    public void asPdf(HttpServletResponse response, String fileName) throws Exception {
        GeonetworkDataDirectory dataDirectory = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
        Path file = Xml.transformFOP(dataDirectory.getUploadDir(), xml, xsl.toString());
        try {
            response.setContentType("application/pdf");
            response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
            response.setContentLength((int) file.toFile().length());
            response.getOutputStream().write(Files.readAllBytes(file));
            response.getOutputStream().flush();
        } finally {
            FileUtils.deleteQuietly(file.toFile());
        }

    }

    public XsltResponseWriter withJson(String json) {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        GeonetworkDataDirectory dataDirectory = applicationContext.getBean(GeonetworkDataDirectory.class);
        Path jsonPath = dataDirectory.getWebappDir().resolve(json);
        ObjectMapper mapper = new ObjectMapper();

        try {
            Map<String, String> values = mapper.readValue(jsonPath.toFile(), Map.class);
            Element element = this.xml.getChild(TRANSLATIONS);
            values.forEach((k, v) -> {
                if (XML11Char.isXML11ValidNCName(k)) {
                    element.addContent(new Element(k).setText(v));
                } else {
                    Log.warning(Geonet.GEONETWORK, String.format(
                        "JSON key '%s' can't be used in XSLT API response. Avoid usage of XML invalid characters in keys if this key is required in XSLT processing.",
                        k
                    ));
                }
            });
        } catch (IOException e) {
            Log.warning(Geonet.GEONETWORK, String.format(
                "Can't find JSON file '%s'.", jsonPath
            ));
        }

        return this;
    }

}

/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.services.main;

import jeeves.constants.Jeeves;
import jeeves.guiservices.session.Get;
import jeeves.server.UserSession;
import jeeves.server.dispatchers.ServiceManager;
import jeeves.server.dispatchers.guiservices.XmlCacheManager;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.XslUtil;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Map;

/**
 * HTML pages of the user interface (search, editor, admin console, sign in, ...) and the
 * OpenSearch description, rendered with their XSLT from the information the pages need
 * (settings, user, translations and URLs).
 */
@Controller
public class UiPageController {
    private static final String HTML = "text/html; charset=UTF-8";
    private static final String DEFAULT_LANGUAGE = "eng";
    private static final String ERROR_PAGE = "error";

    private static final Map<String, Page> PAGES = Map.ofEntries(
        Map.entry("catalog.search", new Page("ui-search/search.xsl", true, HTML)),
        Map.entry("catalog.edit", new Page("ui-metadata/editor.xsl", true, HTML)),
        Map.entry("display", new Page("ui-metadata/view/formatter-viewer.xsl", true, HTML)),
        Map.entry("admin.console", new Page("ui-admin/admin.xsl", false, HTML)),
        Map.entry("catalog.signin", new Page("ui-admin/signin.xsl", false, HTML)),
        Map.entry("contact.us", new Page("ui-admin/contactus.xsl", false, HTML)),
        Map.entry("new.account", new Page("ui-admin/new-account.xsl", false, HTML)),
        Map.entry("new.password", new Page("ui-admin/new-password.xsl", false, HTML)),
        Map.entry("node-change-warning", new Page("ui-admin/node-change-warning.xsl", true, HTML)),
        Map.entry("service-not-allowed", new Page("ui/service-not-allowed.xsl", true, HTML)),
        Map.entry(ERROR_PAGE, new Page("ui/error-page.xsl", false, HTML)),
        Map.entry("portal.opensearch", new Page("services/rss/opensearch.xsl", false, "text/xml; charset=UTF-8")));

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ServiceManager serviceManager;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private XmlCacheManager xmlCacheManager;

    @Autowired
    private GeonetworkDataDirectory dataDirectory;

    @Autowired
    private NodeInfo nodeInfo;

    @RequestMapping(value = "/{portal}/{lang:[a-z]{3}}/{page:catalog\\.search|catalog\\.edit|display|admin\\.console"
        + "|catalog\\.signin|contact\\.us|new\\.account|new\\.password|node-change-warning|service-not-allowed"
        + "|error|portal\\.opensearch}",
        method = {RequestMethod.GET, RequestMethod.POST})
    public void page(@PathVariable String lang, @PathVariable String page,
                     HttpServletRequest request, HttpServletResponse response) throws Exception {
        // When the catalogue failed to start, every page shows the startup errors.
        if (serviceManager.isStartupError()) {
            page = ERROR_PAGE;
        }
        Page config = PAGES.get(page);

        Element params = new Element(Jeeves.Elem.REQUEST);
        for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements(); ) {
            String name = names.nextElement();
            if (!name.isEmpty()) {
                for (String value : request.getParameterValues(name)) {
                    params.addContent(new Element(name).setText(value));
                }
            }
        }

        Element gui = new Element(Jeeves.Elem.GUI)
            .addContent(localizedXml("strings", lang))
            .addContent(Get.getSessionAsXML(getUserSession(request)))
            .addContent(getEnv(request));
        if (config.i18n) {
            gui.addContent(localizedXml("i18n", lang));
        }
        if (page.equals(ERROR_PAGE)) {
            gui.addContent(getStartupError());
        }

        String baseUrl = serviceManager.getBaseUrl();
        String nodeId = nodeInfo.getId();
        gui.addContent(new Element(Jeeves.Elem.LANGUAGE).setText(lang))
            .addContent(new Element(Jeeves.Elem.LANGUAGE_2_CHARS).setText(XslUtil.twoCharLangCode(lang)))
            .addContent(new Element(Jeeves.Elem.REQ_SERVICE).setText(page))
            .addContent(new Element(Jeeves.Elem.BASE_URL).setText(baseUrl))
            .addContent(new Element(Jeeves.Elem.LOC_URL).setText(baseUrl + "/loc/" + lang))
            .addContent(new Element(Jeeves.Elem.BASE_SERVICE).setText(baseUrl + "/" + nodeId))
            .addContent(new Element(Jeeves.Elem.NODE_ID).setText(nodeId))
            .addContent(new Element(Jeeves.Elem.LOC_SERVICE).setText(baseUrl + "/" + nodeId + "/" + lang))
            .addContent(new Element("nodeUrl").setText(settingManager.getNodeURL()))
            .addContent(new Element("baseUrl").setText(settingManager.getBaseURL()))
            .addContent(new Element("serverUrl").setText(settingManager.getServerURL()));

        Element root = new Element(Jeeves.Elem.ROOT)
            .addContent(gui)
            .addContent(((Element) params.clone()).setName(Jeeves.Elem.RESPONSE))
            .addContent(params);

        ByteArrayOutputStream html = new ByteArrayOutputStream();
        Xml.transform(root, dataDirectory.getWebappDir().resolve("xslt").resolve(config.stylesheet), html);

        response.setContentType(config.contentType);
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Cache-Control", "no-cache");
        response.addHeader("Expires", "-1");
        html.writeTo(response.getOutputStream());
    }

    private Element localizedXml(String name, String lang) throws Exception {
        return xmlCacheManager.get(applicationContext, true, dataDirectory.getWebappDir().resolve("loc"), "xml/" + name + ".xml",
            lang, DEFAULT_LANGUAGE, true).setName(name);
    }

    private static UserSession getUserSession(HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        return httpSession == null ? null : (UserSession) httpSession.getAttribute(Jeeves.Elem.SESSION);
    }

    /**
     * Public settings of the catalogue, its read-only state and whether micro services are enabled.
     */
    private Element getEnv(HttpServletRequest request) throws Exception {
        Element settings = settingManager.getAllAsXML(true);
        Element system = settings.getChild("system");
        system.addContent(new Element("readonly").setText(Boolean.toString(NodeInfo.isReadOnly())));
        if (settings.getChild("map") != null) {
            system.addContent(settings.getChild("map").detach());
        }
        Object microServicesTargetUri = request.getServletContext().getAttribute("MicroServicesProxy.targetUri");
        system.addContent(new Element("microservicesEnabled")
            .setText(Boolean.toString(StringUtils.isNotBlank((String) microServicesTargetUri))));
        return ((Element) system.detach()).setName("env");
    }

    private Element getStartupError() {
        Element startupError = new Element("startupError");
        if (serviceManager.isStartupError()) {
            Element error = new Element("error");
            serviceManager.getStartupErrors().forEach((key, value) -> error.addContent(new Element(key).setText(value)));
            startupError.addContent(error);
        } else {
            startupError.addContent(new Element("ok"));
        }
        return startupError;
    }

    private static class Page {
        final String stylesheet;
        final boolean i18n;
        final String contentType;

        Page(String stylesheet, boolean i18n, String contentType) {
            this.stylesheet = stylesheet;
            this.i18n = i18n;
            this.contentType = contentType;
        }
    }
}

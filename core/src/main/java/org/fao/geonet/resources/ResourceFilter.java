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

package org.fao.geonet.resources;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.setting.SettingManager;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ConcurrentMap;

/**
 * Servlet for serving up resources located in GeoNetwork data directory. For example, this solves a
 * largely historical issue because logos are hardcoded across the application to be in
 * /images/logos.  However this is often not desirable.  They would be better to be in the data
 * directory and thus possibly outside of geonetwork (allowing easier upgrading of geonetwork
 * etc...)
 *
 * User: jeichar Date: 1/17/12 Time: 4:03 PM
 */
public class ResourceFilter implements Filter {
    private static final int FIVE_DAYS = 60 * 60 * 24 * 5;
    private static final int SIX_HOURS = 60 * 60 * 6;
    private FilterConfig config;
    private ServletContext servletContext;
    private volatile Pair<byte[], Long> defaultImage;
    private ConcurrentMap<String, Pair<byte[], Long>> faviconMap = Maps.newConcurrentMap();

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        this.servletContext = config.getServletContext();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException {
        new Instance(request, response).execute();
    }

    public synchronized void destroy() {
        servletContext = null;
        defaultImage = null;
        faviconMap = null;
    }

    public class Instance {
        private final ServletRequest request;
        private final ServletResponse response;

        private final Path resourcesDir;
        private final Path schemaPublicationDir;
        private final Path appPath;
        private final String nodeId;
        private final String siteId;
        private final Resources resources;
        private Pair<byte[], Long> favicon;

        public Instance(ServletRequest request, ServletResponse response) throws IOException {
            final ConfigurableApplicationContext applicationContext = JeevesDelegatingFilterProxy
                    .getApplicationContextFromServletContext(servletContext);
            this.appPath = applicationContext.getBean(GeonetworkDataDirectory.class).getWebappDir();
            this.resources = applicationContext.getBean(Resources.class);
            this.siteId = applicationContext.getBean(SettingManager.class).getSiteId();
            this.resourcesDir = resources.locateResourcesDir(servletContext, applicationContext);
            this.schemaPublicationDir = applicationContext.getBean(GeonetworkDataDirectory.class).getSchemaPublicationDir();
            if (defaultImage == null) {
                defaultImage = resources.loadResource(resourcesDir, servletContext, appPath, "images/logos/" + siteId + ".png", new byte[0], -1);
            }
            this.nodeId = applicationContext.getBean(NodeInfo.class).getId();
            if (!faviconMap.containsKey(nodeId)) {
                final byte[] defaultImageBytes = defaultImage.one();
                addFavIcon(nodeId, resources.loadResource(resourcesDir, servletContext, appPath, "images/logos/" + siteId + ".ico",
                                                          defaultImageBytes, -1));
            }

            this.favicon = faviconMap.get(nodeId);

            this.request = request;
            this.response = response;
        }

        private boolean isGet(ServletRequest request) {
            return ((HttpServletRequest) request).getMethod().equalsIgnoreCase("GET");
        }

        public void execute() throws IOException {
            if (isGet(request)) {
                String filename =  ((HttpServletRequest) request).getPathInfo();
                int extIdx = filename.lastIndexOf('.');
                String ext;
                if (extIdx > 0) {
                    ext = filename.substring(extIdx + 1);
                } else {
                    ext = "png";
                }
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                FileTime lastModified = resources.getLastModified(resourcesDir, servletContext, appPath, filename);
                if (lastModified != null &&
                    new ServletWebRequest((HttpServletRequest) request, httpServletResponse).checkNotModified(lastModified.toMillis())) {
                    return;
                }

                // Figure out the content type based on the extensions, defaulting to an image
                String contentType = extensionToMediaType(ext);

                httpServletResponse.setContentType(contentType);
                httpServletResponse.addHeader("Cache-Control", "max-age=" + SIX_HOURS + ", public");
                if (filename.equals("images/logos/" + siteId + ".ico")) {
                    favicon = resources.loadResource(resourcesDir, servletContext, appPath, "images/logos/" + siteId + ".ico", favicon.one(),
                                                     favicon.two());
                    addFavIcon(nodeId, favicon);

                    httpServletResponse.setContentLength(favicon.one().length);
                    httpServletResponse.addHeader("Cache-Control", "max-age=" + FIVE_DAYS + ", public");
                    response.getOutputStream().write(favicon.one());
                } else if(filename.startsWith("/xml/schemas/")) {
                    Pair<byte[], Long> loadedResource = resources.loadResource(schemaPublicationDir, servletContext, appPath, filename, null, -1);
                    if(loadedResource.two() == -1) {
                        httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        httpServletResponse.setContentLength(loadedResource.one().length);
                        response.getOutputStream().write(loadedResource.one());
                    }
                } else {
                    byte[] defaultData = null;

                    if (!contentType.equals(MediaType.APPLICATION_XML_VALUE)) {
                        defaultData = defaultImage.one();
                    }
                    Pair<byte[], Long> loadResource = resources.loadResource(resourcesDir, servletContext, appPath, filename,
                        defaultData, -1);
                    if (loadResource.two() == -1) {
                        // Return HTTP 404
                        httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
                    } else {
                        httpServletResponse.setContentLength(loadResource.one().length);
                        response.getOutputStream().write(loadResource.one());
                    }
                }
            }
        }

        private synchronized void addFavIcon(String nodeId, Pair<byte[], Long> favicon) {
            if (faviconMap.containsKey(nodeId)) {
                faviconMap.replace(nodeId, favicon);
            } else {
                faviconMap.putIfAbsent(nodeId, favicon);
            }
        }
    }

    private String extensionToMediaType(String ext) {
        final String contentType;
        if(Sets.newHashSet("xml", "xsd", "sch", "dtd").contains(ext)) {
            contentType = MediaType.APPLICATION_XML_VALUE;
        } else if(ext.equals("txt")) {
            contentType = MediaType.TEXT_PLAIN_VALUE;
        } else {
            contentType = "image/" + ext;
        }
        return contentType;
    }
}

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

import jeeves.config.springutil.JeevesDelegatingFilterProxy;

import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.springframework.context.ConfigurableApplicationContext;
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
        private final Path appPath;
        private final String nodeId;
        private final Resources resources;
        private Pair<byte[], Long> favicon;

        public Instance(ServletRequest request, ServletResponse response) throws IOException {
            final ConfigurableApplicationContext applicationContext = JeevesDelegatingFilterProxy
                    .getApplicationContextFromServletContext(servletContext);
            this.appPath = applicationContext.getBean(GeonetworkDataDirectory.class).getWebappDir();
            this.resources = applicationContext.getBean(Resources.class);
            this.resourcesDir = resources.locateResourcesDir(servletContext, applicationContext);
            if (defaultImage == null) {
                defaultImage = resources.loadResource(resourcesDir, servletContext, appPath, "images/logos/GN3.png", new byte[0], -1);
            }
            this.nodeId = applicationContext.getBean(NodeInfo.class).getId();
            if (!faviconMap.containsKey(nodeId)) {
                final byte[] defaultImageBytes = defaultImage.one();
                AddFavIcon(nodeId, resources.loadResource(resourcesDir, servletContext, appPath, "images/logos/GN3.ico",
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

                // TODO : other type of resources html
                httpServletResponse.setContentType("image/" + ext);
                httpServletResponse.addHeader("Cache-Control", "max-age=" + SIX_HOURS + ", public");
                if (filename.equals("images/logos/GN3.ico")) {
                    favicon = resources.loadResource(resourcesDir, servletContext, appPath, "images/logos/GN3.ico", favicon.one(),
                                                     favicon.two());
                    AddFavIcon(nodeId, favicon);

                    httpServletResponse.setContentLength(favicon.one().length);
                    httpServletResponse.addHeader("Cache-Control", "max-age=" + FIVE_DAYS + ", public");
                    response.getOutputStream().write(favicon.one());
                } else {
                    Pair<byte[], Long> loadResource = resources.loadResource(resourcesDir, servletContext, appPath, filename,
                                                                             defaultImage.one(), -1);
                    if (loadResource.two() == -1) {

                        synchronized (this) {
                            defaultImage = resources.loadResource(resourcesDir,
                                config.getServletContext(), appPath, "images/logos/GN3.ico",
                                defaultImage.one(), defaultImage.two());
                        }

                        // Return HTTP 404 ? TODO
                        Log.warning(Geonet.RESOURCES, "Resource not found " + filename +
                            ", default resource returned.");
                        httpServletResponse.setContentType("image/png");
                        httpServletResponse.setHeader("Cache-Control", "no-cache");
                    }
                    httpServletResponse.setContentLength(loadResource.one().length);
                    response.getOutputStream().write(loadResource.one());
                }
            }
        }

        private synchronized void AddFavIcon(String nodeId, Pair<byte[], Long> favicon) {
            if (faviconMap.containsKey(nodeId)) {
                faviconMap.replace(nodeId, favicon);
            } else {
                faviconMap.putIfAbsent(nodeId, favicon);
            }
        }
    }
}

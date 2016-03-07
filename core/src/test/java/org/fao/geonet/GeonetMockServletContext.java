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

package org.fao.geonet;

import org.fao.geonet.utils.IO;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;

import java.net.URISyntaxException;

import java.nio.file.Path;
import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Jesse on 10/17/2014.
 */
public class GeonetMockServletContext extends MockServletContext {
    private Path resourcePath = IO.toPath("");

    public GeonetMockServletContext() {
        super(new FileSystemResourceLoader());
    }

    public void setTestClass(Class testClass) {
        this.resourcePath = getServletRelativePath(AbstractCoreIntegrationTest.getWebappDir(testClass));
    }

    @Override
    protected String getResourceLocation(String path) {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource != null) {
            try {
                return Paths.get(resource.toURI()).toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        String resourceLocation = super.getResourceLocation(path);
        if (resourceLocation.startsWith("/") || resourceLocation.startsWith("\\")) {
            resourceLocation = resourceLocation.substring(1);
        }
        return this.resourcePath.resolve(resourceLocation).toString();
    }

    private Path getServletRelativePath(Path webappDir) {
        Path servletRelativePath = IO.toPath("").toAbsolutePath();
        return servletRelativePath.relativize(webappDir.toAbsolutePath());
    }

}

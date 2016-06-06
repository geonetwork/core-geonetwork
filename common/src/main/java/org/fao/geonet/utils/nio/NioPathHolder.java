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

package org.fao.geonet.utils.nio;

import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Holder the information required by {@link NioPathAwareCatalogResolver} and {@link
 * NioPathAwareEntityResolver} for resolving paths.
 *
 * @author Jesse on 11/4/2014.
 */
public class NioPathHolder {
    private static final ThreadLocal<Path> ACTUAL_RELATIVE_TO = new InheritableThreadLocal<>();
    private static final ThreadLocal<Path> SYS_ID_RELATIVE_TO = new InheritableThreadLocal<>();

    public static void setBase(Path base) {
        if (base != null) {
            ACTUAL_RELATIVE_TO.set(base.getParent());
            SYS_ID_RELATIVE_TO.set(new File(".").getAbsoluteFile().toPath().getParent());
        } else {
            ACTUAL_RELATIVE_TO.set(null);
            SYS_ID_RELATIVE_TO.set(null);
        }
    }

    static InputSource resolveEntity(String publicId, String systemId) throws IOException {
        Path resource = resolveResource(publicId, systemId);
        if (resource != null) {
            return new PathInputSource(resource);
        }
        return null;
    }

    public static Path resolveResource(String publicId, String systemId) {
        if (ACTUAL_RELATIVE_TO.get() != null) {
            if (systemId.startsWith("file:/") || systemId.startsWith(ACTUAL_RELATIVE_TO.get().toUri().getScheme())) {
                try {
                    Path srcPath = Paths.get(new URI(systemId));
                    final Path relativePath = SYS_ID_RELATIVE_TO.get().relativize(srcPath);
                    Path finalPath = ACTUAL_RELATIVE_TO.get().resolve(relativePath.toString());
                    if (Files.isRegularFile(finalPath)) {
                        return finalPath;
                    }
                } catch (URISyntaxException e) {
                    // failed
                }
            } else {
                try {
                    Path srcPath = ACTUAL_RELATIVE_TO.get().resolve(systemId);
                    if (Files.isRegularFile(srcPath)) {
                        return srcPath;
                    }
                } catch (RuntimeException e) {
                    return null;
                }
            }
        }

        return null;
    }
}

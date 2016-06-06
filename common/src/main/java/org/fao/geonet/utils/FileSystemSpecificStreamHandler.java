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

package org.fao.geonet.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A file handler for urls that are created by {@link org.fao.geonet.utils.IO#toURL(java.net.URI)}
 * and represent a url to a file.
 *
 * @author Jesse on 11/18/2014.
 */
class FileSystemSpecificStreamHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        try {
            final Path path = IO.toPath(new URI(u.toExternalForm().replace('\\', '/')));
            return new URLConnection(u) {

                @Override
                public void connect() throws IOException {
                    // nothing to do
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return IO.newInputStream(path);
                }

                @Override
                public int getContentLength() {
                    try {
                        final long size = Files.size(path);
                        if (size > Integer.MAX_VALUE) {
                            throw new AssertionError(path + " size is too large for the getContentLength method.  " +
                                "It is greater than Integer.MAX_VALUE: " + size);
                        }
                        return (int) size;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public String getContentType() {
                    try {
                        return Files.probeContentType(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public long getLastModified() {
                    try {
                        return Files.getLastModifiedTime(path).toMillis();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    return Files.newOutputStream(path);
                }
            };
        } catch (URISyntaxException e1) {
            throw new Error(e1);
        }
    }
}

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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.*;
import java.util.Collections;

/**
 * Zip or unzip files
 */
public class ZipUtil {
    public static void extract(Path zipFile, Path toDir) throws IOException, URISyntaxException {
        try (FileSystem fs = openZipFs(zipFile)) {
            extract(fs, toDir);
        }
    }

    /**
     * Extracts a zip file to a specified directory.
     *
     * @param zipFile the zip file to extract
     * @param toDir   the target directory
     */
    public static void extract(FileSystem zipFile, Path toDir) throws IOException {
        Files.createDirectories(toDir);

        for (Path root : zipFile.getRootDirectories()) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(root)) {
                for (Path path : paths) {
                    final Path fileName = path.getFileName();
                    if (fileName != null) {
                        IO.copyDirectoryOrFile(path, toDir.resolve(fileName.toString()), false);
                    }
                }
            }
        }
    }

    /**
     * FileSystem must be closed when done.  This method should always be called in a try (resource)
     * {} block
     */
    public static FileSystem openZipFs(Path path) throws IOException, URISyntaxException {
        try {
            URI uri = new URI("jar:" + path.toUri().toString());
            return getOrCreateZipFs(uri);
        } catch (Throwable e) {
            try {
                URI uri = new URI("jar:" + URLDecoder.decode(path.toUri().toString(), Constants.ENCODING));
                return getOrCreateZipFs(uri);
            } catch (Throwable e2) {
                URI uri = new URI("jar:" + URLEncoder.encode(path.toUri().toString(), Constants.ENCODING));
                return getOrCreateZipFs(uri);
            }
        }
    }

    private static FileSystem getOrCreateZipFs(URI uri) throws IOException {
        try {
            return FileSystems.getFileSystem(uri);
        } catch (FileSystemNotFoundException e) {
            return FileSystems.newFileSystem(uri, Collections.singletonMap("create", String.valueOf(false)));
        }
    }

    /**
     * Delete path if it exists and create a new zip at the location.
     * <p/>
     * FileSystem must be closed when done.
     * <p/>
     * This method should always be called in a try (resource) {} block
     */
    public static FileSystem createZipFs(Path path) throws IOException, URISyntaxException {
        Files.deleteIfExists(path);

        URI uri = new URI("jar:" + path.toUri());
        return FileSystems.newFileSystem(uri, Collections.singletonMap("create", String.valueOf(true)));
    }
}

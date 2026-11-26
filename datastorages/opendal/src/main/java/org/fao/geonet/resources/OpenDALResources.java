/*
 * =============================================================================
 * ===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.resources;

import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.opendal.Entry;
import org.apache.opendal.Metadata;
import org.apache.opendal.Operator;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.List;

public class OpenDALResources extends Resources {
    @Autowired
    private OpenDALConfiguration opendal;

    @Override
    protected Path getBasePath(final ServiceContext context) {
        return Paths.get("/");
    }

    @Override
    public Path locateResourcesDir(final ServletContext context, final ApplicationContext applicationContext) {
        return Paths.get("/resources");
    }

    @Override
    protected Path locateResourcesDir(final ServiceContext context) {
        return Paths.get("/resources");
    }

    private String getKey(final Path dir, final String name) {
        return getKey(dir.resolve(name));
    }

    private String getKey(final Path path) {
        String pathString;
        if (path.getFileSystem().getSeparator().equals("/")) {
            pathString = path.toString();
        } else {
            pathString = path.toString().replace(path.getFileSystem().getSeparator(), "/");
        }

        // Standardize path: remove leading / if present
        if (pathString.startsWith("/")) {
            pathString = pathString.substring(1);
        }
        return pathString;
    }

    private Path getKeyPath(String key) {
        if (!key.startsWith("/")) {
            return Paths.get("/" + key);
        }
        return Paths.get(key);
    }

    @Nullable
    @Override
    protected Path findImagePath(final String imageName, final Path logosDir) throws IOException {
        final String key = getKey(logosDir, imageName);
        final Operator operator = opendal.getOperator();
        if (imageName.indexOf('.') > -1) {
            try {
                operator.stat(key);
                return getKeyPath(key);
            } catch (Exception e) {
                // ignore
            }
        } else {
            // If no extension, search for files with the name and an image extension
            try {
                List<Entry> entries = operator.list(getKey(logosDir));
                for (Entry entry : entries) {
                    String entryPath = entry.getPath();
                    String name = getFilenameFromPath(entryPath);
                    if (name.startsWith(imageName + ".")) {
                        String ext = FilenameUtils.getExtension(name);
                        if (IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
                            return getKeyPath(entryPath);
                        }
                    }
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return null;
    }

    @Nullable
    @Override
    public ResourceHolder getImage(final ServiceContext context, final String imageName,
                                   final Path logosDir) throws IOException {
        Path path = findImagePath(imageName, logosDir);
        if (path != null) {
            String key = getKey(path);
            return new OpenDALResourceHolder(key, false);
        } else {
            return null;
        }
    }

    @Override
    public ResourceHolder getWritableImage(final ServiceContext context, final String imageName,
                                           final Path logosDir) {
        return new OpenDALResourceHolder(getKey(logosDir, imageName), true);
    }

    @Override
    Pair<byte[], Long> loadResource(final Path resourcesDir, final ServletContext context,
                                    final Path appPath, final String filename, final byte[] defaultValue,
                                    final long loadSince) throws IOException {
        final Path file = locateResource(resourcesDir, context, appPath, filename);
        final String key = getKey(file);
        final Operator operator = opendal.getOperator();
        try {
            Metadata metadata = operator.stat(key);
            final long lastModified = metadata.getLastModified().toEpochMilli();
            if (loadSince < 0 || lastModified > loadSince) {
                byte[] content = operator.read(key);
                return Pair.read(content, lastModified);
            } else {
                return Pair.read(defaultValue, loadSince);
            }
        } catch (Exception e) {
            Log.error(Log.RESOURCES, "Error loading resource: " + key, e);
        }
        return Pair.read(defaultValue, -1L);
    }

    @Override
    protected Path locateResource(@Nullable final Path resourcesDir, final ServletContext context,
                                  final Path appPath, @Nonnull String filename) throws IOException {
        if (filename.charAt(0) == '/' || filename.charAt(0) == '\\') {
            filename = filename.substring(1);
        }

        final String key;
        if (resourcesDir != null) {
            key = getKey(resourcesDir, filename);
        } else {
            key = filename;
        }

        final Operator operator = opendal.getOperator();
        boolean exists = false;
        try {
            operator.stat(key);
            exists = true;
        } catch (Exception e) {
            exists = false;
        }
        if (!exists) {
            Path webappCopy = null;
            if (context != null) {
                final String realPath = context.getRealPath(filename);
                if (realPath != null) {
                    webappCopy = IO.toPath(realPath);
                }
            }

            if (webappCopy == null) {
                webappCopy = appPath.resolve(filename);
            }
            if (!Files.isReadable(webappCopy)) {
                if (resourcesDir != null && resourcesDir.equals(Paths.get("/resources"))) {
                    final ConfigurableApplicationContext applicationContext =
                            JeevesDelegatingFilterProxy.getApplicationContextFromServletContext(context);
                    webappCopy = super.locateResourcesDir(context, applicationContext).resolve(filename);
                }
            }
            if (Files.isReadable(webappCopy)) {
                try (ResourceHolder holder = new OpenDALResourceHolder(key, true)) {
                    Log.info(Log.RESOURCES, "Copying " + webappCopy + " to OpenDAL " + key);
                    Files.copy(webappCopy, holder.getPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                final String suffix = FilenameUtils.getExtension(key);

                // find a different format and convert it to our desired format
                if (IMAGE_WRITE_SUFFIXES.contains(suffix.toLowerCase())) {
                    final String suffixless = FilenameUtils.removeExtension(key);
                    String parentDir = FilenameUtils.getFullPath(key);
                    String baseName = FilenameUtils.getBaseName(key);

                    try {
                        List<Entry> entries = operator.list(parentDir);
                        for (Entry entry : entries) {
                            String entryPath = entry.getPath();
                            String entryName = getFilenameFromPath(entryPath);
                            if (entryName.startsWith(baseName + ".")) {
                                final String ext = FilenameUtils.getExtension(entryName).toLowerCase();
                                if (IMAGE_READ_SUFFIXES.contains(ext)) {
                                    String entryKey = entryPath;
                                    try (ResourceHolder in = new OpenDALResourceHolder(entryKey, true);
                                         ResourceHolder out = new OpenDALResourceHolder(key, true)) {
                                        try (InputStream inS = IO.newInputStream(in.getPath());
                                             OutputStream outS = Files.newOutputStream(out.getPath())) {
                                            Log.info(Log.RESOURCES, "Converting " + entryKey + " to " + key);
                                            BufferedImage image = ImageIO.read(inS);
                                            ImageIO.write(image, suffix, outS);
                                            break;
                                        } catch (IOException e) {
                                            if (context != null) {
                                                context.log("Unable to convert image from " + in.getPath() + " to " +
                                                        out.getPath(), e);
                                            } else {
                                                Log.warning(Log.RESOURCES, "Unable to convert image from " +
                                                        in.getPath() + " to " + out.getPath(), e);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }

        return getKeyPath(key);
    }

    @Override
    protected void addFiles(final DirectoryStream.Filter<Path> iconFilter, final Path webappDir,
                            final HashSet<Path> result) {
        String keyDir = getKey(webappDir);
        if (!keyDir.endsWith("/")) {
            keyDir += "/";
        }
        final Operator operator = opendal.getOperator();
        try {
            List<Entry> entries = operator.list(keyDir);
            for (Entry entry : entries) {
                String fullKey = entry.getPath();
                if (fullKey.endsWith("/")) {
                    continue;
                }
                final Path curPath = getKeyPath(fullKey);
                try {
                    if (iconFilter.accept(curPath)) {
                        result.add(curPath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            Log.error(Log.RESOURCES, "Error listing files in: " + keyDir, e);
        }
    }

    private String getFilenameFromPath(String path) {
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }

    @Nullable
    @Override
    public FileTime getLastModified(final Path resourcesDir, final ServletContext context,
                                    final Path appPath, final String filename) throws IOException {
        final Path file = locateResource(resourcesDir, context, appPath, filename);
        final String key = getKey(file);
        final Operator operator = opendal.getOperator();
        try {
            Metadata metadata = operator.stat(key);
            return FileTime.from(metadata.getLastModified());
        } catch (Exception e) {
            Log.error(Log.RESOURCES, "Error getting last modified for: " + key, e);
        }
        return null;
    }

    @Override
    public void deleteImageIfExists(final String image, final Path dir) throws IOException {
        Path icon = findImagePath(image, dir);
        if (icon != null) {
            opendal.getOperator().delete(getKey(icon));
        }
    }

    private class OpenDALResourceHolder implements ResourceHolder {
        private final String key;
        private Path path = null;
        private boolean writeOnClose = false;

        private OpenDALResourceHolder(final String key, boolean writeOnClose) {
            this.key = key;
            this.writeOnClose = writeOnClose;
        }

        @Override
        public Path getPath() {
            if (path != null) {
                return path;
            }
            final String[] splittedKey = key.split("/");
            try {
                path = Files.createTempFile("opendal-res-", splittedKey[splittedKey.length - 1]);
                final Operator operator = opendal.getOperator();
                try {
                    Metadata metadata = operator.stat(key);
                    byte[] data = operator.read(key);
                    Files.write(path, data);
                } catch (Exception e) {
                    if (writeOnClose) {
                        Files.delete(path);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return path;
        }

        @Override
        public String getRelativePath() {
            return key;
        }

        @Override
        public FileTime getLastModifiedTime() throws IOException {
            Metadata metadata = opendal.getOperator().stat(key);
            return FileTime.from(metadata.getLastModified());
        }

        @Override
        public void abort() {
            writeOnClose = false;
        }

        @Override
        public void close() throws IOException {
            if (path == null) {
                return;
            }
            try {
                if (writeOnClose && Files.isReadable(path)) {
                    byte[] data = Files.readAllBytes(path);
                    opendal.getOperator().write(key, data);
                }
            } finally {
                FileUtils.deleteQuietly(path.toFile());
                path = null;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            if (path != null) {
                FileUtils.deleteQuietly(path.toFile());
            }
        }
    }
}

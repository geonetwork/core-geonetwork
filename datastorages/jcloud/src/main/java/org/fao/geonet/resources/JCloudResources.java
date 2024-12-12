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

import static org.jclouds.blobstore.options.PutOptions.Builder.multipart;

import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.server.context.ServiceContext;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.ListContainerOptions;
import org.jclouds.http.HttpResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

public class JCloudResources extends Resources {
    @Autowired
    JCloudConfiguration jCloudConfiguration;

    private Path resourceBaseDir = null;

    @Override
    public Path locateResourcesDir(final ServletContext context, final ApplicationContext applicationContext) {
        if (this.resourceBaseDir == null) {
            Path systemFullDir = applicationContext.getBean(GeonetworkDataDirectory.class).getSystemDataDir();
            Path resourceFullDir = applicationContext.getBean(GeonetworkDataDirectory.class).getResourcesDir();

            // If the metadata full dir is relative from the system dir then use system dir as the base dir.
            if (resourceFullDir.toString().startsWith(systemFullDir.toString())) {
                this.resourceBaseDir = systemFullDir;
            } else {
                // If the metadata full dir is an absolute folder then use that as the base dir.
                if (resourceFullDir.isAbsolute()) {
                    this.resourceBaseDir = resourceFullDir.getRoot();
                } else {
                    // use it as a relative url.
                    this.resourceBaseDir = Paths.get(".");
                }
            }

            if (this.resourceBaseDir.toString().equals(".")) {
                this.resourceBaseDir = Paths.get(jCloudConfiguration.getBaseFolder()).resolve(resourceFullDir);
            } else {
                this.resourceBaseDir = Paths.get(jCloudConfiguration.getBaseFolder()).resolve(this.resourceBaseDir.relativize(resourceFullDir));
            }
        }
        return this.resourceBaseDir;
    }

    private String getKey(final Path dir, final String name) {
        return getKey(dir.resolve(name));
    }

    private String getKey(final Path path) {

        // Get keyPath as a relative path from /.
        Path keyPath;
        if (path.startsWith(Paths.get("/"))) {
            keyPath = Paths.get("/").relativize(path);
        } else {
            keyPath = path;
        }


        if (resourceBaseDir != null) {
            // If it starts with resource folder then it is missing the basePath so add it.
            if (keyPath.startsWith(Paths.get(jCloudConfiguration.getBaseFolder()).relativize(resourceBaseDir))) {
                keyPath = Paths.get(jCloudConfiguration.getBaseFolder()).resolve(keyPath);
            } else {
                Path resourceDir = Paths.get(jCloudConfiguration.getBaseFolder()).resolve(resourceBaseDir);
                // If it starts with the resource dir by not starting with a "/" then add the "/"
                if (keyPath.startsWith(Paths.get("/").relativize(resourceDir))) {
                    keyPath = Paths.get("/").resolve(keyPath);
                } else {
                    // If it does not start with resource folder then it is missing so add it.
                    if (!keyPath.startsWith(resourceDir)) {
                        keyPath = resourceDir.resolve(keyPath);
                    }
                }
            }
        }

        String key;
        // For windows it may be "\" in which case we need to change it to folderDelimiter which is normally "/"
        if (keyPath.getFileSystem().getSeparator().equals(jCloudConfiguration.getFolderDelimiter())) {
            key = keyPath.toString();
        } else {
            key = keyPath.toString().replace(keyPath.getFileSystem().getSeparator(), jCloudConfiguration.getFolderDelimiter());
        }
        // For Windows, the pathString may start with // so remove one if this is the case.
        if (key.startsWith("//")) {
            key = key.substring(1);
        }

        // Make sure the key that is returns does not starts with "/" as it is already assumed to be relative to the container.
        if (key.startsWith(jCloudConfiguration.getFolderDelimiter())) {
            return key.substring(1);
        } else {
            return key;
        }
    }

    private Path getKeyPath(String key) {
        // Keypath should not reference the base path so it should be removed.
        return Paths.get(key.substring(jCloudConfiguration.getBaseFolder().length()));
    }

    @Nullable
    @Override
    protected Path findImagePath(final String imageName, final Path logosDir) {
        final String key = getKey(logosDir, imageName);
        if (imageName.indexOf('.') > -1) {
            if (jCloudConfiguration.getClient().getBlobStore().blobExists(jCloudConfiguration.getContainerName(), key)) {
                return getKeyPath(key);
            }
        } else {
            ListContainerOptions opts = new ListContainerOptions();
            opts.delimiter(jCloudConfiguration.getFolderDelimiter());
            opts.prefix(key);

            // Page through the data
            String marker = null;
            do {
                if (marker != null) {
                    opts.afterMarker(marker);
                }

                PageSet<? extends StorageMetadata> page = jCloudConfiguration.getClient().getBlobStore().list(jCloudConfiguration.getContainerName(), opts);

                for (StorageMetadata storageMetadata : page) {
                    // Only add to the list if it is a blob and it matches the filter.
                    if (storageMetadata.getType() == StorageType.BLOB) {
                        String ext = FilenameUtils.getExtension(storageMetadata.getName());
                        if (Resources.IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
                            return getKeyPath(storageMetadata.getName());
                        }
                    }
                }
                marker = page.getNextMarker();
            } while (marker != null);
        }
        return null;
    }

    @Nullable
    @Override
    public ResourceHolder getImage(final ServiceContext context, final String imageName,
                                   final Path logosDir) {
        Path path = findImagePath(imageName, logosDir);
        if (path != null) {
            String key = getKey(path);
            return new JCloudResourceHolder(key, false);
        } else {
            return null;
        }
    }

    @Override
    public ResourceHolder getWritableImage(final ServiceContext context, final String imageName,
                                           final Path logosDir) {
        return new JCloudResourceHolder(getKey(logosDir, imageName), true);
    }

    @Override
    Pair<byte[], Long> loadResource(final Path resourcesDir, final ServletContext context,
                                    final Path appPath, final String filename, final byte[] defaultValue,
                                    final long loadSince) throws IOException {
        final Path file = locateResource(resourcesDir, context, appPath, filename);
        final String key = getKey(file, filename);
        try {
            final Blob object = jCloudConfiguration.getClient().getBlobStore().getBlob(jCloudConfiguration.getContainerName(), key);
            if (object != null) {
                final long lastModified = object.getMetadata().getLastModified().toInstant().toEpochMilli();
                try (InputStream in = object.getPayload().openStream()) {
                    if (loadSince < 0 || lastModified > loadSince) {
                        // Todo Modify to support stream otherwise reading full will kill memory
                        byte[] content = new byte[(int) object.getMetadata().getSize().intValue()];
                        new DataInputStream(in).readFully(content);
                        return Pair.read(content, lastModified);
                    } else {
                        return Pair.read(defaultValue, loadSince);
                    }
                }
            } else {
                Log.info(Log.RESOURCES, "Error loading resource " + jCloudConfiguration.getContainerName() + ":" + key);
            }
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                Log.warning(Geonet.RESOURCES,
                    String.format("Unable to locate resource '%s'.", key));
            } else {
                throw e;
            }
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
            key = jCloudConfiguration.getFolderDelimiter() + filename;
        }


        if (!jCloudConfiguration.getClient().getBlobStore().blobExists(jCloudConfiguration.getContainerName(), key)) {
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
                final ConfigurableApplicationContext applicationContext =
                    JeevesDelegatingFilterProxy.getApplicationContextFromServletContext(context);
                if (resourcesDir.equals(locateResourcesDir(context, applicationContext))) {
                    webappCopy = super.locateResourcesDir(context, applicationContext).resolve(filename);
                }
            }
            if (Files.isReadable(webappCopy)) {
                try (ResourceHolder holder = new JCloudResourceHolder(key, true)) {
                    Log.info(Log.RESOURCES, "Copying " + webappCopy + " to " + holder.getPath() + " for resource " + key);
                    Files.copy(webappCopy, holder.getPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {

                final String suffix = FilenameUtils.getExtension(key);

                // find a different format and convert it to our desired format
                if (Resources.IMAGE_WRITE_SUFFIXES.contains(suffix.toLowerCase())) {
                    final String suffixless = FilenameUtils.removeExtension(key);
                    ListContainerOptions opts = new ListContainerOptions();
                    opts.delimiter(jCloudConfiguration.getFolderDelimiter());
                    opts.prefix(suffixless);

                    // Page through the data
                    String marker = null;
                    do {
                        if (marker != null) {
                            opts.afterMarker(marker);
                        }

                        PageSet<? extends StorageMetadata> page = jCloudConfiguration.getClient().getBlobStore().list(jCloudConfiguration.getContainerName(), opts);

                        for (StorageMetadata storageMetadata : page) {
                            // Only add to the list if it is a blob and it matches the filter.
                            if (storageMetadata.getType() == StorageType.BLOB) {
                                final String ext = FilenameUtils.getExtension(storageMetadata.getName()).toLowerCase();
                                if (Resources.IMAGE_READ_SUFFIXES.contains(ext)) {
                                    try (ResourceHolder in = new JCloudResourceHolder(storageMetadata.getName(), true);
                                         ResourceHolder out = new JCloudResourceHolder(key, true)) {
                                        try (InputStream inS = IO.newInputStream(in.getPath());
                                             OutputStream outS = java.nio.file.Files.newOutputStream(out.getPath())) {
                                            Log.info(Log.RESOURCES, "Converting " + storageMetadata.getName() + " to " + key);
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
                        marker = page.getNextMarker();
                    } while (marker != null);
                }
            }
        }

        return getKeyPath(key);
    }

    @Override
    protected void addFiles(final DirectoryStream.Filter<Path> iconFilter, final Path webappDir,
                            final HashSet<Path> result) {
        ListContainerOptions opts = new ListContainerOptions();
        opts.delimiter(jCloudConfiguration.getFolderDelimiter());
        opts.prefix(getKey(webappDir) + jCloudConfiguration.getFolderDelimiter());

        // Page through the data
        String marker = null;
        do {
            if (marker != null) {
                opts.afterMarker(marker);
            }

            PageSet<? extends StorageMetadata> page = jCloudConfiguration.getClient().getBlobStore().list(jCloudConfiguration.getContainerName(), opts);

            for (StorageMetadata storageMetadata : page) {
                // Only add to the list if it is a blob and it matches the filter.
                if (storageMetadata.getType() == StorageType.BLOB) {
                    final Path curPath = getKeyPath(storageMetadata.getName());
                    try {
                        if (iconFilter.accept(curPath)) {
                            result.add(curPath);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            marker = page.getNextMarker();
        } while (marker != null);
    }

    @Nullable
    @Override
    public FileTime getLastModified(final Path resourcesDir, final ServletContext context,
                                    final Path appPath, final String filename) throws IOException {
        final Path file = locateResource(resourcesDir, context, appPath, filename);
        final String key = getKey(file);
        try {
            final Blob object = jCloudConfiguration.getClient().getBlobStore().getBlob(jCloudConfiguration.getContainerName(), key);
            if (object == null) {
                return null;
            } else {
                return FileTime.from(object.getMetadata().getLastModified().toInstant());
            }
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // key does not exist
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public void deleteImageIfExists(final String image, final Path dir) {
        Path icon = findImagePath(image, dir);
        if (icon != null) {
            jCloudConfiguration.getClient().getBlobStore().removeBlob(jCloudConfiguration.getContainerName(), getKey(icon));
        }
    }

    private class JCloudResourceHolder implements ResourceHolder {
        private final String key;
        private Path path = null;
        private Path tempFolderPath = null;
        private boolean writeOnClose = false;

        private JCloudResourceHolder(final String key, boolean writeOnClose) {
            this.key = key;
            this.writeOnClose = writeOnClose;
        }

        @Override
        public Path getPath() {
            if (path != null) {
                return path;
            }
            final String[] splittedKey = key.split(jCloudConfiguration.getFolderDelimiter());
            try {
                // Preserve filename by putting the files into a temporary folder and using the same filename.
                tempFolderPath = Files.createTempDirectory("gn-res-" + splittedKey[splittedKey.length - 2] + "-");
                tempFolderPath.toFile().deleteOnExit();
                path = tempFolderPath.resolve(splittedKey[splittedKey.length - 1]);

                try {
                    final Blob object = jCloudConfiguration.getClient().getBlobStore().getBlob(jCloudConfiguration.getContainerName(), key);
                    if (object == null) {
                        if (writeOnClose && Files.exists(path)) {
                            Files.delete(path);
                        }
                    } else {
                        try (InputStream in = object.getPayload().openStream()) {
                            java.nio.file.Files.copy(in, path,
                                StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (HttpResponseException e) {
                    if (e.getResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                        if (writeOnClose && Files.exists(path)) {
                            Files.delete(path);
                        }
                    } else {
                        throw e;
                    }
                }
            } catch (IOException e) {
                Log.error(Geonet.RESOURCES, String.format(
                    "Error getting path for resource '%s'.", key), e);
                throw new RuntimeException(e);
            }

            return path;
        }

        @Override
        public String getRelativePath() {
            return key;
        }

        @Override
        public FileTime getLastModifiedTime() {
            final Blob object = jCloudConfiguration.getClient().getBlobStore().getBlob(jCloudConfiguration.getContainerName(), key);
            if (object == null) {
                return null;
            } else {
                return FileTime.from(object.getMetadata().getLastModified().toInstant());
            }
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
                    Blob blob = jCloudConfiguration.getClient().getBlobStore().blobBuilder(key)
                        .payload(path.toFile())
                        .contentLength(Files.size(path))
                        .build();
                    // Upload the Blob
                    jCloudConfiguration.getClient().getBlobStore().putBlob(jCloudConfiguration.getContainerName(), blob, multipart());
                }
            } finally {
                // Delete temporary file and folder.
                IO.deleteFileOrDirectory(tempFolderPath, true);
                path = null;
                tempFolderPath = null;
            }
        }
    }
}

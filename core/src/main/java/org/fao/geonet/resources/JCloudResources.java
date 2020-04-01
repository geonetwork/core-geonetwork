package org.fao.geonet.resources;


import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.server.context.ServiceContext;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.domain.Pair;
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
    JCloudCredentials jCloudCredentials;

    @Override
    protected Path getBasePath(final ServiceContext context) {
        // TODO: I'm not sure it can be always the same...
        return Paths.get("/");
    }

    @Override
    public Path locateResourcesDir(final ServletContext context, final ApplicationContext applicationContext) {
        // TODO: I'm not sure it can be always the same...
        return Paths.get("/resources");
    }

    @Override
    protected Path locateResourcesDir(final ServiceContext context) {
        // TODO: I'm not sure it can be always the same...
        return Paths.get("/resources");
    }

    private String getKey(final Path dir, final String name) {
        return getKey(dir.resolve(name));
    }

    private String getKey(final Path path) {
        String pathString;
        // For windows it may be "\" in which case we need to change it to folderDelimiter which is normally "/"
        if (path.getFileSystem().getSeparator().equals(jCloudCredentials.getFolderDelimiter())) {
            pathString = path.toString();
        } else {
            pathString=path.toString().replace(path.getFileSystem().getSeparator(), jCloudCredentials.getFolderDelimiter());
        }
        // remove leading / from path since the basefolder already ends with "/". Absolute paths will have 2 "/"
        return jCloudCredentials.getBaseFolder() + (path.isAbsolute() ? pathString.substring(2) : pathString.substring(1));
    }

    private Path getKeyPath(String key) {
        return Paths.get("/" + key.substring(jCloudCredentials.getBaseFolder().length()));
    }

    @Nullable
    @Override
    protected Path findImagePath(final String imageName, final Path logosDir) {
        final String key = getKey(logosDir, imageName);
        if (imageName.indexOf('.') > -1) {
            if (jCloudCredentials.getClient().getBlobStore().blobExists(jCloudCredentials.getContainerName(), key)) {
                return getKeyPath(key);
            }
        } else {
            ListContainerOptions opts = new ListContainerOptions();
            opts.delimiter(jCloudCredentials.getFolderDelimiter());
            opts.prefix(key);

            // Page through the data
            String marker = null;
            do {
                if (marker != null) {
                    opts.afterMarker(marker);
                }

                PageSet<? extends StorageMetadata> page = jCloudCredentials.getClient().getBlobStore().list(jCloudCredentials.getContainerName(), opts);

                for (StorageMetadata storageMetadata : page) {
                    // Only add to the list if it is a blob and it matches the filter.
                    if (storageMetadata.getType() == StorageType.BLOB) {
                        String ext = FilenameUtils.getExtension(storageMetadata.getName());
                        if (IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
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
        final String key = getKey(file);
        try {
            final Blob object = jCloudCredentials.getClient().getBlobStore().getBlob(jCloudCredentials.getContainerName(), key);
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
                Log.info(Log.RESOURCES, "Error loading resource " + jCloudCredentials.getContainerName() + ":" + key);
            }
        } catch(HttpResponseException e) {
            if (e.getResponse().getStatusCode() != HttpStatus.SC_NOT_FOUND) {
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
            key = jCloudCredentials.getBaseFolder() + filename;
        }


        if (!jCloudCredentials.getClient().getBlobStore().blobExists(jCloudCredentials.getContainerName(), key)) {
            Path webappCopy = null;
            if (context != null) {
                final String realPath = context.getRealPath(filename);
                if (realPath != null) {
                    webappCopy = IO.toPath(realPath);
                }
            }

            if (webappCopy == null) {
                webappCopy = appPath.resolve(filename);  // TODO: this won't work...
            }
            if (!java.nio.file.Files.isReadable(webappCopy)) {
                if (resourcesDir.equals(Paths.get("/resources"))) {
                    final ConfigurableApplicationContext applicationContext =
                        JeevesDelegatingFilterProxy.getApplicationContextFromServletContext(context);
                    webappCopy = super.locateResourcesDir(context, applicationContext).resolve(filename);
                }
            }
            if (java.nio.file.Files.isReadable(webappCopy)) {
                try (ResourceHolder holder = new JCloudResourceHolder(key, true)) {
                    Log.info(Log.RESOURCES, "Copying " + webappCopy + " to " + key);
                    Files.copy(webappCopy, holder.getPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {

                final String suffix = FilenameUtils.getExtension(key);

                // find a different format and convert it to our desired format
                if (IMAGE_WRITE_SUFFIXES.contains(suffix.toLowerCase())) {
                    final String suffixless = FilenameUtils.removeExtension(key);
                    ListContainerOptions opts = new ListContainerOptions();
                    opts.delimiter(jCloudCredentials.getFolderDelimiter());
                    opts.prefix(suffixless);

                    // Page through the data
                    String marker = null;
                    do {
                        if (marker != null) {
                            opts.afterMarker(marker);
                        }

                        PageSet<? extends StorageMetadata> page = jCloudCredentials.getClient().getBlobStore().list(jCloudCredentials.getContainerName(), opts);

                        for (StorageMetadata storageMetadata : page) {
                            // Only add to the list if it is a blob and it matches the filter.
                            if (storageMetadata.getType() == StorageType.BLOB) {
                                final String ext = FilenameUtils.getExtension(storageMetadata.getName()).toLowerCase();
                                if (IMAGE_READ_SUFFIXES.contains(ext)) {
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
        opts.delimiter(jCloudCredentials.getFolderDelimiter());
        opts.prefix(getKey(webappDir) + jCloudCredentials.getFolderDelimiter());

        // Page through the data
        String marker = null;
        do {
            if (marker != null) {
                opts.afterMarker(marker);
            }

            PageSet<? extends StorageMetadata> page = jCloudCredentials.getClient().getBlobStore().list(jCloudCredentials.getContainerName(), opts);

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
            final Blob object = jCloudCredentials.getClient().getBlobStore().getBlob(jCloudCredentials.getContainerName(), key);
            if (object==null) {
                return null;
            } else {
                return FileTime.from(object.getMetadata().getLastModified().toInstant());
            }
        } catch(HttpResponseException e) {
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
            jCloudCredentials.getClient().getBlobStore().removeBlob(jCloudCredentials.getContainerName(), getKey(icon));
        }
    }

    private class JCloudResourceHolder implements ResourceHolder {
        private final String key;
        private Path path = null;
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
            final String[] splittedKey = key.split("/");
            try {
                path = java.nio.file.Files.createTempFile("", splittedKey[splittedKey.length - 1]);
                try {
                    final Blob object = jCloudCredentials.getClient().getBlobStore().getBlob(jCloudCredentials.getContainerName(), key);
                    if (object == null) {
                        if (writeOnClose) {
                            Files.delete(path);
                        }
                    } else {
                        try (InputStream in = object.getPayload().openStream()) {
                            java.nio.file.Files.copy(in, path,
                                    StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (HttpResponseException e) {
                    if (e.getResponse().getStatusCode() == HttpStatus.SC_NOT_FOUND && writeOnClose) {
                        Files.delete(path);
                    } else if (e.getResponse().getStatusCode() != HttpStatus.SC_NOT_FOUND) {
                        throw e;
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
        public FileTime getLastModifiedTime() {
            final Blob object = jCloudCredentials.getClient().getBlobStore().getBlob(jCloudCredentials.getContainerName(), key);
            if (object==null) {
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
            if (writeOnClose && Files.isReadable(path)) {
                Blob blob = jCloudCredentials.getClient().getBlobStore().blobBuilder(key)
                        .payload(path.toFile())
                        .contentLength(Files.size(path))
                        .build();
                // Upload the Blob
                jCloudCredentials.getClient().getBlobStore().putBlob(jCloudCredentials.getContainerName(), blob);
            }
            java.nio.file.Files.delete(path);
        }
    }
}

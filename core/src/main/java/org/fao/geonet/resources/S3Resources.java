package org.fao.geonet.resources;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import jeeves.config.springutil.JeevesDelegatingFilterProxy;
import jeeves.server.context.ServiceContext;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
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

public class S3Resources extends Resources {
    @Autowired
    S3Credentials s3;

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
        return s3.getKeyPrefix() + (path.isAbsolute() ? path.toString().substring(1) : path.toString());
    }

    private Path getKeyPath(String key) {
        return Paths.get("/" + key.substring(s3.getKeyPrefix().length()));
    }

    @Nullable
    @Override
    protected Path findImagePath(final String imageName, final Path logosDir) {
        final String key = getKey(logosDir, imageName);
        if (imageName.indexOf('.') > -1) {
            if (s3.getClient().doesObjectExist(s3.getBucket(), key)) {
                return getKeyPath(key);
            }
        } else {
            final ListObjectsV2Result objects = s3.getClient().listObjectsV2(s3.getBucket(), key);
            for (S3ObjectSummary next: objects.getObjectSummaries()) {
                String ext = FilenameUtils.getExtension(next.getKey());
                if (IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
                    return getKeyPath(next.getKey());
                }
            }
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
            return new S3ResourceHolder(key, false);
        } else {
            return null;
        }
    }

    @Override
    public ResourceHolder getWritableImage(final ServiceContext context, final String imageName,
                                           final Path logosDir) {
        return new S3ResourceHolder(getKey(logosDir, imageName), true);
    }

    @Override
    Pair<byte[], Long> loadResource(final Path resourcesDir, final ServletContext context,
                                    final Path appPath, final String filename, final byte[] defaultValue,
                                    final long loadSince) throws IOException {
        final Path file = locateResource(resourcesDir, context, appPath, filename);
        final String key = getKey(file);
        try {
            final S3Object object = s3.getClient().getObject(s3.getBucket(), key);
            final long lastModified = object.getObjectMetadata().getLastModified().toInstant().toEpochMilli();
            try (S3ObjectInputStream in = object.getObjectContent()) {
                if (loadSince < 0 || lastModified > loadSince) {
                    byte[] content = new byte[(int) object.getObjectMetadata().getContentLength()];
                    new DataInputStream(in).readFully(content);
                    return Pair.read(content, lastModified);
                } else {
                    return Pair.read(defaultValue, loadSince);
                }
            }
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
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
            key = s3.getKeyPrefix() + filename;
        }


        if (!s3.getClient().doesObjectExist(s3.getBucket(), key)) {
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
                try (ResourceHolder holder = new S3ResourceHolder(key, true)) {
                    Log.info(Log.RESOURCES, "Copying " + webappCopy + " to S3 " + key);
                    Files.copy(webappCopy, holder.getPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {

                final String suffix = FilenameUtils.getExtension(key);

                // find a different format and convert it to our desired format
                if (IMAGE_WRITE_SUFFIXES.contains(suffix.toLowerCase())) {
                    final String suffixless = FilenameUtils.removeExtension(key);
                    final ListObjectsV2Result objects =
                        s3.getClient().listObjectsV2(s3.getBucket(), suffixless + ".");
                    for (S3ObjectSummary object: objects.getObjectSummaries()) {
                        final String ext = FilenameUtils.getExtension(object.getKey()).toLowerCase();
                        if (IMAGE_READ_SUFFIXES.contains(ext)) {
                            try (ResourceHolder in = new S3ResourceHolder(object.getKey(), true);
                                 ResourceHolder out = new S3ResourceHolder(key, true)) {
                                try (InputStream inS = IO.newInputStream(in.getPath());
                                     OutputStream outS = java.nio.file.Files.newOutputStream(out.getPath())) {
                                    Log.info(Log.RESOURCES, "Converting " + object.getKey() + " to " + key);
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
            }
        }

        return getKeyPath(key);
    }

    @Override
    protected void addFiles(final DirectoryStream.Filter<Path> iconFilter, final Path webappDir,
                            final HashSet<Path> result) {
        final ListObjectsV2Result objects =
            s3.getClient().listObjectsV2(s3.getBucket(), getKey(webappDir) + "/");
        for (S3ObjectSummary cur: objects.getObjectSummaries()) {
            final Path curPath = getKeyPath(cur.getKey());
            try {
                if (iconFilter.accept(curPath)) {
                    result.add(curPath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Nullable
    @Override
    public FileTime getLastModified(final Path resourcesDir, final ServletContext context,
                                    final Path appPath, final String filename) throws IOException {
        final Path file = locateResource(resourcesDir, context, appPath, filename);
        final String key = getKey(file);
        try {
            final ObjectMetadata md = s3.getClient().getObjectMetadata(s3.getBucket(), key);
            return FileTime.from(md.getLastModified().toInstant());
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                // bucket/key does not exist
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
            s3.getClient().deleteObject(s3.getBucket(), getKey(icon));
        }
    }

    private class S3ResourceHolder implements ResourceHolder {
        private final String key;
        private Path path = null;
        private boolean writeOnClose = false;

        private S3ResourceHolder(final String key, boolean writeOnClose) {
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
                    final S3Object object = s3.getClient().getObject(s3.getBucket(), key);
                    try (S3ObjectInputStream in = object.getObjectContent()) {
                        java.nio.file.Files.copy(in, path,
                                                 StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (AmazonServiceException e) {
                    if (e.getStatusCode() == HttpStatus.SC_NOT_FOUND && writeOnClose) {
                        Files.delete(path);
                    } else if (e.getStatusCode() != HttpStatus.SC_NOT_FOUND) {
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
            final ObjectMetadata md = s3.getClient().getObjectMetadata(s3.getBucket(), key);
            return FileTime.from(md.getLastModified().toInstant());
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
                s3.getClient().putObject(s3.getBucket(), key, path.toFile());
            }
            java.nio.file.Files.delete(path);
        }
    }
}

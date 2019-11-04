package org.fao.geonet.resources;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FilenameUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

public class FileResources extends Resources {
    @Override
    protected @Nullable Path findImagePath(String imageName, Path logosDir) throws IOException {
        if (imageName.indexOf('.') > -1) {
            final Path imagePath = logosDir.resolve(imageName);
            if (Files.exists(imagePath)) {
                return imagePath;
            }
        } else {
            try (DirectoryStream<Path> possibleLogos = Files.newDirectoryStream(logosDir, imageName + ".*")) {
                for (final Path next: possibleLogos) {
                    String ext = FilenameUtils.getExtension(next.getFileName().toString());
                    if (IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
                        return next;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public @Nullable ResourceHolder getImage(ServiceContext context,
                                             String imageName, Path logosDir) throws IOException {
        Path path = findImagePath(imageName, logosDir);
        if (path != null) {
            return new FileResourceHolder(getBasePath(context), path);
        } else {
            return null;
        }
    }

    Pair<byte[], Long> loadResource(Path resourcesDir,
                                    ServletContext context, Path appPath, String filename,
                                    byte[] defaultValue, long loadSince) throws IOException {
        Path file = locateResource(resourcesDir, context, appPath, filename);

        if (Files.exists(file)) {

            try {
                final long lastModified = Files.getLastModifiedTime(file).to(TimeUnit.MILLISECONDS);
                if (loadSince < 0 || lastModified > loadSince) {
                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    Files.copy(file, data);
                    return Pair.read(data.toByteArray(), lastModified);
                } else {
                    return Pair.read(defaultValue, loadSince);
                }
            } catch (IOException e) {
                Log.warning(Geonet.RESOURCES, "Unable to find resource: "
                    + filename);
            }
        }
        return Pair.read(defaultValue, -1L);
    }

    @Override
    public ResourceHolder getWritableImage(ServiceContext context, String imageName, Path logosDir) {
        try {
            Files.createDirectories(logosDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Path path = logosDir.resolve(imageName);
        return new FileResourceHolder(getBasePath(context), path);
    }

    protected Path locateResource(@Nullable Path resourcesDir,
                                  ServletContext context, Path appPath,
                                  @Nonnull String filename) throws IOException {
        if (filename.charAt(0) == '/' || filename.charAt(0) == '\\') {
            filename = filename.substring(1);
        }

        Path file;
        if (resourcesDir != null) {
            file = resourcesDir.resolve(filename);
        } else {
            file = IO.toPath(filename);
        }

        if (!Files.exists(file)) {
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
            if (Files.exists(webappCopy)) {
                IO.copyDirectoryOrFile(webappCopy, file, false);
            }

            final String fileName = file.getFileName().toString();
            final int indexOfDot = fileName.lastIndexOf(".");
            final String suffixless = FilenameUtils.removeExtension(fileName);
            final String suffix = FilenameUtils.getExtension(fileName);

            if (!Files.exists(file) && IMAGE_WRITE_SUFFIXES.contains(suffix.toLowerCase())) {
                // find a different format and convert it to our desired format
                DirectoryStream.Filter<Path> filter = entry -> {
                    String name = entry.getFileName().toString();
                    boolean startsWith = name.startsWith(suffixless);
                    final String ext = FilenameUtils.getExtension(name).toLowerCase();
                    boolean canReadImage = name.length() > indexOfDot && IMAGE_READ_SUFFIXES.contains(ext);
                    return startsWith && canReadImage;
                };
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(file.getParent(), filter)) {
                    Iterator<Path> iter = paths.iterator();
                    if (iter.hasNext()) {
                        Path path = iter.next();
                        try (
                            InputStream in = IO.newInputStream(path);
                            OutputStream out = Files.newOutputStream(file)
                        ) {
                            try {
                                BufferedImage image = ImageIO.read(in);
                                ImageIO.write(image, suffix, out);
                            } catch (IOException e) {
                                if (context != null) {
                                    context.log("Unable to convert image from " + path + " to " + file, e);
                                }
                            }
                        }
                    }
                }
            }
        }

        return file;
    }

    protected void addFiles(DirectoryStream.Filter<Path> iconFilter, Path webappDir, HashSet<Path> result) {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(webappDir, iconFilter)) {
            for (Path file : paths) {
                result.add(file);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileTime getLastModified(Path resourcesDir, ServletContext context, Path appPath, String filename) throws IOException {
        Path file = locateResource(resourcesDir, context, appPath, filename);
        if (file != null && Files.exists(file)) {
            return Files.getLastModifiedTime(file);
        }
        return null;
    }

    public void deleteImageIfExists(final String image, final Path dir) throws IOException {
        Path icon = findImagePath(image, dir);
        if (icon != null) {
            Files.deleteIfExists(icon);
        }
    }

    private static class FileResourceHolder implements ResourceHolder {
        private String relativePath;
        private final Path path;

        private FileResourceHolder(final Path basePath, final Path path) {
            this.relativePath = basePath.relativize(path).toString().replace('\\', '/');
            this.path = path;
        }

        @Override
        public Path getPath() {
            return path;
        }

        @Override
        public String getRelativePath() {
            return relativePath;
        }

        @Override
        public FileTime getLastModifiedTime() throws IOException {
            return Files.getLastModifiedTime(path);
        }

        @Override
        public void abort() {
            IO.deleteFile(getPath(), false, Geonet.GEONETWORK);
        }

        @Override
        public void close() {
            // nothing to do
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }
}

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

import com.google.common.collect.Sets;
import com.google.common.io.Files;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.MetadataResource;
import org.fao.geonet.domain.MetadataResourceVisibility;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.XmlRequest;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Utility methods for managing resources that are site dependent. In other words that can be added
 * to or modified by Geonetwork and therefore should not be overwritten if the geonetwork webapp is
 * redeployed. Those resources are usually located in <geonetwork.dir>/resources. htmlcache,
 * images/logos, images/harvesting are the main updated resources.
 * <p/>
 * User: jeichar Date: 1/17/12 Time: 5:51 PM
 */
public class Resources {

    private final static Set<String> IMAGE_READ_SUFFIXES;
    private final static Set<String> IMAGE_WRITE_SUFFIXES;
    private static final Set<String> IMAGE_EXTENSIONS = Sets.newHashSet(ImageIO.getReaderFileSuffixes());

    static {
        HashSet<String> suffixes = new HashSet<>();
        for (String string : ImageIO.getReaderFileSuffixes()) {
            suffixes.add(string.toLowerCase());
        }

        IMAGE_READ_SUFFIXES = Collections.unmodifiableSet(suffixes);

        suffixes = new HashSet<>();
        for (String string : ImageIO.getReaderFileSuffixes()) {
            suffixes.add(string.toLowerCase());
        }

        IMAGE_WRITE_SUFFIXES = Collections.unmodifiableSet(suffixes);
    }

    /**
     * Find the configured directory containing logos. The directory the logos are located in
     * depends on the configuration of dataImagesDir in the config.xml.
     * <p/>
     * (Overrides will be applied so the actual config can be in overrides)
     *
     * @return locateResourcesDir(...) + FS + "images" + FS + "logos"
     */
    public Path locateLogosDir(ServiceContext context) {
        return getPath(context, "logos");
    }

    /**
     * Look in the logos dir for an image with the name provided.  If the imageName does not have an
     * extension then the logos dir will be searched for all images with the provided name.
     *
     * @param imageName the name of the image to look up with or without the file extension
     * @param logosDir  the directory to search
     * @return null if the image does not exist in the logosDir or a path to the image.
     */
    private @Nullable Path findImagePath(String imageName, Path logosDir) throws IOException {
        if (imageName.indexOf('.') > -1) {
            final Path imagePath = logosDir.resolve(imageName);
            if (java.nio.file.Files.exists(imagePath)) {
                return imagePath;
            }
        } else {
            try (DirectoryStream<Path> possibleLogos = java.nio.file.Files.newDirectoryStream(logosDir, imageName + ".*")) {
                for (final Path next: possibleLogos) {
                    String ext = Files.getFileExtension(next.getFileName().toString());
                    if (IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
                        return next;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Get a ResourceHolder pointing to an existing image.
     */
    public @Nullable ResourceHolder getImage(ServiceContext context, String imageName, Path logosDir) throws IOException {
        Path path = findImagePath(imageName, logosDir);
        if (path != null) {
            return new FileResourceHolder(getBasePath(context), path);
        } else {
            return null;
        }
    }

    /**
     * Like {@link #getImage(ServiceContext, String, Path)} but doesn't return null if the file doesn't exist and the caller can modify
     * the returned image.
     */
    public ResourceHolder getWritableImage(ServiceContext context, String imageName, Path logosDir) {
        final Path path = logosDir.resolve(imageName);
        return new FileResourceHolder(getBasePath(context), path);
    }

    private Path getPath(final ServiceContext context, final String logos) {
        final Path base = getBasePath(context);
        Path path = base.resolve("images").resolve(logos);
        try {
            java.nio.file.Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    private Path getBasePath(final ServiceContext context) {
        ServletContext servletContext = null;
        if (context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }
        return servletContext == null ? context.getAppPath() : locateResourcesDir(servletContext, context.getApplicationContext());
    }

    /**
     * Find the configured directory containing harvester logos. The directory the logos are located
     * in depends on the configuration of dataImagesDir in the config.xml.
     * <p/>
     * (Overrides will be applied so the actual config can be in overrides)
     *
     * @return locateResourcesDir(...) + FS + "images" + FS + "harvesting"
     */
    public Path locateHarvesterLogosDir(ServiceContext context) {
        return getPath(context, "harvesting");
    }

    /**
     * The same as {@link #locateHarvesterLogosDir(ServiceContext)} but for Spring MVC
     */
    public Path locateHarvesterLogosDirSMVC(ApplicationContext applicationContext) throws IOException {
        Path path = locateResourcesDir(null, applicationContext).resolve("images").resolve("harvesting");

        java.nio.file.Files.createDirectories(path);

        return path;
    }

    /**
     * The root of the "data" images. A Data image is an image that is dependent on the
     * webapplication instance. They are the images that change from installation to installation
     * like logos and harvester logos.
     * <p/>
     * The directory is configured by config.xml and the configuration overrides. See the
     * information on configuration overrides for methods of configuring them.
     *
     * @return the root of data images. Subdirectories such as logos and harvesting likely contain
     * the actual images
     */
    private Path locateResourcesDir(ServiceContext context) {
        if (context.getServlet() != null) {
            return locateResourcesDir(context.getServlet().getServletContext(), context.getApplicationContext());
        }

        return context.getBean(GeonetworkDataDirectory.class).getResourcesDir();
    }

    /**
     * The root of the "data" images. A Data image is an image that is dependent on the
     * webapplication instance. They are the images that change from installation to installation
     * like logos and harvester logos.
     * <p/>
     * The directory is configured by config.xml and the configuration overrides. See the
     * information on configuration overrides for methods of configuring them.
     *
     * @return the root of data images. Subdirectories such as logos and harvesting likely contain
     * the actual images
     */
    public Path locateResourcesDir(ServletContext context, ApplicationContext applicationContext) {
        Path property = null;
        try {
            property = applicationContext.getBean(GeonetworkDataDirectory.class).getResourcesDir();
        } catch (NoSuchBeanDefinitionException e) {
            final String realPath = context.getRealPath("/WEB-INF/data/resources");
            if (realPath != null) {
                property = IO.toPath(realPath);
            }
        }

        if (property == null) {
            return IO.toPath("resources");
        } else {
            return property;
        }
    }

    /**
     * Load a data image. The "imagesDir" will first be searched for the filename, then the context then
     * finally the appPath+FS+filename. if the image is not in imagesDir but is found in one of the
     * other locations it will be copied to imagesDir for future use.
     * <p/>
     * Use case of this copying is the default harvesting logos are shipped with the webapp and when
     * used they are copied to the imagesDir so that the imagesDir contain all relevant images. This
     * allow system administrators to ignore the images in the webapp and only configure/backup
     * images in the logos directory.
     *
     * @param resourcesDir Should be {@link #locateResourcesDir(ServiceContext)}. This is added as a
     *                     parameter because it takes time to look up the imagesDir. for performance
     *                     reasons a caller can cache imagesDir. If null the imagesDir will be
     *                     looked up.
     * @param context      the context for finding the image if the image is not in imagesDir. (may
     *                     be null)
     * @param appPath      the fallback if images dir and context cannot be used to lookup the
     *                     imagesDir (or if context is null which is permitted)
     * @param filename     the file to look up. will normally contains a prefix like logos/ or
     *                     harvesting/
     * @param defaultValue the image to return if unable to find the actual image
     * @param loadSince    If > -1 then it will be compared to the lastModified of the file. If it
     *                     is < lastModified then the file will be loaded otherwise
     *                     <defaultValue,loadSince> will be returned
     * @return the bytes of the actual image or defaultValue and the lastModified timestamp.
     * Timestamp will be -1 if defaultValue is returned. If <ul> <li>an error occurs</li> <li>file
     * does not exist</li> <li> loadSince is >= file.lastModified<li> </ul> The defaultValue will be
     * returned
     */
    Pair<byte[], Long> loadResource(Path resourcesDir,
                                    ServletContext context, Path appPath, String filename,
                                    byte[] defaultValue, long loadSince) throws IOException {
        Path file = locateResource(resourcesDir, context, appPath, filename);

        if (java.nio.file.Files.exists(file)) {

            try {
                final long lastModified = java.nio.file.Files.getLastModifiedTime(file).to(TimeUnit.MILLISECONDS);
                if (loadSince < 0 || lastModified > loadSince) {
                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    java.nio.file.Files.copy(file, data);
                    return Pair.read(data.toByteArray(), lastModified);
                } else {
                    Pair.read(defaultValue, loadSince);
                }
            } catch (IOException e) {
                Log.warning(Geonet.RESOURCES, "Unable to find resource: "
                    + filename);
            }
        }
        return Pair.read(defaultValue, -1L);
    }

    /**
     * Load a data image. The "imagesDir" ( {@link #locateResourcesDir(javax.servlet.ServletContext,
     * org.springframework.context.ApplicationContext)} will first be searched for the filename, then
     * the context then finally the appPath+FS+filename. if the image is not in imagesDir but is
     * found in one of the other locations it will be copied to imagesDir for future use.
     * <p/>
     * Use case of this copying is the default harvesting logos are shipped with the webapp and when
     * used they are copied to the imagesDir so that the imagesDir contain all relevant images. This
     * allow system administrators to ignore the images in the webapp and only configure/backup
     * images in the logos directory.
     *
     * @param context      a possible null context for trying to find the images dir and the logos
     *                     within the webapp. If null system properties will be used to try and find
     *                     the configuration overrides and appPath will be used to find the logos
     *                     within the webapp
     * @param appPath      fallback if the context is not available
     * @param filename     the file to look up. will normally contains a prefix like logos/ or
     *                     harvesting/
     * @param defaultValue the image to return if unable to find the actual image
     * @return the bytes of the actual image or defaultValue
     */
    public Pair<byte[], Long> loadImage(ServletContext context,
                                               Path appPath, String filename, byte[] defaultValue)
        throws IOException {
        return loadResource(null, context, appPath, filename, defaultValue, -1);
    }

    private Path locateResource(@Nullable Path resourcesDir,
                                ServletContext context, Path appPath, @Nonnull String filename)
        throws IOException {
        if (filename.charAt(0) == '/' || filename.charAt(0) == '\\') {
            filename = filename.substring(1);
        }

        Path file;
        if (resourcesDir != null) {
            file = resourcesDir.resolve(filename);
        } else {
            file = IO.toPath(filename);
        }

        if (!java.nio.file.Files.exists(file)) {
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
            if (java.nio.file.Files.exists(webappCopy)) {
                IO.copyDirectoryOrFile(webappCopy, file, false);
            }

            final String fileName = file.getFileName().toString();
            final int indexOfDot = fileName.lastIndexOf(".");
            final String suffixless = Files.getNameWithoutExtension(fileName);
            final String suffix = Files.getFileExtension(fileName);

            if (!java.nio.file.Files.exists(file) && IMAGE_WRITE_SUFFIXES.contains(suffix.toLowerCase())) {
                // find a different format and convert it to our desired format
                DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {

                    @Override
                    public boolean accept(Path entry) throws IOException {
                        String name = entry.getFileName().toString();
                        boolean startsWith = name.startsWith(suffixless);
                        final String ext = Files.getFileExtension(name).toLowerCase();
                        boolean canReadImage = name.length() > indexOfDot && IMAGE_READ_SUFFIXES.contains(ext);
                        return startsWith && canReadImage;
                    }
                };
                try (DirectoryStream<Path> paths = java.nio.file.Files.newDirectoryStream(file.getParent(), filter)) {
                    Iterator<Path> iter = paths.iterator();
                    if (iter.hasNext()) {
                        Path path = iter.next();
                        try (
                            InputStream in = IO.newInputStream(path);
                            OutputStream out = java.nio.file.Files.newOutputStream(file)
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

    // ---------------------------------------------------------------------------

    /**
     * Copy the filename from icon to logos/destName+extension. The destName should not have the file
     * extension.
     *
     * @param context  a possibly null context for searching for the source icon
     * @param icon     a relative path from images directory ( {@linkplain #locateResourcesDir(ServiceContext)})
     *                 for example harvesting/defaultHarvester.png
     * @param destName the name of the final image (in logos directory) so just the name.
     */
    public void copyLogo(ServiceContext context, String icon,
                                String destName) {
        ServletContext servletContext = null;
        if (context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }
        Path appDir = context.getAppPath();

        Path des = null;
        try {
            Path src = locateResource(
                locateResourcesDir(context), servletContext,
                appDir, icon);

            String extension = Files.getFileExtension(src.getFileName().toString());
            des = locateLogosDir(context).resolve(destName + "." + extension);

            if (java.nio.file.Files.exists(src)) {
                java.nio.file.Files.copy(src, des, REPLACE_EXISTING, NOFOLLOW_LINKS);
            }
        } catch (IOException e) {
            // --- we ignore exceptions here, just log them

            context.warning("Cannot copy icon -> " + e.getMessage());
            context.warning(" (C) Source : " + icon);
            context.warning(" (C) Destin : " + des);
        }
    }

    // ---------------------------------------------------------------------------

    /**
     * copy the "unknown" filename to the logos directory for the name
     *
     * @param context  a possibly null context for searching for the unknown icon
     * @param destName the filename prefix to copy the unknown filename to.
     */
    public void copyUnknownLogo(ServiceContext context, String destName) {
        copyLogo(context, "unknown-filename.png", destName);
    }

    /**
     * List all the files in the provided logosDir (eg. "logos", "harvesting").
     *
     * Searches #locateDataImagesDir(ServiceContext)/logosDir and adds all the files that are
     * found.
     *
     * The search is not recursive.
     *
     * @param context    a possibly null context for searching for the source icon
     * @param logosDir   the directory to search. It should not have the images prefix. it should
     *                   just be the relative name like: "logos" or "harvesting"
     * @param iconFilter the file filter for selecting the files in the listing
     * @return all files in {@linkplain #locateResourcesDir(ServiceContext) /logosDir} that match
     * the iconFitler
     */
    public Set<Path> listFiles(ServiceContext context, String logosDir,
                                      DirectoryStream.Filter<Path> iconFilter) {
        Path dir = locateResourcesDir(context).resolve("images").resolve(logosDir);
        HashSet<Path> result = new HashSet<>();

        addFiles(iconFilter, dir, result);

        return result;
    }

    private void addFiles(DirectoryStream.Filter<Path> iconFilter, Path webappDir, HashSet<Path> result) {

        HashSet<Path> names = new HashSet<>();
        try (DirectoryStream<Path> paths = java.nio.file.Files.newDirectoryStream(webappDir, iconFilter)) {
            for (Path file : paths) {
                if (!names.contains(file.getFileName())) {
                    result.add(file);
                    names.add(file.getFileName());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public FileTime getLastModified(Path resourcesDir, ServletContext context, Path appPath, String filename) throws IOException {
        Path file = locateResource(resourcesDir, context, appPath, filename);
        if (file != null && java.nio.file.Files.exists(file)) {
            return java.nio.file.Files.getLastModifiedTime(file);
        }
        return null;
    }

    public void deleteImageIfExists(final String image, final Path dir) throws IOException {
        Path icon = findImagePath(image, dir);
        if (icon != null) {
            java.nio.file.Files.deleteIfExists(icon);
        }

    }

    public void createImageFromReq(final Path logoDir, final String filename, final XmlRequest req) throws IOException {
        final Path logoFile = logoDir.resolve(filename);
        try {
            req.executeLarge(logoFile);
        } catch (IOException e) {
            IO.deleteFile(logoFile, false, Geonet.GEONETWORK);
            throw e;
        }
    }

    public void copyFile(final Path input, final Path logoDir, final String filename) throws IOException {
        Path output = logoDir.resolve(filename);
        java.nio.file.Files.copy(input, output);

    }

    public interface ResourceHolder extends Closeable {
        Path getPath();
        String getRelativePath();
        FileTime getLastModifiedTime() throws IOException;
    }

    private static class FileResourceHolder implements ResourceHolder {
        private String relativePath;
        private final Path path;

        public FileResourceHolder(final Path basePath, final Path path) {
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
            return java.nio.file.Files.getLastModifiedTime(path);
        }

        @Override
        public void close() {
            // nothing to do
        }
    }
}

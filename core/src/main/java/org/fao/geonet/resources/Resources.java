package org.fao.geonet.resources;

import com.google.common.io.Files;
import jeeves.server.context.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility methods for managing resources that are site dependent. In other words
 * that can be added to or modified by Geonetwork and therefore should not be
 * overwritten if the geonetwork webapp is redeployed. Those resources are usually
 * located in <geonetwork.dir>/resources. htmlcache, images/logos, images/harvesting
 * are the main updated resources.
 * <p/>
 * User: jeichar Date: 1/17/12 Time: 5:51 PM
 */
public class Resources {

    private final static Set<String> IMAGE_READ_SUFFIXES;
    private final static Set<String> IMAGE_WRITE_SUFFIXES;

    static {
        HashSet<String> suffixes = new HashSet<String>();
        for (String string : ImageIO.getReaderFileSuffixes()) {
            suffixes.add(string.toLowerCase());
        }

        IMAGE_READ_SUFFIXES = Collections.unmodifiableSet(suffixes);

        suffixes = new HashSet<String>();
        for (String string : ImageIO.getReaderFileSuffixes()) {
            suffixes.add(string.toLowerCase());
        }

        IMAGE_WRITE_SUFFIXES = Collections.unmodifiableSet(suffixes);
    }

    /**
     * Find the configured directory containing logos. The directory the logos
     * are located in depends on the configuration of dataImagesDir in the
     * config.xml.
     * <p/>
     * (Overrides will be applied so the actual config can be in overrides)
     *
     * @return locateResourcesDir(...) + FS + "images" + FS + "logos"
     */
    public static String locateLogosDir(ServiceContext context) {
        ServletContext servletContext = null;
        if (context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }
        return locateLogosDir(servletContext, context.getApplicationContext(), context.getAppPath());
    }

    /**
     * Find the configured directory containing logos. The directory the logos
     * are located in depends on the configuration of dataImagesDir in the
     * config.xml.
     * <p/>
     * (Overrides will be applied so the actual config can be in overrides)
     *
     * @return locateResourcesDir(...) + FS + "images" + FS + "logos"
     */
    public static String locateLogosDir(ServletContext context, ConfigurableApplicationContext applicationContext, String appDir) {
        String path = (context == null ? appDir : locateResourcesDir(context, applicationContext)) + File.separator
                      + "images" + File.separator + "logos";
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            throw new AssertionError(
                    "Unable to create the images/logos directory. Permissions problem? "
                    + path);
        }
        return path;
    }

    /**
     * Find the configured directory containing harvester logos. The directory
     * the logos are located in depends on the configuration of dataImagesDir in
     * the config.xml.
     * <p/>
     * (Overrides will be applied so the actual config can be in overrides)
     *
     * @return locateResourcesDir(...) + FS + "images" + FS + "harvesting"
     */
    public static String locateHarvesterLogosDir(ServiceContext context) {
        ServletContext servletContext = null;
        if (context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }
        return locateHarvesterLogosDir(servletContext, context.getApplicationContext(), context.getAppPath());
    }

    /**
     * Find the configured directory containing harvester logos. The directory
     * the logos are located in depends on the configuration of dataImagesDir in
     * the config.xml.
     * <p/>
     * (Overrides will be applied so the actual config can be in overrides)
     *
     * @return locateResourcesDir(...) + FS + "images" + FS + "harvesting"
     */
    public static String locateHarvesterLogosDir(ServletContext context,
                                                 ConfigurableApplicationContext applicationContext,
                                                 String appDir) {
        String path = (context == null ? appDir : locateResourcesDir(context, applicationContext)) + File.separator
                      + "images" + File.separator + "harvesting";
        File file = new File(path);
        if (!file.exists() && !file.mkdirs()) {
            throw new AssertionError(
                    "Unable to create the harvester logos directory. Permissions problem? "
                    + path);
        }
        return path;
    }

    /**
     * The root of the "data" images. A Data image is an image that is dependent
     * on the webapplication instance. They are the images that change from
     * installation to installation like logos and harvester logos.
     * <p/>
     * The directory is configured by config.xml and the configuration
     * overrides. See the information on configuration overrides for methods of
     * configuring them.
     *
     * @return the root of data images. Subdirectories such as logos and
     *         harvesting likely contain the actual images
     */
    public static String locateResourcesDir(ServiceContext context) {
        if (context.getServlet() != null) {
            return locateResourcesDir(context.getServlet().getServletContext(), context.getApplicationContext());
        }

        return context.getBean(GeonetworkDataDirectory.class).getResourcesDir().getAbsolutePath();
    }

    /**
     * The root of the "data" images. A Data image is an image that is dependent
     * on the webapplication instance. They are the images that change from
     * installation to installation like logos and harvester logos.
     * <p/>
     * The directory is configured by config.xml and the configuration
     * overrides. See the information on configuration overrides for methods of
     * configuring them.
     *
     * @return the root of data images. Subdirectories such as logos and
     *         harvesting likely contain the actual images
     */
    public static String locateResourcesDir(ServletContext context, ApplicationContext applicationContext) {
        String property = null;
        try {
            property = applicationContext.getBean(GeonetworkDataDirectory.class).getResourcesDir().getPath();
        } catch (NoSuchBeanDefinitionException e) {
            property = context.getRealPath("/WEB-INF/data/resources");
        }

        if (property == null) {
            return "resources";
        } else {
            return property;
        }
    }

    /**
     * Load a data image. The "imagesDir" will first be searched for the logo,
     * then the context then finally the appPath+FS+filename. if the image is
     * not in imagesDir but is found in one of the other locations it will be
     * copied to imagesDir for future use.
     * <p/>
     * Use case of this copying is the default harvesting logos are shipped with
     * the webapp and when used they are copied to the imagesDir so that the
     * imagesDir contain all relevant images. This allow system administrators
     * to ignore the images in the webapp and only configure/backup images in
     * the logos directory.
     *
     * @param resourcesDir Should be {@link #locateResourcesDir(ServiceContext)}. This
     *                     is added as a parameter because it takes time to look up the
     *                     imagesDir. for performance reasons a caller can cache
     *                     imagesDir. If null the imagesDir will be looked up.
     * @param context      the context for finding the image if the image is not in
     *                     imagesDir. (may be null)
     * @param appPath      the fallback if images dir and context cannot be used to
     *                     lookup the imagesDir (or if context is null which is
     *                     permitted)
     * @param filename     the file to look up. will normally contains a prefix like
     *                     logos/ or harvesting/
     * @param defaultValue the image to return if unable to find the actual image
     * @param loadSince    If > -1 then it will be compared to the lastModified of the file.  If it is
     *                     < lastModified then the file will be loaded otherwise <defaultValue,loadSince> will be returned
     * @return the bytes of the actual image or defaultValue and the lastModified timestamp.
     *         Timestamp will be -1 if defaultValue is returned.
     *         If <ul>
     *         <li>an error occurs</li>
     *         <li>file does not exist</li>
     *         <li>loadSince is >= file.lastModified<li>
     *         </ul>
     *         The defaultValue will be returned
     */
    static Pair<byte[], Long> loadResource(String resourcesDir, ServletContext context,
                                           String appPath, String filename, byte[] defaultValue, long loadSince)
            throws IOException {
        File file = locateResource(resourcesDir, context, appPath, filename);

        if (file.exists()) {

            try {
                if (loadSince < 0 || file.lastModified() > loadSince) {
                    ByteArrayOutputStream data = new ByteArrayOutputStream();
                    transferTo(file, data, true);
                    return Pair.read(data.toByteArray(), file.lastModified());
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
     * Load a data image. The "imagesDir" (
     * {@link #locateResourcesDir(javax.servlet.ServletContext, org.springframework.context.ApplicationContext)} will first be
     * searched for the logo, then the context then finally the
     * appPath+FS+filename. if the image is not in imagesDir but is found in one
     * of the other locations it will be copied to imagesDir for future use.
     * <p/>
     * Use case of this copying is the default harvesting logos are shipped with
     * the webapp and when used they are copied to the imagesDir so that the
     * imagesDir contain all relevant images. This allow system administrators
     * to ignore the images in the webapp and only configure/backup images in
     * the logos directory.
     *
     * @param context      a possible null context for trying to find the images dir and
     *                     the logos within the webapp. If null system properties will be
     *                     used to try and find the configuration overrides and appPath
     *                     will be used to find the logos within the webapp
     * @param appPath      fallback if the context is not available
     * @param filename     the file to look up. will normally contains a prefix like
     *                     logos/ or harvesting/
     * @param defaultValue the image to return if unable to find the actual image
     * @return the bytes of the actual image or defaultValue
     */
    public static Pair<byte[], Long> loadImage(ServletContext context, String appPath,
                                               String filename, byte[] defaultValue) throws IOException {
        return loadResource(null, context, appPath, filename, defaultValue, -1);
    }

    public static Pair<byte[], Long> loadImage(ApplicationContext context,
                                               String filename, byte[] defaultValue) throws IOException {
        final GeonetworkDataDirectory gnDataDir = context.getBean(GeonetworkDataDirectory.class);
        String resourceDir = gnDataDir.getResourcesDir().getAbsolutePath();
        String appPath = gnDataDir.getWebappDir();
        return loadResource(resourceDir, null, appPath, filename, defaultValue, -1);
    }

    private static File locateResource(String resourcesDir,
                                       ServletContext context, String appPath, String filename)
            throws IOException {
        File file = new File(resourcesDir, filename);

        if (!file.exists()) {
            File webappCopy = null;
            if (context != null) {
                final String realPath = context.getRealPath(filename);
                if (realPath != null) {
                    webappCopy = new File(realPath);
                }
            }

            if (webappCopy == null) {
                webappCopy = new File(appPath, filename);
            }
            if (webappCopy.exists()) {
                IO.mkdirs(file.getParentFile(), "The resources container directory for the file: " + filename);
                transferTo(webappCopy, new FileOutputStream(file), true);
            }

            final int indexOfDot = file.getName().lastIndexOf(".");
            final String suffixless;
            String suffix;
            if (indexOfDot == -1) {
                suffixless = file.getName();
                suffix = ".png";
            } else {
                suffixless = file.getName().substring(0, indexOfDot);
                suffix = file.getName().substring(indexOfDot + 1);
            }
            if (!file.exists() && IMAGE_WRITE_SUFFIXES.contains(suffix.toLowerCase())) {
                // find a different format and convert it to our desired format
                File[] found = file.getParentFile().listFiles(new FilenameFilter() {

                    @Override
                    public boolean accept(final File arg0, final String name) {
                        boolean startsWith = name.startsWith(suffixless);
                        final String ext = Files.getFileExtension(name).toLowerCase();
                        boolean canReadImage = name.length() > indexOfDot && IMAGE_READ_SUFFIXES.contains(ext);
                        return startsWith && canReadImage;
                    }
                });

                if (found != null && found.length > 0) {
                    BufferedImage image = ImageIO.read(found[0]);
                    try {
                        ImageIO.write(image, suffix, file);
                    } catch (IOException e) {
                        context.log("Unable to convert image from " + found[0] + " to " + file, e);
                    }
                }
            }
        }

        return file;
    }

    private static void transferTo(File path, OutputStream out, boolean close)
            throws IOException {
        final FileInputStream fileInputStream = new FileInputStream(path);
        try {
            fileInputStream.getChannel().transferTo(0, Long.MAX_VALUE,
                    Channels.newChannel(out));
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            if (close)
                IOUtils.closeQuietly(out);
        }
    }

    // ---------------------------------------------------------------------------

    /**
     * Copy the logo from icon to logos/destName+extension. The destName should
     * not have the file extension.
     *
     * @param context  a possibly null context for searching for the source icon
     * @param icon     a relative path from images directory (
     *                 {@linkplain #locateResourcesDir(ServiceContext)}) for example
     *                 harvesting/defaultHarvester.gif
     * @param destName the name of the final image (in logos directory) so just the
     *                 name.
     */
    public static void copyLogo(ServiceContext context, String icon,
                                String destName) {
        ServletContext servletContext = null;
        if (context.getServlet() != null) {
            servletContext = context.getServlet().getServletContext();
        }
        String appDir = context.getAppPath();

        File des = null;
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            File src = Resources.locateResource(
                    Resources.locateResourcesDir(context), servletContext,
                    appDir, icon);

            int extIdx = src.getName().lastIndexOf('.');
            String extension = src.getName().substring(extIdx);
            des = new File(Resources.locateLogosDir(context), destName
                                                              + extension);

            is = new FileInputStream(src);
            os = new FileOutputStream(des);

            BinaryFile.copy(is, os);
        } catch (IOException e) {
            // --- we ignore exceptions here, just log them

            context.warning("Cannot copy icon -> " + e.getMessage());
            context.warning(" (C) Source : " + icon);
            context.warning(" (C) Destin : " + des);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    // ---------------------------------------------------------------------------

    /**
     * copy the "unknown" logo to the logos directory for the name
     *
     * @param context  a possibly null context for searching for the unknown icon
     * @param destName the filename prefix to copy the unknown logo to.
     */
    public static void copyUnknownLogo(ServiceContext context, String destName) {
        copyLogo(context, "unknown-logo.gif", destName);
    }


    /**
     * List all the files in the provided logosDir (eg. "logos", "harvesting").
     * 
     * Searches {@linkplain #locateDataImagesDir(ServiceContext)/logosDir} 
     * and adds all the files that are found.
     * 
     * The search is not recursive.
     * 
     * @param context
     *            a possibly null context for searching for the source icon
     * @param logosDir
     *            the directory to search. It should not have the images prefix.
     *            it should just be the relative name like: "logos" or
     *            "harvesting"
     * @param iconFilter
     *            the file filter for selecting the files in the listing
     * @return all files in {@linkplain #locateResourcesDir(ServiceContext)
     *         /logosDir} that match the iconFitler
     */
    public static Set<File> listFiles(ServiceContext context, String logosDir,
            FileFilter iconFilter) {
        String folderPath = "images"
                + File.separator + logosDir;
        File dir = new File(locateResourcesDir(context), folderPath);
        HashSet<String> names = new HashSet<String>();
        HashSet<File> result = new HashSet<File>();

        addFiles(iconFilter, dir, names, result);

        return result;
    }

    private static void addFiles(FileFilter iconFilter, File webappDir,
                                 HashSet<String> names, HashSet<File> result) {
        File[] files;
        files = webappDir.listFiles(iconFilter);
        if (files != null) {
            for (File file : files) {
                if (!names.contains(file.getName())) {
                    result.add(file);
                    names.add(file.getName());
                }
            }
        }
    }

}

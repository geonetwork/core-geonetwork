//=============================================================================
//===	Copyright (C) 2001-2014 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.thumbnail;

import jeeves.server.context.ServiceContext;
import org.dom4j.DocumentException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.mapfish.print.MapPrinter;
import org.mapfish.print.output.OutputFormat;
import org.mapfish.print.output.PrintParams;
import org.mapfish.print.utils.PJsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import javax.imageio.ImageIO;

/**
 * Use MapFish print module to generate thumbnail.
 *
 * Created by francois on 3/25/14.
 */
public class ThumbnailMaker {
    private static final String LOGGER_NAME = "thumbnail.maker";
    private static final String CONFIG_FILE = "WEB-INF/config-print/print-config.yaml";
    private MapPrinter mapPrinter = null;
    private File configFile = null;
    private String configFilePath = null;
    private long lastModified = Long.MIN_VALUE;

    @Autowired
    private ApplicationContext _applicationContext;

    public void init(ServiceContext context) {
        configFilePath = context.getAppPath() + File.separator + CONFIG_FILE;
        initMapPrinter();
    }

    private void initMapPrinter() {
        long lastModifiedCheck = lastModified;
        if (configFile != null) {
            lastModifiedCheck = configFile.lastModified();
        }

        if (configFile == null || lastModified < lastModifiedCheck) {
            mapPrinter = _applicationContext.getBean(MapPrinter.class);
            configFile = new File(configFilePath);
            if (Log.isDebugEnabled(LOGGER_NAME)) {
                Log.debug(LOGGER_NAME, "Loading or reloading configuration file: " +
                    configFile.getAbsolutePath());
            }

            try {
                mapPrinter.setYamlConfigFile(configFile);

                if (Log.isDebugEnabled(LOGGER_NAME)) {
                    Log.debug(LOGGER_NAME, "Print module configuration: " +
                            mapPrinter.getConfig().toString());
                }

                lastModified = lastModifiedCheck;
            } catch (FileNotFoundException e) {
                Log.error(LOGGER_NAME, "Thumbnail maker configuration file " +
                configFilePath + " not found. Error is " + e.getMessage());
            }
        }
    }

    private MapPrinter getMapPrinter() {
        return mapPrinter;
    }

    public Path generateThumbnail(String jsonConfig, Integer rotationAngle)
            throws IOException, DocumentException, com.itextpdf.text.DocumentException {

        PJsonObject specJson = MapPrinter.parseSpec(jsonConfig);
        if (Log.isDebugEnabled(LOGGER_NAME)) {
            Log.debug(LOGGER_NAME, "Generating thumbnail from config: " + jsonConfig);
        }
        final OutputFormat outputFormat = getMapPrinter().getOutputFormat(specJson);

        Path tempFile = Files.createTempFile("thumbnail", "." + outputFormat.getFileSuffix());

        try (OutputStream out = Files.newOutputStream(tempFile)) {
            PrintParams params = new PrintParams(
                    getMapPrinter().getConfig(),
                    configFile.getParentFile(),
                    specJson,
                    out,
                    new HashMap<String, String>());
            outputFormat.print(params);
        } catch (InterruptedException e) {
            Log.error(Geonet.GEONETWORK, "Error creating a thumbnail", e);
        }

        if (rotationAngle != null) {
            rotate(tempFile, outputFormat.getFileSuffix(), rotationAngle);
        }
        return tempFile;
    }

    public static void rotate(Path imageFile, String extension, int angle) {
        BufferedImage originalImage;
        try {
            originalImage = readImage(imageFile);
            BufferedImage rotatedImage = rotate(originalImage, angle);
            writeImage(rotatedImage, imageFile, extension);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static BufferedImage readImage(Path fileLocation) throws IOException {
        BufferedImage img;
        try (InputStream in = Files.newInputStream(fileLocation)) {
            img = ImageIO.read(in);
        }
        return img;
    }
    public static void writeImage(BufferedImage img, Path fileLocation, String extension) throws IOException {
        try (OutputStream out = Files.newOutputStream(fileLocation)) {
            ImageIO.write(img, extension, out);
        }
    }

    /**
     * Rotate an image
     *
     * @param image
     * @param angle Angle in degree
     * @return
     */
    public static BufferedImage rotate(BufferedImage image, double angle) {
        angle = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
        int w = image.getWidth(), h = image.getHeight();
        int neww = (int)Math.floor(w*cos+h*sin), newh = (int)Math.floor(h*cos+w*sin);
        BufferedImage result = new BufferedImage(neww, newh, Transparency.TRANSLUCENT);
        Graphics2D g = result.createGraphics();
        g.translate((neww-w)/2, (newh-h)/2);
        g.rotate(angle, w/2.0, h/2.0);
        g.drawRenderedImage(image, null);
        g.dispose();
        return result;
    }
}

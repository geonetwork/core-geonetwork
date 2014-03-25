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

import com.lowagie.text.DocumentException;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.utils.Log;
import org.mapfish.print.MapPrinter;
import org.mapfish.print.output.PrintParams;
import org.mapfish.print.output.OutputFormat;
import org.mapfish.print.utils.PJsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

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

    public File generateThumbnail(String jsonConfig)
            throws IOException, DocumentException {

        PJsonObject specJson = MapPrinter.parseSpec(jsonConfig);
        if (Log.isDebugEnabled(LOGGER_NAME)) {
            Log.debug(LOGGER_NAME, "Generating thumbnail from config: " +
                jsonConfig);
        }
        final OutputFormat outputFormat =
                getMapPrinter().getOutputFormat(specJson);

        File tempFile = File.createTempFile("thumbnail",
                "." + outputFormat.getFileSuffix());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
            PrintParams params = new PrintParams(
                    getMapPrinter().getConfig(),
                    configFile.getParentFile(),
                    specJson,
                    out,
                    new HashMap<String, String>());

            outputFormat.print(params);
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return tempFile;
    }
}

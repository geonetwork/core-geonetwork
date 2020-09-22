//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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

package org.fao.geonet.services.logo;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Util;
import org.fao.geonet.api.records.formatters.Resource;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.BadParameterEx;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.resources.Resources;
import org.fao.geonet.utils.FilePathChecker;
import org.fao.geonet.utils.IO;
import org.jdom.Element;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

/**
 * Set the logo of the current node.
 *
 * @author francois
 */
@Deprecated
public class Set implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        Resources resources = context.getBean(Resources.class);
        Path harvestingLogoDirectory = resources.locateHarvesterLogosDir(context);
        Path nodeLogoDirectory = resources.locateLogosDir(context);

        String file = Util.getParam(params, Params.FNAME);
        String asFavicon = Util.getParam(params, Params.FAVICON, "0");

        FilePathChecker.verify(file);

        if (StringUtils.isEmpty(file)) {
            throw new Exception("Logo name is not defined.");
        }

        SettingManager settingMan = context.getBean(SettingManager.class);
        String nodeUuid = settingMan.getSiteId();

        try (Resources.ResourceHolder logoResource = resources.getImage(context, file, harvestingLogoDirectory)) {
            final Path logoFilePath;
            if (logoResource == null) {
                logoFilePath = context.getAppPath().resolve("images/harvesting/" + file);
            } else {
                logoFilePath = logoResource.getPath();
            }
            try (InputStream inputStream = Files.newInputStream(logoFilePath)) {
                BufferedImage source = ImageIO.read(inputStream);

                if ("1".equals(asFavicon)) {
                    try (Resources.ResourceHolder logo = resources.getWritableImage(context, "favicon.png", nodeLogoDirectory)) {
                        createFavicon(source, logo.getPath());
                    }
                } else {
                    try (Resources.ResourceHolder logo = resources.getWritableImage(context, nodeUuid + ".png", nodeLogoDirectory);
                         Resources.ResourceHolder defaultLogo = resources.getWritableImage(context, "logo.png", nodeLogoDirectory)) {

                        if (!file.endsWith(".png")) {
                            try (OutputStream logoOut = Files.newOutputStream(logo.getPath());
                                 OutputStream defLogoOut = Files.newOutputStream(defaultLogo.getPath())) {
                                ImageIO.write(source, "png", logoOut);
                                ImageIO.write(source, "png", defLogoOut);
                            }
                        } else {
                            Files.copy(logoFilePath, logo.getPath(), StandardCopyOption.REPLACE_EXISTING);
                            Files.copy(logoFilePath, defaultLogo.getPath(), StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(
                "Unable to move uploaded thumbnail to destination directory. Error: " + e.getMessage());
        }

        Element response = new Element("response");
        response.addContent(new Element("status").setText("Logo set."));
        return response;
    }

    private void createFavicon(Image img, Path outFile) throws IOException {
        int width = 32;
        int height = 32;
        String type = "png";

        Image thumb = img.getScaledInstance(width, height,
            BufferedImage.SCALE_SMOOTH);

        BufferedImage bimg = new BufferedImage(width, height,
            BufferedImage.TRANSLUCENT);

        Graphics2D g = bimg.createGraphics();
        g.drawImage(thumb, 0, 0, null);
        g.dispose();

        try (OutputStream out = Files.newOutputStream(outFile)) {
            ImageIO.write(bimg, type, out);
        }
    }
}

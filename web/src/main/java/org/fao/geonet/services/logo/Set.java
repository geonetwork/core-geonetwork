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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import jeeves.exceptions.BadParameterEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

/**
 * Set the logo of the current node.
 * 
 * @author francois
 * 
 */
public class Set implements Service {
    private String harvestingLogoDirectory;
    private volatile String nodeLogoDirectory = null;

    public void init(String appPath, ServiceConfig params) throws Exception {
    }

    public Element exec(Element params, ServiceContext context)
            throws Exception {
        synchronized (this) {
            if(harvestingLogoDirectory == null) {
                harvestingLogoDirectory = Resources.locateHarvesterLogosDir(context);
            }
            if(nodeLogoDirectory == null) {
                nodeLogoDirectory = Resources.locateLogosDir(context);
            }
        }
        String file = Util.getParam(params, Params.FNAME);
        String asFavicon = Util.getParam(params, Params.FAVICON, "0");

        if (file.contains("..")) {
            throw new BadParameterEx(
                    "Invalid character found in resource name.", file);
        }

        if ("".equals(file)) {
            throw new Exception("Logo name is not defined.");
        }

        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager settingMan = gc.getSettingManager();
        String nodeUuid = settingMan.getValue("system/site/siteId");

        try {
        	String logoFilePath = harvestingLogoDirectory + File.separator + file;
        	File logoFile = new File(logoFilePath);
        	if (!logoFile.exists()) {
        		logoFilePath = context.getAppPath() + "images" + File.separator + "harvesting" + File.separator + file;
        		logoFile = new File(logoFilePath);
        	}
            BufferedImage source = ImageIO.read(logoFile);

            if ("1".equals(asFavicon)) {
                createFavicon(source, nodeLogoDirectory + File.separator + "favicon.gif");
            } else {
                String logo = nodeLogoDirectory + File.separator + nodeUuid + ".gif";
                String defaultLogo = nodeLogoDirectory + File.separator + "logo.gif";
    
                if (!file.endsWith(".gif")) {
                    ImageIO.write(source, "gif", new File(logo));
                    ImageIO.write(source, "gif", new File(defaultLogo));
                } else {
                    copyLogo(logoFilePath, logo);
                    copyLogo(logoFilePath, defaultLogo);
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

    private void copyLogo(String file, String logo) throws IOException {
        FileInputStream is = new FileInputStream(file);
        FileOutputStream os = new FileOutputStream(logo);
        BinaryFile.copy(is, os, true, true);
    }

    private void createFavicon(Image img, String outFile) throws IOException {
        int width = 16;
        int height = 16;
        String type = "gif";

        Image thumb = img.getScaledInstance(width, height,
                BufferedImage.SCALE_SMOOTH);

        BufferedImage bimg = new BufferedImage(width, height,
                BufferedImage.TRANSLUCENT);

        Graphics2D g = bimg.createGraphics();
        g.drawImage(thumb, 0, 0, null);
        g.dispose();

        ImageIO.write(bimg, type, new File(outFile));
    }
}
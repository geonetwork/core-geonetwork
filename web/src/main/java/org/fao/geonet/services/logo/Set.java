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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

/**
 * Set the logo of the current node.
 * 
 * @author francois
 * 
 */
public class Set implements Service {
	private String harvestingLogoDirectory;
	private String nodeLogoDirectory;

	public void init(String appPath, ServiceConfig params) throws Exception {
		harvestingLogoDirectory = appPath + "images/harvesting/";
		nodeLogoDirectory = appPath + "images/logos/";
	}

	public Element exec(Element params, ServiceContext context)
			throws Exception {
		String file = Util.getParam(params, Params.FNAME);

		if ("".equals(file)) {
			throw new Exception("Logo name is not defined.");
		}

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager settingMan = gc.getSettingManager();
		String nodeUuid = settingMan.getValue("system/site/siteId");

		try {
			String logo = nodeLogoDirectory + "/" + nodeUuid + ".gif";
			if (file.endsWith(".png")) {
				BufferedImage source = ImageIO.read(new File(
						harvestingLogoDirectory + file));
				ImageIO.write(source, "gif", new File(logo));
			} else {

				FileInputStream is = new FileInputStream(
						harvestingLogoDirectory + file);
				FileOutputStream os = new FileOutputStream(logo);
				BinaryFile.copy(is, os, true, true);
			}

		} catch (Exception e) {
			throw new Exception(
					"Unable to move uploaded thumbnail to destination directory");
		}

		Element response = new Element("response");
		response.addContent(new Element("status").setText("Logo set."));
		return response;
	}
}
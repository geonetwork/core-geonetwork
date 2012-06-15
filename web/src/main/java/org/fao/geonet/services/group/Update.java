//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.group;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;

import javax.imageio.ImageIO;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.resources.Resources;
import org.jdom.Element;

//=============================================================================

/** Update the information of a group
  */

public class Update implements Service
{
	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String id    = params.getChildText(Params.ID);
		String name  = Util.getParam(params, Params.NAME);
		String descr = Util.getParam(params, Params.DESCRIPTION, "");
		String email = params.getChildText(Params.EMAIL);
		String website = params.getChildText("website");
		if (website != null && website.length() > 0 && !website.startsWith("http://")) {
            website = "http://" + website;
        }

        // Logo management ported/adapted from GeoNovum GeoNetwork app.
        // Original devs: Heikki Doeleman and Thijs Brentjens
        String logoFile = params.getChildText("logofile");
        String logoUUID = null;
        if (logoFile != null && logoFile.length() > 0) {
            // logo uploaded

            // IE returns complete path of file, while FF only the name (strip path for IE)
            logoFile = stripPath(logoFile);
            
            BufferedImage bufferedImage = ImageIO.read(new File(context.getUploadDir(), logoFile));
            // scale to 40px width
            int imgWidth = bufferedImage.getWidth();
            int imgHeight = bufferedImage.getHeight();
            int hardcodedWidth = 40;
            int scaledHeight = hardcodedWidth * imgHeight / imgWidth;
            Image thumb = bufferedImage.getScaledInstance(hardcodedWidth, scaledHeight, BufferedImage.SCALE_SMOOTH);
            BufferedImage bimg = new BufferedImage(hardcodedWidth, scaledHeight, BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = bimg.createGraphics();
            g.drawImage(thumb, 0, 0, null);
            g.dispose();
            String logoDir = Resources.locateLogosDir(context);
            logoUUID = UUID.randomUUID().toString();
            ImageIO.write(bimg, "png", new File(logoDir, logoUUID + ".png"));
        }

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		Element elRes = new Element(Jeeves.Elem.RESPONSE);

		if (id == null)	// For Adding new group
		{
			int newId = context.getSerialFactory().getSerial(dbms, "Groups");

			String query = "INSERT INTO Groups(id, name, description, email, website, logoUuid) VALUES (?, ?, ?, ?, ?, ?)";

            dbms.execute(query, newId, name, descr, email, website, logoUUID);

			elRes.addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.ADDED));
		}
		else 	//--- For Update
		{
			String query = "UPDATE Groups SET name=?, description=?, email=? WHERE id=?";

			dbms.execute(query, name, descr, email, new Integer(id));

			elRes.addContent(new Element(Jeeves.Elem.OPERATION).setText(Jeeves.Text.UPDATED));
		}

		return elRes;
	}
	
    private String stripPath(String file) {
        if (file.indexOf('\\') > 0) {
            String[] pathTokens = file.split("\\\\");
            file = pathTokens[pathTokens.length-1];
        }

        return file;
    }
}

//=============================================================================


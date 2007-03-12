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

package org.fao.geonet.services.thumbnail;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Util;
import lizard.tiff.Tiff;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.ConcurrentUpdateEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

//=============================================================================

public class Set implements Service
{
	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		String  id            = Util.getParam    (params, Params.ID);
		String  type          = Util.getParam    (params, Params.TYPE);
		String  version       = Util.getParam    (params, Params.VERSION);
		String  file          = Util.getParam    (params, Params.FNAME);
		String  scalingDir    = Util.getParam    (params, Params.SCALING_DIR);
		int     scalingFactor = Util.getParamInt (params, Params.SCALING_FACTOR);
		boolean scaling       = Util.getParamBool(params, Params.SCALING, false);

		boolean createSmall        = Util.getParamBool(params, Params.CREATE_SMALL, false);
		String  smallScalingDir    = Util.getParam    (params, Params.SMALL_SCALING_DIR,   "");
		int     smallScalingFactor = Util.getParamInt (params, Params.SMALL_SCALING_FACTOR, 0);

		Lib.resource.checkPrivilege(context, id, AccessManager.OPER_EDIT);

		//-----------------------------------------------------------------------
		//--- environment vars

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		//--- check if the metadata has been modified from last time

		if (version != null && !dataMan.getVersion(id).equals(version))
			throw new ConcurrentUpdateEx(id);

		//-----------------------------------------------------------------------
		//--- create destination directory

		String dataDir = Lib.resource.getDir(context, Params.Access.PUBLIC, id);

		new File(dataDir).mkdirs();

		//-----------------------------------------------------------------------
		//--- create the small thumbnail, removing the old one

		if (createSmall)
		{
			String smallFile = getFileName(file, true);
			String inFile    = context.getUploadDir() + file;
			String outFile   = dataDir + smallFile;

			removeOldThumbnail(context, id, "small");
			createThumbnail(inFile, outFile, smallScalingFactor, smallScalingDir);
			dataMan.setThumbnail(dbms, id, true, smallFile);
		}

		//-----------------------------------------------------------------------
		//--- create the requested thumbnail, removing the old one

		removeOldThumbnail(context, id, type);

		if (scaling)
		{
			String newFile = getFileName(file, type.equals("small"));
			String inFile  = context.getUploadDir() + file;
			String outFile = dataDir + newFile;

			createThumbnail(inFile, outFile, scalingFactor, scalingDir);

			if (!new File(inFile).delete())
				context.error("Error while deleting thumbnail : "+inFile);

			dataMan.setThumbnail(dbms, id, type.equals("small"), newFile);
		}
		else
		{
			//--- move uploaded file to destination directory

			File inFile  = new File(context.getUploadDir(), file);
			File outFile = new File(dataDir,                file);

			if (!inFile.renameTo(outFile))
			{
				inFile.delete();
				throw new Exception("unable to move uploaded thumbnail to destination directory");
			}

			dataMan.setThumbnail(dbms, id, type.equals("small"), file);
		}

		//-----------------------------------------------------------------------

		Element response = new Element("a");
		response.addContent(new Element("id").setText(id));
		response.addContent(new Element("version").setText(dataMan.getNewVersion(id)));

		return response;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private void removeOldThumbnail(ServiceContext context, String id, String type) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getDataManager();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		Element result = dataMan.getThumbnails(dbms, id);

		if (result == null)
			throw new IllegalArgumentException("Metadata not found --> " + id);

		result = result.getChild(type);

		//--- if there is no thumbnail, we return

		if (result == null)
			return;

		//-----------------------------------------------------------------------
		//--- remove thumbnail

		dataMan.unsetThumbnail(dbms, id, type.equals("small"));

		//--- remove file

		String file = Lib.resource.getDir(context, Params.Access.PUBLIC, id) + result.getText();

		if (!new File(file).delete())
			context.error("Error while deleting thumbnail : "+file);
	}

	//--------------------------------------------------------------------------

	private void createThumbnail(String inFile, String outFile, int scalingFactor,
										  String scalingDir) throws IOException
	{
		BufferedImage origImg = getImage(inFile);

		int imgWidth  = origImg.getWidth();
		int imgHeight = origImg.getHeight();

		int width;
		int height;

		if (scalingDir.equals("width"))
		{
			width  = scalingFactor;
			height = width * imgHeight / imgWidth;
		}
		else
		{
			height = scalingFactor;
			width  = height * imgWidth / imgHeight;
		}

		Image thumb = origImg.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);

		BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

		Graphics2D g = bimg.createGraphics();
		g.drawImage(thumb, 0, 0, null);
		g.dispose();

		ImageIO.write(bimg, IMAGE_TYPE, new File(outFile));
	}

	//--------------------------------------------------------------------------

	private String getFileName(String file, boolean small)
	{
		int pos = file.lastIndexOf(".");

		if (pos != -1)
			file = file.substring(0, pos);

		return small 	? file + SMALL_SUFFIX +"."+ IMAGE_TYPE
							: file +"."+ IMAGE_TYPE;
	}

	//--------------------------------------------------------------------------

	public BufferedImage getImage(String inFile) throws IOException
	{
		String lcFile = inFile.toLowerCase();

		if (lcFile.endsWith(".tif") || lcFile.endsWith(".tiff"))
		{
			//--- load the TIFF/GEOTIFF file format

			Image image = getTiffImage(inFile);

			int width = image.getWidth(null);
			int height= image.getHeight(null);

			BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = bimg.createGraphics();
			g.drawImage(image, 0,0, null);
			g.dispose();

			return bimg;
		}

		return ImageIO.read(new File(inFile));
	}

	//--------------------------------------------------------------------------

	private Image getTiffImage(String inFile) throws IOException
	{
		Tiff t = new Tiff();
		t.readInputStream(new FileInputStream(inFile));

		if (t.getPageCount() == 0)
			throw new IOException("No images inside TIFF file");

		return t.getImage(0);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private static final String IMAGE_TYPE   = "png";
	private static final String SMALL_SUFFIX = "_s";

}

//=============================================================================



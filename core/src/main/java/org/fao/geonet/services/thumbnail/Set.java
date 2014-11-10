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

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import lizard.tiff.Tiff;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.ConcurrentUpdateEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.NotInReadOnlyModeService;
import org.fao.geonet.utils.IO;
import org.jdom.Element;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

public class Set extends NotInReadOnlyModeService {
    private static final ImageObserver IMAGE_OBSERVER = new ImageObserver() {
        @Override
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
            return false;
        }
    };
    //--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(Path appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element serviceSpecificExec(Element params, ServiceContext context) throws Exception
	{
		String  id            = Util.getParam     (params, Params.ID);
		String  type          = Util.getParam     (params, Params.TYPE);
		String  version       = Util.getParam     (params, Params.VERSION);
		String  file          = Util.getParam     (params, Params.FNAME);
		String  scalingDir    = Util.getParam     (params, Params.SCALING_DIR);
		boolean scaling       = Util.getParam     (params, Params.SCALING, false);
		int     scalingFactor = Util.getParamAsInt(params, Params.SCALING_FACTOR);

		boolean createSmall        = Util.getParam(params, Params.CREATE_SMALL,        false);
		String  smallScalingDir    = Util.getParam(params, Params.SMALL_SCALING_DIR,   "");
		int     smallScalingFactor = Util.getParam(params, Params.SMALL_SCALING_FACTOR, 0);

		Lib.resource.checkEditPrivilege(context, id);

		//-----------------------------------------------------------------------
		//--- environment vars

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getBean(DataManager.class);

		//--- check if the metadata has been modified from last time

		if (version != null && !dataMan.getVersion(id).equals(version))
			throw new ConcurrentUpdateEx(id);

        boolean imageExists = testValidImage(getFileName(file, true), false);
        imageExists |= testValidImage(getFileName(file, false), false);
        imageExists |= testValidImage(context.getUploadDir().resolve(file), false);

        if (!imageExists) {
            throw new IllegalArgumentException("No image file uploaded");
        }

        //-----------------------------------------------------------------------
		//--- create destination directory

		Path dataDir = Lib.resource.getDir(context, Params.Access.PUBLIC, id);

        Files.createDirectories(dataDir);

		//-----------------------------------------------------------------------
		//--- create the small thumbnail, removing the old one

		if (createSmall)
		{
			Path smallFile = getFileName(file, true);
			Path inFile    = context.getUploadDir().resolve(file);
            Path outFile   = dataDir.resolve(smallFile);

			removeOldThumbnail(context, id, "small", false);
			createThumbnail(inFile, outFile, smallScalingFactor, smallScalingDir);
			dataMan.setThumbnail(context, id, true, smallFile.toString(), false);
		}

		//-----------------------------------------------------------------------
		//--- create the requested thumbnail, removing the old one

		removeOldThumbnail(context, id, type, false);

		if (scaling)
		{
			Path newFile = getFileName(file, type.equals("small"));
			Path inFile  = context.getUploadDir().resolve(file);
			Path outFile = dataDir.resolve(newFile);

			createThumbnail(inFile, outFile, scalingFactor, scalingDir);

            try {
                Files.delete(inFile);
            } catch (IOException e) {
                context.error("Error while deleting thumbnail : "+inFile);
            }

			dataMan.setThumbnail(context, id, type.equals("small"), newFile.toString(), false);
		}
		else
		{
			//--- move uploaded file to destination directory
            final Path outPath = context.getUploadDir().resolve(file);
            Files.move(outPath, dataDir.resolve(file), StandardCopyOption.REPLACE_EXISTING);

			dataMan.setThumbnail(context, id, type.equals("small"), file, false);
		}

        dataMan.indexMetadata(id, false);
        //-----------------------------------------------------------------------

		Element response = new Element("a");
		response.addContent(new Element("id").setText(id));
		response.addContent(new Element("version").setText(dataMan.getNewVersion(id)));

		return response;
	}

    private boolean testValidImage(Path inFile, boolean mustExistAndBeValid) throws IOException {
        if (inFile != null && Files.exists(inFile)) {
            // Test that file is an image before removing old files.
            getImage(inFile);
            return true;
        } else {
            if (mustExistAndBeValid) {
                throw new IllegalArgumentException(inFile + ": expected but does not exist");
            }
            return false;
        }
    }
    /**
     * TODO javadoc.
     *
     * @param id
     * @param context
     */
    private Path createDataDir(String id, ServiceContext context) {
        Path dataDir = Lib.resource.getDir(context, Params.Access.PUBLIC, id);
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            context.error("Failed to make dir: " + dataDir);
        }
        return dataDir;
    }

	// FIXME : not elegant
	public Element execOnHarvest(
							Element params, 
							ServiceContext context, 
							DataManager dataMan) throws Exception {

		String  id            = Util.getParam     (params, Params.ID);
        Path dataDir = createDataDir(id, context);
		
		//-----------------------------------------------------------------------
		//--- create the small thumbnail, removing the old one
        boolean createSmall        = Util.getParam(params, Params.CREATE_SMALL,        false);
        String  file          = Util.getParam     (params, Params.FNAME);
        String  scalingDir    = Util.getParam     (params, Params.SCALING_DIR, "width");
        boolean scaling       = Util.getParam     (params, Params.SCALING, false);
        int     scalingFactor = Util.getParam     (params, Params.SCALING_FACTOR, 1);
        String  type          = Util.getParam     (params, Params.TYPE);
//        String  version       = Util.getParam     (params, Params.VERSION);

        boolean imageExists = testValidImage(getFileName(file, true), false);
        imageExists |= testValidImage(getFileName(file, false), false);
        imageExists |= testValidImage(context.getUploadDir().resolve(file), false);

        if (!imageExists) {
            throw new IllegalArgumentException("No image file uploaded");
        }

        if (createSmall) {
			Path smallFile = getFileName(file, true);
			Path inFile    = context.getUploadDir().resolve(file);
			Path outFile   = dataDir.resolve(smallFile);

            String  smallScalingDir    = Util.getParam(params, Params.SMALL_SCALING_DIR,   "");
            int     smallScalingFactor = Util.getParam(params, Params.SMALL_SCALING_FACTOR, 0);
			// FIXME should be done before removeOldThumbnail(context, dbms, id, "small");
			createThumbnail(inFile, outFile, smallScalingFactor, smallScalingDir);
            dataMan.setThumbnail(context, id, true, smallFile.toString(), false);
       }

		//-----------------------------------------------------------------------
		//--- create the requested thumbnail, removing the old one
        removeOldThumbnail(context, id, type, false);
        saveThumbnail(scaling, file, type, dataDir, scalingDir, scalingFactor, dataMan, id, context);

		//-----------------------------------------------------------------------
        dataMan.indexMetadata(id, false);
        Element response = new Element("Response");
		response.addContent(new Element("id").setText(id));
		// NOT NEEDEDresponse.addContent(new Element("version").setText(dataMan.getNewVersion(id)));

		return response;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

    private void saveThumbnail(boolean scaling, String file, String type, Path dataDir, String scalingDir,
                               int scalingFactor, DataManager dataMan, String id, ServiceContext context) throws Exception {
            if (scaling) {
                Path newFile = getFileName(file, type.equals("small"));
                Path inFile  = context.getUploadDir().resolve(file);
                Path outFile = dataDir.resolve(newFile);
                
                createThumbnail(inFile, outFile, scalingFactor, scalingDir);

                try {
                    Files.delete(inFile);
                } catch (IOException e) {
                    context.error("Error while deleting thumbnail : " + inFile);
                }

                dataMan.setThumbnail(context, id, type.equals("small"), newFile.toString(), false);
            } else {
                //--- move uploaded file to destination directory
                Path inFile  = context.getUploadDir().resolve(file);
                Path outFile = dataDir.resolve(file);

                try {
                    Files.move(inFile, outFile);
                } catch (Exception e) {
                    IO.deleteFileOrDirectory(inFile);
                    throw new Exception("Unable to move uploaded thumbnail to destination: " + outFile + ". Error: " + e.getMessage());
                }
			
                dataMan.setThumbnail(context, id, type.equals("small"), file, false);
            }
        }

	private void removeOldThumbnail(ServiceContext context, String id, String type, boolean indexAfterChange) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

		DataManager dataMan = gc.getBean(DataManager.class);

		Element result = dataMan.getThumbnails(context, id);

		if (result == null)
			throw new IllegalArgumentException("Metadata not found --> " + id);

		result = result.getChild(type);

		//--- if there is no thumbnail, we return

		if (result == null)
			return;

		//-----------------------------------------------------------------------
		//--- remove thumbnail

		dataMan.unsetThumbnail(context, id, type.equals("small"), indexAfterChange);

		//--- remove file

		String file = Lib.resource.getDir(context, Params.Access.PUBLIC, id) + getFileName(result.getText());
		if (!new File(file).delete())
			context.error("Error while deleting thumbnail : "+file);
	}

	//--------------------------------------------------------------------------

	private void createThumbnail(Path inFile, Path outFile, int scalingFactor,
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

		BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);

		Graphics2D g = bimg.createGraphics();
		g.drawImage(thumb, 0, 0, null);
		g.dispose();

        try (OutputStream out = Files.newOutputStream(outFile)) {
            ImageIO.write(bimg, IMAGE_TYPE, out);
        }
	}

	//--------------------------------------------------------------------------

	private Path getFileName(String file, boolean small)
	{
		int pos = file.lastIndexOf('.');

		if (pos != -1) {
            file = file.substring(0, pos);
        }

        final String path;
        if (small) {
            path = file + SMALL_SUFFIX + "." + IMAGE_TYPE;
        } else {
            path = file + "." + IMAGE_TYPE;
        }

        return IO.toPath(path);
	}

	//--------------------------------------------------------------------------
    @Nonnull
	public BufferedImage getImage(Path inFile) throws IOException
	{
		String lcFile = inFile.getFileName().toString().toLowerCase();

		if (lcFile.endsWith(".tif") || lcFile.endsWith(".tiff"))
		{
			//--- load the TIFF/GEOTIFF file format

			Image image = getTiffImage(inFile);

			int width = image.getWidth(IMAGE_OBSERVER);
			int height= image.getHeight(IMAGE_OBSERVER);

			BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g = bimg.createGraphics();
			g.drawImage(image, 0,0, null);
			g.dispose();

			return bimg;
		}
        try (InputStream in = Files.newInputStream(inFile)) {
            return ImageIO.read(in);
        }
	}

	//--------------------------------------------------------------------------

	private Image getTiffImage(Path inFile) throws IOException
	{
	    try (InputStream fin = Files.newInputStream(inFile)) {
    		Tiff t = new Tiff();
            t.readInputStream(fin);
    
    		if (t.getPageCount() == 0)
    			throw new IOException("No images inside TIFF file");
    
    		return t.getImage(0);
	    }
	}

	/**
	 * Return file name from full url thumbnail formated as
	 * http://wwwmyCatalogue.com:8080/srv/eng/resources.get?uuid=34baff6e-3880-4589-a5e9-4aa376ecd2a5&fname=snapshot3.png
	 * @param file
	 * @return
	 */
	private String getFileName(String file)
	{
		if(file.indexOf(FNAME_PARAM) < 0) {
			return file;
		}
		else {
			return file.substring(file.lastIndexOf(FNAME_PARAM)+FNAME_PARAM.length());
		}
	}
	
	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private static final String IMAGE_TYPE   = "png";
	private static final String SMALL_SUFFIX = "_s";
	private static final String FNAME_PARAM   = "fname=";

}

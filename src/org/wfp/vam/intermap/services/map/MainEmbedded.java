//==============================================================================
package org.wfp.vam.intermap.services.map;

import java.util.List;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.DefaultMapServers;
import org.wfp.vam.intermap.kernel.map.MapMerger;

/**
 * Very similar to Main.
 * This class is used to initialize IM when embedded in GN.
 * We force some inits, such as initial layer, if not present.
 *
 * @author ETj
 */

public class MainEmbedded implements Service
{
	public void init(String appPath, ServiceConfig config) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);

		boolean doReset = params.getChildText("reset") != null;
		if( doReset )
		{
			mm = new MapMerger();
			context.getUserSession().setProperty(Constants.SESSION_MAP, mm);
		}

		// Add default context if none exists
		if (mm.size() == 0) // No layers to merge
		{
			System.out.println("MainEmbedded: SETTING DEFAULT CONTEXT");
			setDefaultContext(mm, context);
		}

		Element minimap = buildMiniMap(mm);
		Element oldmap  = buildMap(mm, context); // dont know how/where these info are used
		Element bigmap  = getBigMapInfo(mm, context); // pls see method's javadoc

		return new Element("response")
			.addContent(new Element("status")                 // Always not empty
								.setAttribute("empty", "false"))
			.addContent(new Element("layersRoot")
								.addContent(layers(params, context)))
			.addContent(minimap)
			.addContent(bigmap)
			.addContent(oldmap);
	}

	/**
	 * Method buildMap
	 *
	 * @see getBigMapInfo(MapMerger mm, ServiceContext context)
	 */
	private Element buildMap(MapMerger mm, ServiceContext context) throws Exception
	{
		int width  = MapUtil.getImageWidth(context);
		int height = MapUtil.getImageHeight(context);

		// Merge the images now, because errors in merging have to be reported
		// in the layers frame
		String imagename = mm.merge(width, height);
		String url = MapUtil.getTempUrl() + "/" + imagename;

		String tool = MapUtil.getTool(context);

		return new Element("mapRoot")
		   .addContent(new Element("response") // too many levels: preserving external references backcompatibility
				.addContent(new Element(Constants.URL).setText(url))
				.addContent(new Element("tool").setText(tool))
				.addContent(mm.toElement())
				.addContent(new Element("imageWidth").setText(width  + ""))
				.addContent(new Element("imageHeight").setText(height + ""))
				.addContent(new Element("imageSize")
								.setText((String)context.getUserSession().getProperty(Constants.SESSION_SIZE))));
	}

	/**
	 * This method is used to return some info used by the javascript code,
	 * and is similar to the minimap one.
	 * The info returned in mapRoot are far too complex to be parsed there,
	 * so here we'll include only what we really need client-side.
	 * <BR>Please also consider that a lot of refactorization has been made in order
	 * to enable IM to handle more than one map at time, so that now static info (w, h, bb)
	 * are going to be passed from server to client and back again.
	 * We can retrieve these info because the big map has just been generated,
	 * but keep an eye on the order in which the various methods are called.
	 * <BR>
	 * FIXME: we expect to reorganize (at least optimize) the info returned in the mapRoot element in buildMap();
	 *
	 * @author ETj
	 */
	private Element getBigMapInfo(MapMerger mm, ServiceContext context)
	{
		int width  = MapUtil.getImageWidth(context);
		int height = MapUtil.getImageHeight(context);

		String imagename = mm.getImageName();
		String url = MapUtil.getTempUrl() + "/" + imagename;

		return new Element("bigmap")
			.addContent(new Element("imgUrl").setText(url))
			.addContent(mm.getBoundingBox().toElement()) // "extent"
			.addContent(new Element("width").setText(""+width))
			.addContent(new Element("height").setText(""+height));
	}

	/**
	 * Method buildMiniMap
	 */
	private Element buildMiniMap(MapMerger mm) throws Exception
	{
		int w = MapUtil.getMiniMapWidth();
		int h = MapUtil.getMiniMapHeight();

		String imagename = mm.merge(w, h);
		String url = MapUtil.getTempUrl() + "/" + imagename;

		return new Element("minimap")
			.addContent(new Element("imgUrl").setText(url))
			.addContent(mm.getBoundingBox().toElement()) // "extent"
			.addContent(new Element("width").setText(""+w))
			.addContent(new Element("height").setText(""+h));
	}

	public static void setDefaultContext(MapMerger mm, ServiceContext context) throws Exception
	{
		Element mapContext = DefaultMapServers.getDefaultContext();

		// Add each layer in the map context to the map
		for (Element elServer: (List<Element>)mapContext.getChildren("server"))
		{
			String serverType = elServer.getAttributeValue(Constants.MAP_SERVER_TYPE);
			String serverUrl  = elServer.getAttributeValue(Constants.MAP_SERVER_URL);

			for (Element elLayer: (List<Element>)elServer.getChildren(Constants.MAP_LAYER))
			{
				try
				{
					String serviceName = elLayer.getAttributeValue("name");
					MapUtil.addService(Integer.parseInt(serverType), serverUrl, serviceName, "", mm);
				}
				catch (Exception e) { e.printStackTrace(); } // DEBUG: tell the user
			}
		}

		MapUtil.setDefBoundingBox(mm);

		// Set image size if not set
		String size = (String)context.getUserSession().getProperty(Constants.SESSION_SIZE);
		if (size == null)
			context.getUserSession().setProperty(Constants.SESSION_SIZE, MapUtil.getDefaultImageSize());

		// Update the user session
		context.getUserSession().setProperty(Constants.SESSION_MAP, mm);

//		return null;
	}

	public Element layers(Element params, ServiceContext context) throws Exception
	{
		// Get the MapMerger object from the user session
		MapMerger mm = MapUtil.getMapMerger(context);

		return mm.size() > 0 ?
				new Element("response").setContent(mm.toElement()):
				null;
	}
}

//=============================================================================


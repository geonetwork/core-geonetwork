/**
 * Common.java
 *
 * @author Created by Omnicore CodeGuide
 */

package org.wfp.vam.intermap.services.map;

import java.util.*;

import org.jdom.*;

import jeeves.server.context.*;

import org.wfp.vam.intermap.kernel.map.mapServices.*;
import org.wfp.vam.intermap.kernel.map.mapServices.arcims.*;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.*;

import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.*;
import java.net.URLDecoder;
import jeeves.server.UserSession;

public class MapUtil
{
	private static int _minh;
	private static int _minw;
	private static int _maxh;
	private static int _maxw;

	private static String defaultImageSize;

	private static String url;

	public static void setImageSizes(int minh, int minw, int maxh, int maxw) {
		_minh = minh;
		_minw = minw;
		_maxh = maxh;
		_maxw = maxw;
	}

	// Returns the MapMerger object for the user session
	public static MapMerger getMapMerger(ServiceContext context)
	{
		MapMerger mm = (MapMerger)context.getUserSession()
			.getProperty(Constants.SESSION_MAP);
		if (mm == null)
			mm = new MapMerger();

		return mm;
	}

	public static void setAoi(ServiceContext context, float minx, float miny, float maxx, float maxy)
		throws Exception
	{
		BoundingBox bb = new BoundingBox(maxy, miny, maxx, minx);

		UserSession us = context.getUserSession();
		us.setProperty("aoi", bb);
	}

	protected static BoundingBox getAoi(ServiceContext context)
		throws Exception
	{
		UserSession us = context.getUserSession();
		return (BoundingBox)us.getProperty("aoi");
	}

	protected static void unsetAoi(ServiceContext context)
		throws Exception
	{
		UserSession us = context.getUserSession();
		us.setProperty("aoi", new BoundingBox(0, 0, 0, 0)); // there is no way at the moment to set it null or delete it
	}

	protected static void setActiveLayer(Element request, MapMerger mm)
		throws Exception
	{
		StringTokenizer t = new StringTokenizer(request.getChildText("activeLayer"), "_");
		try {
			mm.setActiveLayer(Integer.parseInt(t.nextToken()), Integer.parseInt(t.nextToken()));
		}
		catch (Exception e) {} // Who cares?
	}

	protected static void setVisibleLayers(Element request, MapMerger mm)
	{
		for(Element param : (List<Element>)request.getChildren())
		{
			String key   = param.getName();
			String value = param.getText();
			StringTokenizer t = new StringTokenizer(key, "_");
			if (t.hasMoreTokens()) {
				if (t.nextToken().equals("layerVisible")) {
					String serviceId = t.nextToken();
					String layerId = t.nextToken();
					MapService ms = mm.getService(Integer.parseInt(serviceId));
					if (ms != null) {
						boolean visible = value.equals("true");
						ms.setLayerVisible(layerId, visible);
					}
				}
			}
		}
	}

	protected static void setTransparency(Element request, MapMerger mm) {
		List layers = request.getChildren();
		for (Iterator i = layers.iterator(); i.hasNext(); ) {
			Element param = (Element)i.next();
			String key = param.getName();
			String value = param.getText();
			StringTokenizer t = new StringTokenizer(key, "_");
			if (t.hasMoreTokens()) {
				if (t.nextToken().equals("t")) {
					int serviceId = Integer.parseInt(t.nextToken());
					try {
						float tr = Float.parseFloat(value);
						if (tr >= 0 && tr <= 100)
							mm.setTransparency(serviceId, Float.parseFloat(value) / 100);
					}
					catch (Exception e) { }
				}
			}
		}
	}


	/**
	 * @return the id of the service, or -1 if the service has not been added
	 */
	public static int addService(int serverType, String serverUrl, String serviceName, String vsp, MapMerger mm) throws Exception
	{
		// Do not add the service if it is already there
//		for (Enumeration e = mm.getServices(); e.hasMoreElements(); ) {
//			MapService ms = (MapService)e.nextElement();
		for(MapService service: mm.getServices())
		{
			String url = service.getServerURL();
			String name = service.getName();
			if (url.equals(serverUrl) && name.equals(serviceName))
				return -1;
		}

		int ret;
		
		switch (serverType)
		{
			case ArcIMSService.TYPE :
				ret = mm.addService(new ArcIMSService(serverUrl, serviceName));
				break;
				
			case WmsService.TYPE :
				Element capabilities = WmsGetCapClient.getCapabilities(serverUrl);
				WmsService s = new WmsService(serverUrl, serviceName, capabilities);
				ret = mm.addService(s);
				setVendorSpecificParams(s, vsp);
				break;
				
			default:
				throw new IllegalArgumentException("Unknown serverType " + serverType +
												   " for service " + serviceName + " @ " + serverUrl);
		}

		return ret;
	}

	private static void setVendorSpecificParams(WmsService service, String params) {
		if (params == null) return;

		String stParams = URLDecoder.decode(params);

		HashMap hmParams = new HashMap();
		StringTokenizer t1;
		for (t1 = new StringTokenizer(stParams, "&"); t1.hasMoreTokens(); ) {
			String s = t1.nextToken();
			int p = s.indexOf("=");

			if (p != -1) {
				String name = s.substring(0, p);
//				System.out.println("name:" + name);
				String value = s.substring(p + 1, s.length());
//				System.out.println("value: " + value);

				hmParams.put(name, value);
			}
		}

//		System.out.println("hmParams: " + hmParams);
		service.setVendorSpecificParams(hmParams); //TEST
	}
	
	public static void setDefaultContext(MapMerger mm) throws Exception
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
	}
	

	public static void setDefBoundingBox(MapMerger mm) throws Exception
	{
//		Vector v = new Vector();
//		for (Enumeration e = mm.getServices(); e.hasMoreElements(); )
//			v.add( ((MapService)e.nextElement()).getDefBoundingBox() );
//		mm.setBoundingBox(BoundingBox.union(v));

		List<BoundingBox> lbb = new ArrayList<BoundingBox>();
		for(MapService service: mm.getServices())
			lbb.add( service.getDefBoundingBox() );

		mm.setBoundingBox(BoundingBox.union(lbb));
	}

	protected static void setBBoxFromUrl(String bbox, MapMerger mm) {
		StringTokenizer st = new StringTokenizer(bbox, ",");

		try {
			float w = Float.parseFloat(st.nextToken());
			float s = Float.parseFloat(st.nextToken());
			float e = Float.parseFloat(st.nextToken());
			float n = Float.parseFloat(st.nextToken());

			mm.setBoundingBox(new BoundingBox(n, s, e, w));
		}

		catch (Exception e) {
			mm.setBoundingBox(new BoundingBox());
		}
	}

	public static int getImageWidth(ServiceContext srvContext) {
		String size = (String)srvContext.getUserSession().getProperty(Constants.SESSION_SIZE);
		int width;
		if (size.equals("small")) width = _minw;
		else width = _maxw;

		return width;
	}

	public static int getImageHeight(ServiceContext srvContext) {
		String size = (String)srvContext.getUserSession().getProperty(Constants.SESSION_SIZE);
		int height;
		if (size.equals("small")) height = _minh;
		else height = _maxh;

		return height;
	}

	// Returns the MapMerger object for the user session
	 static String getTool(ServiceContext srvContext)
	{
		String tool = (String)srvContext.getUserSession()
			.getProperty(Constants.SESSION_TOOL);
		if (tool == null)
			tool = Constants.DEFAULT_TOOL;

		return tool;
	}

	public static void setTempUrl(String url) {
		MapUtil.url = url;
	}


	public static String getTempUrl() {
		return MapUtil.url;
	}

	protected static Element getExtents(WmsService service) throws Exception {
		return service.getExtents();
	}

	protected static void moveTo(BoundingBox bb, int x, int y, int width, int height) throws Exception
	{
		float mapX = bb.getWest() + (bb.getEast() - bb.getWest()) * x / width;
		float mapY = bb.getNorth() - (bb.getNorth() - bb.getSouth()) * y / height;
		bb.moveTo(mapX, mapY);
	}

	protected static BoundingBox move(BoundingBox bb, int deltaX, int deltaY, int width, int height) throws Exception
	{
		float mapX = (bb.getEast() - bb.getWest()) * (float)deltaX / width;
		float mapY = (bb.getNorth() - bb.getSouth()) * (float)deltaY / height;
		return bb.move(mapX, mapY);
	}

	protected static BoundingBox zoomInBox(BoundingBox bb, int mapimgx, int mapimgy, int mapimgx2, int mapimgy2, int imageWidth, int imageHeigth)
		throws Exception {

		// Move and zoom
		float mapX = bb.getWest() + (bb.getEast() - bb.getWest()) * mapimgx / imageWidth;
		float mapY = bb.getNorth() - (bb.getNorth() - bb.getSouth()) * mapimgy / imageHeigth;
		float mapX2 = bb.getWest() + (bb.getEast() - bb.getWest()) * mapimgx2 / imageWidth;
		float mapY2 = bb.getNorth() - (bb.getNorth() - bb.getSouth()) * mapimgy2 / imageHeigth;

		return new BoundingBox(mapY2, mapY, mapX2, mapX);
	}

	protected static BoundingBox zoomOutBox(BoundingBox bb, int mapimgx, int mapimgy, int mapimgx2, int mapimgy2, int imageWidth, int imageHeight)
		throws Exception {

		// Move and zoom
		float north = bb.getNorth() + (bb.getNorth() - bb.getSouth()) * imageHeight / (mapimgy - mapimgy2);
		float south = bb.getSouth() - (bb.getNorth() - bb.getSouth()) * imageHeight / (mapimgy - mapimgy2);
		float east = bb.getEast() + (bb.getEast() - bb.getWest()) * imageWidth / (mapimgx2 - mapimgx);
		float west = bb.getWest() - (bb.getEast() - bb.getWest()) * imageWidth / (mapimgx2 - mapimgx);

//		System.out.println("north : " + north + "; south = " + south + "; east = " + east + "; west = " + west);

		return new BoundingBox(north, south, east, west);
	}

	public static void setDefaultImageSize(String s) {
		defaultImageSize = s;
	}

	public static String getDefaultImageSize() {
		return defaultImageSize;
	}

}


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

package org.wfp.vam.intermap.services.map;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.jdom.Element;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.DefaultMapServers;
import org.wfp.vam.intermap.kernel.map.MapMerger;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;
import org.wfp.vam.intermap.kernel.map.mapServices.arcims.ArcIMSService;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCExtension;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCLayer;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCViewContext;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCWindow;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.CapabilitiesStore;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.WmsService;

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

	private static boolean existService(MapMerger mm, String serverUrl, String serviceName)
	{
		// Do not add the service if it is already there
		for(MapService service: mm.getServices())
		{
			String url = service.getServerURL();
			String name = service.getName();
			if (url.equals(serverUrl) && name.equals(serviceName))
				return true;
		}

		return false;
	}


	/**
	 * Add a WMCLayer ad a service.
	 * WMCLayer decoding is processed here.
	 */
	public static int addService(MapMerger mm, WMCLayer layer) throws Exception
	{
		int id = addService(WmsService.TYPE,
						   layer.getServer().getOnlineResource().getHref(),
						   layer.getName(),
						   "", // layer.getChildText("vendor_spec_par"); // DEBUG
						   mm);

		WMCExtension ext = layer.getExtension();
		if(ext != null)
		{
			Element et = ext.getChild("Transparency");
			if(et != null)
				mm.setTransparency(id, Float.parseFloat(et.getText()));
		}

		return id;
	}

	/**
	 * @return the id of the service, or -1 if the service has not been added
	 */
	public static int addService(int serverType, String serverUrl, String serviceName, String vsp, MapMerger mm) throws Exception
	{
		// Do not add the service if it is already there
		if(existService(mm, serverUrl, serviceName))
			return -1;

		int ret;

		switch (serverType)
		{
			case ArcIMSService.TYPE :
				ret = mm.addService(new ArcIMSService(serverUrl, serviceName));
				break;

			case WmsService.TYPE :
//				Element capabilities = WmsGetCapClient.getCapabilities(serverUrl);   // Old version: we may need it in snv commit
				Element capabilities = CapabilitiesStore.getCapabilities(serverUrl); // NEW VERSION: WORK IN PROGRESS
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

	/**
	 * @deprecated ETj: image dimensions are no longer handled server side
	 */
	public static int getImageWidth(ServiceContext srvContext) {
		String size = (String)srvContext.getUserSession().getProperty(Constants.SESSION_SIZE);
		int width;
		if (size.equals("small")) width = _minw;
		else width = _maxw;

		return width;
	}

	/**
	 * @deprecated ETj: image dimensions are no longer handled server side
	 */
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

	public static BoundingBox setScale(BoundingBox bb, int imageWidth, int imageHeight, long scale, float dpi)
		throws Exception
	{
		float cx = (bb.getEast()+bb.getWest())/2;
		float cy = (bb.getNorth()+bb.getSouth())/2;

		// Set the scale
		double degScale = scale/423307109.727 * 96f/dpi;
		float dx = (float)(imageWidth  * degScale);
	 	float dy = (float)(imageHeight * degScale);

		return new BoundingBox( cy+dy/2, cy-dy/2,
									   cx+dx/2, cx-dx/2);
	}


	public static void setDefaultImageSize(String s) {
		defaultImageSize = s;
	}

	public static String getDefaultImageSize() {
		return defaultImageSize;
	}

	public static String setContext(MapMerger mm, WMCViewContext vc) throws Exception
	{
		for (WMCLayer layer: vc.getLayerList())
		{
			try
			{
				MapUtil.addService(mm, layer);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// set bounding box
		mm.setBoundingBox(new BoundingBox(vc.getGeneral().getBoundingBox()));

		WMCWindow win = vc.getGeneral().getWindow();
		String imagename = mm.merge(win.getWidth(), win.getHeight());
		String url = MapUtil.getTempUrl() + "/" + imagename;

		return url;
	}
}



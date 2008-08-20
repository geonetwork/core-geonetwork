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

package org.wfp.vam.intermap.kernel.map.mapServices.wms;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import jeeves.utils.Log;

import org.jdom.Element;
import org.jdom.Text;
import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;
import org.wfp.vam.intermap.kernel.map.mapServices.constants.MapServices;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.dimensions.Extents;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl.WMSFactory;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSCapabilities;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSEX_GeographicBoundingBox;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSFormat;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSLayer;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSLegendURL;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSOperationType;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSStyle;
import org.wfp.vam.intermap.util.Util;

public class WmsService extends MapService
{
	public static final int TYPE = 2;

	private boolean visible; // Not used yet => not degugged
	private BoundingBox bb = new BoundingBox();

	private final WMSLayer _wmsLayer; // the part of the getCapabilities response regarding the layer
	private final WMSCapabilities _wmscapa;

	private String _styleName; // WMS style for this layer
	private final WMSCapabilities.WMSVer _wmsVer; // Version of the WMS server
	private String imageType;

	private Element errorElement;

	private Map<String, String> htExtents = new HashMap<String, String>();
	private HashMap hmVsp = new HashMap();


	public WmsService(String mapServerUrl, String serviceName, Element capabilities)
		throws Exception
	{
		super(mapServerUrl, serviceName);

		this.info = capabilities;
		_wmscapa = WMSFactory.parseCapabilities(capabilities);
		_wmsVer = _wmscapa.getVersion();

		imageType = getImageType(_wmscapa);

		_wmsLayer = _wmscapa.getCapability().getLayer().getLayer(serviceName);

		WMSStyle wmsstyle = _wmsLayer.getStyle(0);
		if(wmsstyle != null)
			_styleName = _wmsLayer.getStyle(0).getName();
	}

	/**
	 * Returns LayerDoc
	 */
	public WMSLayer getWmsLayer()
	{
		return _wmsLayer;
	}

	/**
	 * Method setExtent
	 */
	public void setExtent(String name, String value) {
		htExtents.put(name, value);
	}

	// Sets the vendor specific parameters
	public void setVendorSpecificParams(HashMap hmVspars){
		hmVsp  = hmVspars;		// TEST
	}

	/** Returns 2 (-> WMS) */
	public int getType() { return TYPE; }

	/**
	 * Method getImageUrl
	 */
	public String getImageUrl(BoundingBox bBox, int width, int height)
	{
		return getImageUrl(bBox, width, height, name);
	}

	/** Returns the image request URL with  many image names (utility method) */
	public String getImageUrl(BoundingBox bBox, int width, int height, String name)
	{
		bb = bBox;
		String request;

		String getMapHref = _wmscapa.getCapability().getRequest().getGetMap().getDCPType(0).getHttpGetHref();
		String prefix = setPrefix(getMapHref);

		if (_wmsVer == WMSCapabilities.WMSVer.V100)
		{
			// WMTVER is deprecated but needed for older map servers
			// SERVICE should not be required, but servers were found to give errors without it if I remember. FIXME
			request = prefix + "SERVICE=WMS&WMTVER=" + _wmsVer + "&REQUEST=map";
		}
		else
		{
			/* Dec 15, 2004 - ticheler - Changed the REQUEST=map to REQUEST=GetMap according to specs because the
			 * REQUEST=map seems to trigger exceptions (non-compliant behaviour)*/
			request = prefix + "SERVICE=WMS&VERSION=" + _wmsVer + "&REQUEST=GetMap";
		}

		request += "&LAYERS=" + name
				+ "&SRS=EPSG:4326"
				+ "&BBOX=" + bb.getWest() + "," + bb.getSouth() + "," + bb.getEast() + "," + bb.getNorth()
				+ "&WIDTH="	+ width + "&HEIGHT=" + height
				+ "&FORMAT=" + imageType + "&TRANSPARENT=TRUE";


		// set extents
		for (String key: htExtents.keySet())
		{
			String value = htExtents.get(key);
			request += "&" + key + "=" + value;
		}

		// Set style
		if (_styleName != null)
			request += "&STYLES=" + _styleName;
        else
            request += "&STYLES=";

        Log.debug(Constants.WMS," - GetMap request : "+ request);

		return request;
	}

	private static String setPrefix(String prefix)
	{
		if (prefix.indexOf("?") == -1) return prefix + "?";
		else if (!prefix.endsWith("?")) return prefix + "&";
		else return prefix;
	}

	public int getActiveLayer() { return 1; }

	public void identify(int layer, int x, int y, int width, int height, int tolerance, String reqFormat)
		throws Exception
	{
		WMSOperationType gfi = _wmscapa.getCapability().getRequest().getGetFeatureInfo(); // Optional request
		String getFeatureInfoHref  = gfi.getDCPType(0).getHttpGetHref();

		String prefix = setPrefix(getFeatureInfoHref);
//		System.out.println("Requested format: " + reqFormat); // DEBUG
		if (reqFormat == null) reqFormat = ""; //DEBUG
		WMSFormat infoFormat = strFormat(reqFormat);
//		System.out.println("Accepted format: " + infoFormat); // DEBUG
//		String infoFormat = isGml() ? "application/vnd.ogc.gml" : "text/plain"; //TEST
		// FIXME Changed to ensure compatability with WMS 1.1.1 and 1.1.0
		String url;

		if (_wmsVer == WMSCapabilities.WMSVer.V100)
		{
			url = prefix + "WMTVER=" + _wmsVer + "&REQUEST=GetFeatureInfo";
	    }
		else
		{
			url = prefix + "SERVICE=WMS&VERSION=" + _wmsVer + "&REQUEST=GetFeatureInfo";
	    }

		url +=  "&LAYERS=" + name
				+ "&SRS=EPSG:4326"
				+ "&BBOX=" + bb.getWest() + "," + bb.getSouth() + ","	+ bb.getEast() + "," + bb.getNorth()
				+ "&WIDTH=" + width + "&HEIGHT=" + height
				+ "&FORMAT=" + imageType
				+ "&QUERY_LAYERS=" + name + "&X=" + x + "&Y=" + y
				+ "&INFO_FORMAT=" + infoFormat
				+ "&STYLES=" + _styleName;
		
		Log.debug(Constants.WMS," - GetFeatureInfo request : "+ url);
		
		lastResponse = new Element("url").setText(url);

		// Use the following code if you need to proxy the WMS server response.

//		HttpClient h = new HttpClient(url);
//
//		if(infoFormat == WMSFormat.APP_GML
//		   || infoFormat == WMSFormat.TEXT_XHTML  //.equals(Constants.FORMAT_GML) || infoFormat.equals(Constants.FORMAT_XHTML)) {
//		   || infoFormat == WMSFormat.v100_GML1  // TODO test me
//		   || infoFormat == WMSFormat.v100_GML2  // TODO test me
//		   || infoFormat == WMSFormat.v100_GML3) // TODO test me
//		{
//			lastResponse = new Element("gml").addContent(h.getElement());
////			System.out.println("lastResponse element, " + infoFormat + ": " + Xml.getString(lastResponse)); // DEBUG
//		}
//		else if (infoFormat == WMSFormat.TEXT_HTML) // infoFormat.equals(Constants.FORMAT_HTML)) {
//		{
//			String strResult = h.getString();
////			System.out.println("Server response: " + strResult); // DEBUG
//			lastResponse = new Element("html").addContent(new CDATA(strResult)); //Add the html as content to an <html> element
//		}
//		else {
//			lastResponse = new Element("text").addContent(new CDATA(h.getString()));
//		}
////		System.out.println("lastResponse element, "+infoFormat+": " + Xml.getString(lastResponse)); // DEBUG
	}

	/**
	 * Returns the available format to be requested.
	 * The method checks out if the requested Format is available in the capabilities document.
	 * If requested format is not found, another format among the available ones will be used,
	 * choosing one according to this priority list: HTML, XHTML, GML and finally PLAIN
	 */
	private WMSFormat strFormat(String reqFormat)
	{
		WMSOperationType gfi = _wmscapa.getCapability().getRequest().getGetFeatureInfo();
		Set<WMSFormat> availableFormats = gfi.getFormats();
		WMSFormat req = WMSFormat.parse(reqFormat);
		if(req==null)
			return WMSFormat.TEXT_PLAIN;

		if(availableFormats.contains(req))
			return req; //return format that was requested, should normally suffice, rest is only to trap faulty requests
		else if (availableFormats.contains(WMSFormat.TEXT_HTML))
			return WMSFormat.TEXT_HTML;
		else if (availableFormats.contains(WMSFormat.TEXT_XHTML))
			return WMSFormat.TEXT_XHTML;
		else if (availableFormats.contains(WMSFormat.APP_GML))
			return WMSFormat.APP_GML;
		else
			return WMSFormat.TEXT_PLAIN;
	}

	/** Not used yet */
	public void setLayerVisible(String id, boolean visible) { this.visible = visible; }

	/** Returns the default bounding box, as in LatLonBoundingBox */
	public BoundingBox getDefBoundingBox()
	{
		WMSEX_GeographicBoundingBox gbb = _wmsLayer.getGeoBB();
		if(gbb != null)
			return  Util.getBB(gbb);
		else // it should not happen, because layers must always have a geobb according to WMS specs
			return new BoundingBox();
	}

	/** Returns the current BoundingBox */
	public BoundingBox getBoundingBox() { return bb; }

	/** Not used yet */
	public Vector getVisibleLayers() {
		Vector v = new Vector();
		if (visible)
			v.add(new Integer(1));
		return v;
	}

	/** Returns the legend for the currently selected style, as in the Style element */
	public String getLegendUrl()
	{
//		if (_wmsVer != WMSCapabilities.WMSVer.V100)//.startsWith("1.1")) // 1.0 documentation was no clear for the legend URL
//		{
			WMSStyle wstyle = _wmsLayer.getStyle(_styleName);
			if(wstyle != null)
			{
				WMSLegendURL wlegurl = wstyle.getLegendURL(0);
				if(wlegurl != null)
					return wlegurl.getOnlineResource().getHref();
			}
//		}

		return null;
	}

	/** Converts this service into a Jdom Element, to be used by the Jeeves Map service */
	public Element toElement()
	{
		Element elService = new Element("service")
			.setAttribute("type", "WMS")
			.setAttribute("name", getName())
			.addContent(getInfo())
			.addContent(new Element(MapServices.LAST_RESPONSE_TAG).addContent(getLastResponse()));

		if (_styleName != null)
			elService.setAttribute("style", _styleName);

		// add selected extents information
		Element extents = new Element("selectedExtents");
		for(String key: htExtents.keySet())
		{
			String value = htExtents.get(key);
			extents.addContent(new Element(key).setText(value));
		}

		elService.addContent(extents);

		return elService;
	}

	/** Useless for WMS layers */
	public void setActiveLayer(int layer) throws Exception {}

	/** Sets the style for this service */
	public void setStyleName(String style) { _styleName = style; }
	public String getStyleName() { return _styleName;}

	public String getTitle()
	{
		return _wmsLayer.getTitle();
	}

	public Element getExtents()
	{
//		Extents e = new Extents(layerInfo);
		Extents e = new Extents(_wmsLayer.getDimensionIterator());
		return e.getJdom();
	}

	/** Returns one of the image types returned by the server choosing
	 *  from gif, png and jpeg
	 */
	private String getImageType(WMSCapabilities wmsc)
	{
		Set<WMSFormat> formats = wmsc.getCapability().getRequest().getGetMap().getFormats();
		if(WMSCapabilities.WMSVer.V100 == wmsc.getVersion())
		{
			if(formats.contains(WMSFormat.v100_PNG))
				return WMSFormat.v100_PNG.toString();
			else if(formats.contains(WMSFormat.v100_GIF))
				return WMSFormat.v100_GIF.toString();
			else if(formats.contains(WMSFormat.v100_JPEG))
				return WMSFormat.v100_JPEG.toString();
		}
		else
		{
			if(formats.contains(WMSFormat.IMAGE_PNG))
				return WMSFormat.IMAGE_PNG.toString();
			else if(formats.contains(WMSFormat.IMAGE_GIF))
				return WMSFormat.IMAGE_GIF.toString();
			else if(formats.contains(WMSFormat.IMAGE_JPG))
				return WMSFormat.IMAGE_JPG.toString();
		}

		return formats.iterator().next().toString();
	}

	private static Element toHtml(String s) {
		Vector v = new Vector();

		for (StringTokenizer t = new StringTokenizer(s, "\r\n"); t.hasMoreTokens(); ) {
			Text line = new Text(t.nextToken());
			v.add(line);
			v.add(new Element("br"));
		}

		return new Element("html").setContent(v);
	}


}

//==============================================================================
//=== $Log: WmsService.java,v $
//=== Revision 1.15  2005/11/09 11:39:31  sgiaccio
//=== More fixes to WMS get feature info
//===
//=== Revision 1.14  2005/11/09 10:38:51  sgiaccio
//=== *** empty log message ***
//===
//=== Revision 1.13  2005/11/08 17:01:35  sgiaccio
//=== *** empty log message ***
//===
//=== Revision 1.12  2005/11/08 17:00:07  sgiaccio
//=== Some fix to the identify feature on wms layers.
//===
//=== Revision 1.11  2005/09/21 08:00:19  sgiaccio
//=== Cleaned cache management
//===
//=== Revision 1.10  2005/09/19 16:39:26  sgiaccio
//=== Some code cleaning
//===
//=== Revision 1.9  2005/09/13 10:18:10  ticheler
//=== InterMap version 2.0
//===
//==============================================================================



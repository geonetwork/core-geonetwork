/**
 * WmsService.java
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms;

import java.util.*;

import org.jdom.*;

import jeeves.utils.*;

import org.wfp.vam.intermap.Constants;
import org.wfp.vam.intermap.kernel.map.mapServices.*;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;
import org.wfp.vam.intermap.kernel.map.mapServices.constants.MapServices;

import java.io.*;
import java.net.*;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.dimensions.Extents;

public class WmsService extends MapService
{
	public static final int TYPE = 2;
	
	private boolean visible; // Not used yet => not degugged
	private BoundingBox bb = new BoundingBox();
	private Element layerInfo; // the part of the getCapabilities response regarding the layer
	private String style; // WMS style for this layer
	private String wmsVer; // Version of the WMS server
	private String imageType;
	
	// Request prefixes
	private String getCapabilitiesPrefix;
	private String getMapPrefix;
	private String getFeatureInfoPrefix;
	private String describeLayerPrefix;
	private String getLegendGraphicPrefix;
	private String getStylesPrefix;
	private String putStylesPrefix;
	
	private Element errorElement;
	
	private Hashtable htExtents = new Hashtable();
	private HashMap hmVsp = new HashMap();
	
	private static File xslFiles;
	
	public WmsService(String mapServerUrl, String serviceName, Element capabilities)
		throws Exception {
		super(mapServerUrl, serviceName);
		
		this.info = capabilities;
		wmsVer = capabilities.getAttributeValue("version");
		imageType = getImageType(capabilities);
		
		// Get layer info
		Element root = new Element("root").addContent(new Element("serviceName").setText(serviceName))
			.addContent((Element)capabilities.clone());
		Element stylesheet = Xml.loadFile(xslFiles + File.separator + "cap_test.xsl");
		layerInfo = Xml.transform(root, stylesheet);
		
		Element elStyle = layerInfo.getChild("Style");
		if (elStyle != null)
			style = elStyle.getChildText("Name");
		
		getPrefixes();
	}
	
	/**
	 * Method setExtent
	 *
	 * @param    name                a  String
	 * @param    value               a  String
	 *
	 */
	public void setExtent(String name, String value) {
		htExtents.put(name, value);
//		System.out.println(htExtents); // TEST
	}
	
	// Sets the vendor specific parameters
	public void setVendorSpecificParams(HashMap hmVspars){
		hmVsp  = hmVspars;		// TEST
	}
	
	/** Loads the xsl files in a XmlRepository */
	public static void init(String path) {
		xslFiles = new File(path);
	}
	
	/** Returns 2 (-> WMS) */
	public int getType() { return TYPE; }
	
	/** Returns the image request URL with  many image names (utility method) */
	public String getGroupImageUrl(BoundingBox bBox, int width, int height, Vector imageNames) {
		bb = bBox;
		String request;
		
		// Build the comma separated list of image names
		String stImageNames = "";
		for (int i = imageNames.size() - 1; i >= 0; i--) {
			stImageNames += URLEncoder.encode((String)imageNames.get(i));
			if (i > 0)
				stImageNames += ",";
		}
		
		String prefix = setPrefix(getMapPrefix);
		if (wmsVer=="1.0.0") {
			// WMTVER is deprecated but needed for older map servers
			// SERVICE should not be required, but servers were found to give errors without it if I remember. FIXME
			request = prefix + "SERVICE=WMS&WMTVER=" + wmsVer + "&REQUEST=map&LAYERS=" +
				stImageNames + "&SRS=EPSG:4326&BBOX=" + bb.getWest() + ","
				+ bb.getSouth() + "," + bb.getEast() + "," + bb.getNorth() + "&WIDTH="
				+ width + "&HEIGHT=" + height + "&FORMAT=" + imageType + "&TRANSPARENT=TRUE";
		}
		else {
			/* Dec 15, 2004 - ticheler - Changed the REQUEST=map to REQUEST=GetMap according to specs because the
			 * REQUEST=map seems to trigger exceptions (non-compliant behaviour)*/
			request = prefix + "SERVICE=WMS&VERSION=" + wmsVer + "&REQUEST=GetMap&LAYERS=" +
				stImageNames + "&SRS=EPSG:4326&BBOX=" + bb.getWest() + ","
				+ bb.getSouth() + "," + bb.getEast() + "," + bb.getNorth() + "&WIDTH="
				+ width + "&HEIGHT=" + height + "&FORMAT=" + imageType + "&TRANSPARENT=TRUE";
			
		}
		
		// set extents
		for (Enumeration e = htExtents.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			String value = (String)htExtents.get(key);
			request += "&" + key + "=" + value;
		}
		
//		System.out.println("\n\n\nrequest\n\n\n" + request); // TEST
		
		// Set style
		if (style != null) {
			request += "&STYLES=" + style;
		}
        else {
            request += "&STYLES=";
        }
		
		System.out.println("request: " + request);
		
		return request;
	}
	
	/**
	 * Method getGroupImageUrl
	 *
	 * @param    bBox                a  BoundingBox
	 * @param    width               an int
	 * @param    height              an int
	 * @param    imageNames          a  Vector
	 *
	 * @return   an Object
	 */
	public String getImageUrl(BoundingBox bBox, int width, int height)
	{
		Vector imageNames = new Vector();
		imageNames.add(name);
		
		return getGroupImageUrl(bBox, width, height, imageNames);
	}
	
	public int getActiveLayer() { return 1; }
	
	public void identify(int layer, int x, int y, int width, int height, int tolerance, String reqFormat)
		throws Exception {
		String prefix = setPrefix(getFeatureInfoPrefix);
//		System.out.println("Requested format: " + reqFormat); // DEBUG
		if (reqFormat == null) reqFormat = ""; //DEBUG
		String infoFormat = strFormat(reqFormat);
//		System.out.println("Accepted format: " + infoFormat); // DEBUG
//		String infoFormat = isGml() ? "application/vnd.ogc.gml" : "text/plain"; //TEST
		// FIXME Changed to ensure compatability with WMS 1.1.1 and 1.1.0
		String url;
		
		if (wmsVer=="1.0.0") {
			url = prefix + "WMTVER=" + wmsVer + "&REQUEST=GetFeatureInfo&LAYERS="
				+ name
//				+ URLEncoder.encode(name)
				+ "&SRS=EPSG:4326&BBOX=" + bb.getWest() + "," + bb.getSouth() + ","
				+ bb.getEast() + "," + bb.getNorth() + "&WIDTH=" + width
				+ "&HEIGHT=" + height + "&FORMAT=" + imageType
				+ "&QUERY_LAYERS=" + name + "&X=" + x + "&Y=" + y
				+ "&INFO_FORMAT=" + infoFormat;
			
	    } else {
			url = prefix + "SERVICE=WMS&VERSION=" + wmsVer + "&REQUEST=GetFeatureInfo&LAYERS="
				+ name
//	    		+ URLEncoder.encode(name)
				+ "&SRS=EPSG:4326&BBOX=" + bb.getWest() + "," + bb.getSouth() + ","
				+ bb.getEast() + "," + bb.getNorth() + "&WIDTH=" + width
				+ "&HEIGHT=" + height + "&FORMAT=" + imageType
				+ "&QUERY_LAYERS=" + name + "&X=" + x + "&Y=" + y
				+ "&INFO_FORMAT=" + infoFormat;
	    }
		
//		System.out.println("GetFeatureInfo request: " + url.toString()); // DEBUG
		
		HttpClient h = new HttpClient(url);
		
		if(infoFormat.equals(Constants.FORMAT_GML) || infoFormat.equals(Constants.FORMAT_XHTML)) {
			lastResponse = new Element("gml").addContent(h.getElement());
//			System.out.println("lastResponse element, " + infoFormat + ": " + Xml.getString(lastResponse)); // DEBUG
		}
		else if (infoFormat.equals(Constants.FORMAT_HTML)) {
			String strResult = h.getString();
//			System.out.println("Server response: " + strResult); // DEBUG
			lastResponse = new Element("html").addContent(new CDATA(strResult)); //Add the html as content to an <html> element
		}
		else {
			lastResponse = new Element("text").addContent(new CDATA(h.getString()));
		}
//		System.out.println("lastResponse element, "+infoFormat+": " + Xml.getString(lastResponse)); // DEBUG
		
	}
	
	private String strFormat(String reqFormat){
		/*Returns the available format to be requested.
		 The Format requested is checked if available in the capabilities document.
		 If requested format is not found, Intermap will try to use another format provided,
		 ordered as XHTML, HTML, GML and finally PLAIN
		 */
		Hashtable htFormat = new Hashtable();
		Element getFeatureInfo = info.getChild("Capability").getChild("Request").getChild("GetFeatureInfo");
		
		List formats = getFeatureInfo.getChildren("Format");
		for (Iterator i = formats.iterator(); i.hasNext(); ) {
			Element elFormat = (Element)i.next();
			String text = elFormat.getText();
			//System.out.println("format "+i.toString()+text); //TEST
			htFormat.put(text,i);
		}
		
		if (htFormat.containsKey(reqFormat))
			return reqFormat; //return format that was requested, should normally suffice, rest is only to trap faulty requests
		else if (htFormat.containsKey(Constants.FORMAT_HTML))
			return Constants.FORMAT_HTML;
		else if (htFormat.containsKey(Constants.FORMAT_XHTML))
			return Constants.FORMAT_XHTML;
		else if (htFormat.containsKey(Constants.FORMAT_GML))
			return Constants.FORMAT_GML;
		else
			return Constants.FORMAT_PLAIN;
	}
	
	/** Not used yet */
	public void setLayerVisible(String id, boolean visible) { this.visible = visible; }
	
	/** Returns the default bounding box, as in LatLonBoundingBox */
	public BoundingBox getDefBoundingBox() {
		Element elBoundingBox = layerInfo.getChild("LatLonBoundingBox");
		
		if (elBoundingBox != null) {
			float minx = Float.parseFloat(elBoundingBox.getAttributeValue("minx"));
			float miny = Float.parseFloat(elBoundingBox.getAttributeValue("miny"));
			float maxx = Float.parseFloat(elBoundingBox.getAttributeValue("maxx"));
			float maxy = Float.parseFloat(elBoundingBox.getAttributeValue("maxy"));
			
			return new BoundingBox(maxy, miny, maxx, minx);
		}
		else return new BoundingBox();
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
	public String getLegendUrl() throws Exception {
		if (wmsVer.startsWith("1.1")) // 1.0 documentation was no clear for the legend URL
		{
			Namespace ns = org.jdom.Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
			List styles = layerInfo.getChildren("Style");
			for (Iterator i = styles.iterator(); i.hasNext(); ) {
				Element elStyle = (Element)i.next();
				if (elStyle.getChildText("Name").equals(style))
					try {
						return  elStyle.getChild("LegendURL")
							.getChild("OnlineResource").getAttributeValue("href", ns); // null if no legend
					}
				catch (Exception e) {} // LegendUrl is optional, so JDOM throws an exception if he doesn't find it,
				// but we just return null
			}
		}
		
		return null;
	}
	
	/** Converts this service into a Jdom Element, to be used by the Jeeves Map service */
	public Element toElement() {
		Element elService = new Element("service")
			.setAttribute("type", "WMS")
			.setAttribute("name", getName())
			.addContent(getInfo())
			.addContent(new Element(MapServices.LAST_RESPONSE_TAG).addContent(getLastResponse()));
		
		if (style != null)
			elService.setAttribute("style", style);
		
		// add selected extents information
		Element extents = new Element("selectedExtents");
		for (Enumeration e = htExtents.keys(); e.hasMoreElements(); ) {
			String key = (String)e.nextElement();
			String value = (String)htExtents.get(key);
			extents.addContent(new Element(key).setText(value));
		}
		
		elService.addContent(extents);
		
		return elService;
	}
	
	/** Useless for WMS layers */
	public void setActiveLayer(int layer) throws Exception {}
	
	/** Sets the style for this service */
	public void setStyle(String style) { this.style = style; }
	
	public String getTitle() {
		return layerInfo.getChildText("Title");
	}
	
//	private Element getLayerInfo(String name, Element capabilities) {
//		List layers = capabilities.getChild("Capability").getChildren("Layer");
//		return findLayerInfo(name, layers);
//	}
//
//	private Element findLayerInfo(String name, List layers) {
//		for (Iterator i = layers.iterator(); i.hasNext(); ) {
//			Element layer = (Element)i.next();
//
//			String layerName = layer.getChildText("Name");
//			if (layerName != null && layerName.equals(name))
//				return (Element)layer.clone();
//			else {
//				// Recursively apply this method
//				Element info = findLayerInfo(name, layer.getChildren());
//				if (info != null) return info;
//			}
//		}
//
//		return null; // Layer not found
//	}
	
	public Element getExtents() {
		Extents e = new Extents(layerInfo);
		return e.getJdom();
	}
	
	private void getPrefixes() throws Exception {
		Element request = info.getChild("Capability").getChild("Request");
		if (wmsVer.startsWith("1.1")) {
			// Required requests
			getCapabilitiesPrefix = get11Prefix(request, "GetCapabilities");
			getMapPrefix = get11Prefix(request, "GetMap");
			
			// Optional requests
			try { getFeatureInfoPrefix = get11Prefix(request, "GetFeatureInfo"); } catch (Exception e) {}
			try { describeLayerPrefix = get11Prefix(request, "DescribeLayer"); } catch (Exception e) {}
			try { getLegendGraphicPrefix = get11Prefix(request, "GetLegendGraphic"); } catch (Exception e) {}
			try { getStylesPrefix = get11Prefix(request, "GetStyles"); } catch (Exception e) {}
			try { putStylesPrefix = get11Prefix(request, "PutStyles"); } catch (Exception e) {}
		}
			
		else if (wmsVer.startsWith("1.0")) {
			// Required requests
			getCapabilitiesPrefix = get10Prefix(request, "Capabilities");
			getMapPrefix = get10Prefix(request, "Map");
			
			// Optional requests
			try { getFeatureInfoPrefix = get10Prefix(request, "FeatureInfo"); } catch (Exception e) {}
		}
			
		else throw new Exception();
	}
	
	private String setPrefix(String prefix) {
		if (prefix.indexOf("?") == -1) return prefix + "?";
		else if (!prefix.endsWith("?")) return prefix + "&";
		else return prefix;
	}
	
	private String get11Prefix(Element elRequest, String request) {
		Namespace ns = org.jdom.Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");
		return elRequest.getChild(request).getChild("DCPType").getChild("HTTP")
			.getChild("Get").getChild("OnlineResource").getAttributeValue("href", ns);
	}
	
	private String get10Prefix(Element elRequest, String request) {
		return elRequest.getChild(request).getChild("DCPType").getChild("HTTP")
			.getChild("Get").getAttributeValue("onlineResource");
	}
	
	/** Returns one of the image types returned by the server choosing
	 *  from gif, ong and jpeg
	 */
	private String getImageType(Element capabilities) {
		// WmtVer = 1.1.x
		if (wmsVer.startsWith("1.1")) {
			List typesEl = capabilities.getChild("Capability")
				.getChild("Request")
				.getChild("GetMap")
				.getChildren("Format");
			Vector types = new Vector();
			for (Iterator i = typesEl.iterator(); i.hasNext(); )
				types.add(((Element)i.next()).getText());
			if (types.contains("image/png"))
				return "image/png";
			else if (types.contains("image/gif"))
				return "image/gif";
			else
				return "image/jpeg"; // Try jpeg
		}
			
			// WmsVer = 1.0.x
		else if (wmsVer.startsWith("1.0")) {
			List typesEl = capabilities.getChild("Capability")
				.getChild("Request")
				.getChild("Map")
				.getChild("Format")
				.getChildren();
			Vector types = new Vector();
			for (Iterator i = typesEl.iterator(); i.hasNext(); )
				types.add(((Element)i.next()).getName());
			if (types.contains("JPEG"))
				return "JPEG";
			else if (types.contains("PNG"))
				return "PNG";
			else if (types.contains("GIF"))
				return "GIF";
			else
				return "JPEG"; // Try jpeg
		}
		
		return "image/jpeg"; // Try to guess jpeg
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





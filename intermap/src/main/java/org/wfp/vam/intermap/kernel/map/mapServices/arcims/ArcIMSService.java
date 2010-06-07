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

package org.wfp.vam.intermap.kernel.map.mapServices.arcims;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.BoundingBox;
import org.wfp.vam.intermap.kernel.map.mapServices.MapService;
import org.wfp.vam.intermap.kernel.map.mapServices.ServiceException;
import org.wfp.vam.intermap.kernel.map.mapServices.constants.MapServices;
import org.wfp.vam.intermap.util.XmlUtil;

public class ArcIMSService extends MapService
{

	/**
	 * Method getGroupImageUrl
	 */
	public String getGroupImageUrl(BoundingBox bBox, int width, int height, List imageNames)
	{
		// TODO
		return null;
	}

	public static final int TYPE = 1;

	private BoundingBox bb;
	private int activeLayer = 0;
	private String errorStr;

	private static final double METERS_IN_DEGREE = 111195.0;
	private static final double FEET_IN_DEGREE = 364813.0;

//	private Hashtable htLayerInfo; // Used to fasten the access to the getServiceInfo response

	/**
	 * Sets the ArcIMS map server URL and the service name.
	 * <p>Also sends a GET_SERVICE_INFO request to the map server and sets the
	 * starting visibility for the layers as specified in the response.
	 * <p>The GET_SERVICE_INFO response can be retrieved using the toElement
	 * method.
	 *
	 * @param    url                the map server url
	 * @param    name               the map service name
	 *
	 */
	public ArcIMSService(String url, String name)
		throws Exception
	{
		super(url, name);

		// Send the request to the map server
		Element axlRequest = AxlRequestBuilder.getRequest("getServiceInfo.xml");
		ArcIMSClient client = new ArcIMSClient(url, name, axlRequest);
		Element response = client.getElement();

		// Get the LAYERINFO elements
		List lLayers = response.getChild("RESPONSE")
			.getChild("SERVICEINFO")
			.getChildren("LAYERINFO");

		// Set the internal id for each layer in the service. We can't use the
		// layer name because it maight contain spaces, and Jeeves would throw
		// an exception in converting them from HTTP paramerets to elements
		int internalId = 1;
		for (Iterator i = lLayers.iterator(); i.hasNext(); ) {
			Element elLayerInfo = (Element)i.next();
			if(activeLayer == 0 && elLayerInfo.getAttributeValue("type").equals("featureclass"))
				activeLayer = internalId;
			elLayerInfo.setAttribute("internalId", "" + internalId++);
//			htLayerInfo.put(new Integer(internalId), elLayerInfo);
		}

		// Fills the layers Hashtable with the service's layers as key, and
		// set set the visible property for each one
		for (Iterator i = lLayers.iterator(); i.hasNext(); ) {
			Element elLayerInfo = (Element)i.next();
			boolean show = elLayerInfo.getAttributeValue("visible").equals("true");
			setLayerVisible(elLayerInfo.getAttributeValue("internalId"), show);
		}

		info = new Element(MapServices.INFO_TAG).addContent(response);
		bb = getDefBoundingBox(); // Get the default bounding box
	}

	public int getType() {
		return TYPE;
	}

	/**
	 * Builds a Bounding Box from the envelope tag in the GET_SERVICE_INFO
	 * response.
	 *
	 * @return   The default service envelope
	 *
	 */
	public BoundingBox getDefBoundingBox() throws Exception {
		Element elBb = XmlUtil.getElement(info, "/info/ARCXML/RESPONSE/SERVICEINFO/PROPERTIES/ENVELOPE");

		float maxy = Float.parseFloat(elBb.getAttributeValue("maxy"));
		float miny = Float.parseFloat(elBb.getAttributeValue("miny"));
		float maxx = Float.parseFloat(elBb.getAttributeValue("maxx"));
		float minx = Float.parseFloat(elBb.getAttributeValue("minx"));

		// Convert map units
		String units = XmlUtil.getElement(info, "/info/ARCXML/RESPONSE/SERVICEINFO/PROPERTIES/MAPUNITS")
			.getAttributeValue("units");
		if (units.equals("meters")) {
			// Meters to decimal degrees
			maxy /= METERS_IN_DEGREE;
			miny /= METERS_IN_DEGREE;
			maxx /= METERS_IN_DEGREE;
			minx /= METERS_IN_DEGREE;
		}
		if (units.equals("feet")) {
			// Feet to decimal degrees
			maxy /= FEET_IN_DEGREE;
			miny /= FEET_IN_DEGREE;
			maxx /= FEET_IN_DEGREE;
			minx /= FEET_IN_DEGREE;
		}

		BoundingBox bb = new BoundingBox(maxy, miny, maxx, minx);

		return bb;
	}

	public BoundingBox getBoundingBox() { return bb; }

	/**
	 * Sends a GET_IMAGE request to the map server.
	 *
	 * @param    bBox                a  BoundingBox
	 *
	 * @return  the URL of the image on the server
	 *
	 * @throws   Exception
	 *
	 */
	public String getImageUrl(BoundingBox bBox, int width, int height)
		throws Exception, ServiceException
	{
		this.bb = bBox;

		// Build the request for the map server
		Element elService = this.toElement()
			.addContent(new Element("imageWidth").setText(width + ""))
			.addContent(new Element("imageHeight").setText(height + ""));
		Element request = AxlRequestBuilder.getRequest(elService, "getImage.xsl");

		// Send the request and get the response as an Element
		ArcIMSClient client = new ArcIMSClient(serverUrl, name, request);
		lastResponse = client.getElement();

		// Check if an error was generated on the server
		checkArcImsError();

		// Set the current bounding box from the response
		Element elEnvelope = lastResponse.getChild("RESPONSE")
			.getChild("IMAGE")
			.getChild("ENVELOPE");
		bb = new BoundingBox(Float.parseFloat(elEnvelope.getAttributeValue("maxy")),
							   Float.parseFloat(elEnvelope.getAttributeValue("miny")),
							   Float.parseFloat(elEnvelope.getAttributeValue("maxx")),
							   Float.parseFloat(elEnvelope.getAttributeValue("minx")));

		// Get the image URL from the ArcXML response
		return XmlUtil.getElement(lastResponse, "/ARCXML/RESPONSE/IMAGE/OUTPUT")
			.getAttributeValue("url");
	}

	/**
	 * Sends a GET_EXTRACT request to the map server.
	 *
	 * @param    bBox                a  BoundingBox
	 *
	 * @return  the URL of the image on the server
	 *
	 * @throws   Exception
	 *
	 */
	public String getShapefileUrl(BoundingBox bBox, int width, int height)
		throws Exception, ServiceException
	{
		this.bb = bBox;

		// Build the request for the map server
		Element elService = this.toElement()
			.addContent(new Element("imageWidth").setText(width + ""))
			.addContent(new Element("imageHeight").setText(height + ""));
		Element request = AxlRequestBuilder.getRequest(elService, "getShape.xsl");

		// Send the request and get the response as an Element
		ArcIMSClient client = new ArcIMSClient(serverUrl, name, "Extract", request);
		lastResponse = client.getElement();

		// Check if an error was generated on the server
		checkArcImsError();

		// Get the image URL from the ArcXML response
		return XmlUtil.getElement(lastResponse, "/ARCXML/RESPONSE/EXTRACT/OUTPUT")
			.getAttributeValue("url");
	}

	/**
	 * Sends a GET_IMAGE request to the map server to get the legend.
	 *
	 * @param    bBox                a  BoundingBox
	 *
	 * @return  the URL of the image on the server
	 *
	 * @throws   Exception
	 *
	 */
	public String getLegendUrl()
		throws Exception, ServiceException
	{
		// Build the request for the map server
		Element request = AxlRequestBuilder.getRequest(this.toElement(), "getLegend.xsl");

		// Send the request and get the response as an Element
		ArcIMSClient client = new ArcIMSClient(serverUrl, name, request);
		Element lastResponse =  client.getElement();

		checkArcImsError();

		// Get the image URL from the ArcXML response
		return XmlUtil.getElement(lastResponse, "/ARCXML/RESPONSE/IMAGE/LEGEND")
			.getAttributeValue("url");
	}

	public void identify(int layer, int x, int y, int width, int height, int tolerance, String reqFormat)
		throws Exception, ServiceException
	{
		float mapX = bb.getWest() + (bb.getEast() - bb.getWest()) * x / width;
		float mapY = bb.getNorth() - (bb.getNorth() - bb.getSouth()) * y /  height;
		float deltax = (bb.getEast() - bb.getWest()) / width * tolerance / 2;
		float deltay = (bb.getNorth() - bb.getSouth()) / height * tolerance / 2;

		// Build the request for the map server
		Element request = AxlRequestBuilder.getRequest(this.toElement()
			.addContent(new Element("activeLayer").setText(layer + ""))
			.addContent(new BoundingBox(mapY + deltay, mapY - deltay, mapX + deltax, mapX - deltax).toElement()), "identify.xsl");

		// Send the request and get the response as an Element
		ArcIMSClient client = new ArcIMSClient(serverUrl, name, "Query", request);
		lastResponse =  client.getElement();

		checkArcImsError();
	}

	public void setActiveLayer(int layer)
		throws Exception
	{
//		if (!layers.contains(new Integer(layer)))
//			throw new Exception();
		activeLayer = layer;
	}

	public int getActiveLayer() {
		return activeLayer;
	}

	/**
	 * Sets layer visibility
	 *
	 * @param    id                  layer id
	 * @param    visible             layer visibility
	 *
	 */
	public void setLayerVisible(String id, boolean visible) {
		if (visible)
			layers.put(id, "show");
		else
			layers.put(id, "hide");
	}

	/**
	 * Returns an array of all visible layers
	 *
	 * @return   a Vector
	 *
	 */
	public Vector getVisibleLayers() {
		Vector show = new Vector();
		for (Enumeration e = layers.keys(); e.hasMoreElements(); ) {
			Object key = e.nextElement();
			if (layers.get(key) == "show")
				show.add(key);
		}
		return show;
	}

	/**
	 * Returns an element represenation of the status of the map service
	 *
	 * @return   an Element
	 *
	 */
	public Element toElement() {
		// Visible layers
		Element elVisible = new Element("visibleLayers");
		for (Enumeration e = layers.keys(); e.hasMoreElements(); ) {
			Object key = e.nextElement();
			if (layers.get(key) == "show")
				elVisible.addContent(new Element("layer")
										 .setAttribute("internalId", (String)key)
										 .setAttribute("visible", "true"));
			else
				elVisible.addContent(new Element("layer").setAttribute("internalId", (String)key).setAttribute("visible", "false"));
		}

		return new Element("service")
			.setAttribute("type", "ArcIMS")
			.setAttribute("name", this.getName())
			.addContent(this.getInfo())
			.addContent(new Element(MapServices.LAST_RESPONSE_TAG).addContent(this.getLastResponse()))
			.addContent(elVisible)
			.addContent( new Element("envelope").addContent(getBoundingBox().toElement()) );
	}

	public String getErrorMsg() {
		return errorStr;
	}

	/**
	 * Sets the error string and throws a ServiceException if
	 * an error messaged received from the image server
	 *
	 * @throws   ServiceException if an error messaged received from the image
	 *           server
	 *
	 */
	private void checkArcImsError()
		throws Exception
	{
		Element error = XmlUtil.getElement(lastResponse, "/ARCXML/RESPONSE/ERROR");
		if (error != null) {
			errorStr = error.getText();
			throw new ServiceException();
		}
	}

}


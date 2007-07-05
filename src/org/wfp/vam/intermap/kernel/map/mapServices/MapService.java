/**
 * MapServices.java
 *
 * @author Stefano Giaccio
 */

package org.wfp.vam.intermap.kernel.map.mapServices;


import java.util.*;

import org.jdom.*;

import org.wfp.vam.intermap.kernel.map.mapServices.constants.MapServices;

public abstract class MapService
{
	// Map server parameters
	protected String serverUrl;
	protected String name;

	// Map server generic information
	protected Element info;

	protected Hashtable layers = new Hashtable();

	// Last response from the server
	protected Element lastResponse = new Element(MapServices.LAST_RESPONSE_TAG);

	/**
	 * Constructor
	 *
	 * @param    url                 map server URL
	 * @param    name                map service name
	 * @param    type                map service type
	 *
	 */
	public MapService(String mapServerUrl, String serviceName) {
		serverUrl = mapServerUrl;
		name = serviceName;
	}
	
	/**
	 * Method getTitle
	 *
	 * @return   a  String
	 */
	public String getTitle() { return getName(); };

	public abstract int getType();

	public String getServerURL()
	{ return serverUrl; }

	public String getName()
	{ return name; }

	public Element getInfo()
	{ return (Element)info.clone(); }

	public abstract String getImageUrl(BoundingBox bBox, int width, int height) throws ServiceException, Exception;

	public abstract String getGroupImageUrl(BoundingBox bBox, int width, int height, Vector imageNames);
	
	public abstract String getLegendUrl() throws Exception;

	public abstract void identify(int layer, int x, int y, int width, int height, int tolerance, String reqFormat) throws Exception;

//	public abstract void getFieldValue(int layer, int x, int y, int width, int height, int tolerance, String fieldName) throws Exception;
	
	public Element getLastResponse()
	{ return (Element)lastResponse.clone(); }

	public abstract BoundingBox getDefBoundingBox() throws Exception;

	public abstract BoundingBox getBoundingBox();

	public abstract void setActiveLayer(int layer) throws Exception;

	public abstract int getActiveLayer();

	public abstract void setLayerVisible(String id, boolean visible);

	public abstract Vector getVisibleLayers();

	public abstract Element toElement();

}


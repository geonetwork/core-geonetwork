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


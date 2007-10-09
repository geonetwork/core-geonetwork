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

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSOperationType;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSRequest;

/**
 * @author ETj
 */
public class WMSRequestImpl implements WMSRequest
{
	private WMSOperationType _getCapabilities = null; // 1..1
	private WMSOperationType _getMap = null; // 1..1
	private WMSOperationType _getFeatureInfo = null; //0..1
	// TODO: extendedapabilities


	private WMSRequestImpl()
	{}


	public static WMSRequest newInstance()
	{
		return new WMSRequestImpl();
	}

	public static WMSRequest parse(Element eRequest)
	{
		WMSRequestImpl r = new WMSRequestImpl();

		r.setGetCapabilities(WMSFactory.parseOperationType(eRequest.getChild("GetCapabilities")));
		r.setGetMap(WMSFactory.parseOperationType(eRequest.getChild("GetMap")));

		Element gfi = eRequest.getChild("GetFeatureInfo");
		if(gfi != null)
			r.setGetFeatureInfo(WMSFactory.parseOperationType(gfi));

		return r;
	}

	/**
	 * Sets GetCapabilities
	 */
	public void setGetCapabilities(WMSOperationType getCapabilities)
	{
		_getCapabilities = getCapabilities;
	}

	/**
	 * Returns GetCapabilities
	 */
	public WMSOperationType getGetCapabilities()
	{
		return _getCapabilities;
	}

	/**
	 * Sets GetMap
	 */
	public void setGetMap(WMSOperationType getMap)
	{
		_getMap = getMap;
	}

	/**
	 * Returns GetMap
	 */
	public WMSOperationType getGetMap()
	{
		return _getMap;
	}

	/**
	 * Sets GetFeatureInfo
	 */
	public void setGetFeatureInfo(WMSOperationType getFeatureInfo)
	{
		_getFeatureInfo = getFeatureInfo;
	}

	/**
	 * Returns GetFeatureInfo
	 */
	public WMSOperationType getGetFeatureInfo()
	{
		return _getFeatureInfo;
	}

}


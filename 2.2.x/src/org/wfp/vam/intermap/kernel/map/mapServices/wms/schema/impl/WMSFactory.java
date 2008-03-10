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


import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.*;

import org.jdom.Element;
import org.jdom.JDOMException;

/**
 * @author ETj
 */
public class WMSFactory
{

	/**
	 * Method parseDimension
	 */
	public static WMSDimension parseDimension(Element edim)
	{
		return WMSDimensionImpl.parse(edim);
	}

	/**
	 * Method parseDCPType
	 */
	public static WMSDCPType parseDCPType(Element eDCP)
	{
		return WMSDCPTypeImpl.parse(eDCP);
	}

	/**
	 * Method parseOperationType
	 */
	public static WMSOperationType parseOperationType(Element eOpType)
	{
		return WMSOperationTypeImpl.parse(eOpType);
	}

	/**
	 * Method parseRequest
	 */
	public static WMSRequest parseRequest(Element eRequest)
	{
		return WMSRequestImpl.parse(eRequest);
	}

	/**
	 * Method parseMetadataURL
	 */
	public static WMSMetadataURL parseMetadataURL(Element emdu)
	{
		return WMSMetadataURLImpl.parse(emdu);
	}

	/**
	 * Method parseBoundingBox
	 */
	public static WMSBoundingBox parseBoundingBox(Element ebb)
	{
		return WMSBoundingBoxImpl.parse(ebb);
	}

	/**
	 * Method parseGeoBBox111
	 */
	public static WMSEX_GeographicBoundingBox parseGeoBBox111(Element egbb)
	{
		return WMSEX_GeographicBoundingBoxImpl.parse111(egbb);
	}

	/**
	 * Method parseGeoBBox
	 */
	public static WMSEX_GeographicBoundingBox parseGeoBBox(Element egbb)
	{
		return WMSEX_GeographicBoundingBoxImpl.parse(egbb);
	}

	/**
	 * Method parseCapability
	 */
	public static WMSCapability parseCapability(Element capability)
	{
		return WMSCapabilityImpl.parse(capability);
	}

	/**
	 * Method parseCapabilities
	 */
	public static WMSCapabilities parseCapabilities(Element capabilities)
	{
		try
		{
			return WMSCapabilitiesImpl.parse(capabilities);
		}
		catch (JDOMException e)
		{
			System.out.println(e);
			return null;
		}
	}

	/**
	 * Method parseLegendURL
	 */
	public static WMSLegendURL parseLegendURL(Element eLegendUrl)
	{
		return WMSLegendURLImpl.parse(eLegendUrl);
	}

	/**
	 * Method parseStyle
	 * @return   a  WMSStyle
	 */
	public static WMSStyle parseStyle(Element eStyle)
	{
		return WMSStyleImpl.parse(eStyle);
	}

	/**
	 * Method parseWMSService
	 */
	public static WMSService parseService(Element service)
	{
		return WMSServiceImpl.parse(service);
	}

	/**
	 * Method parseOnlineResource
	 */
	public static WMSOnlineResource parseOnlineResource(Element onlineResource)
	{
		return WMSOnlineResourceImpl.parse(onlineResource);
	}

	static public WMSLayer createLayer()
	{
		return WMSLayerImpl.newInstance();
	}

	static public WMSLayer parseLayer(Element eLayer)
	{
		return WMSLayerImpl.parse(eLayer);
	}

}



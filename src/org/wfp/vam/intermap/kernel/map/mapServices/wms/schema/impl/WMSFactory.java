/**
 * WMCFactory.java
 *
 */

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



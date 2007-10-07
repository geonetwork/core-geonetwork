/**
 * WMSRequestImpl.java
 *
 */

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


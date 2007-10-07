/**
 * WMSCapabilityImpl.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSCapability;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSLayer;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSRequest;

/**
 * @author ETj
 */
public class WMSCapabilityImpl implements WMSCapability
{
	private WMSRequest _request = null;
//	private WMSException _exception;
//	private List<WMSExtendedCapabilities> _extendedCapabilities = new ArrayList<WMSExtendedCapabilities>();
	private WMSLayer _layer = null; // Layer 0..1

	private WMSCapabilityImpl()
	{}

	public static WMSCapability newInstance()
	{
		return new WMSCapabilityImpl();
	}

	public static WMSCapability parse(Element eCapability)
	{
		WMSCapabilityImpl ci = new WMSCapabilityImpl();

		ci.setRequest(WMSFactory.parseRequest(eCapability.getChild("Request")));
		ci.setLayer(WMSFactory.parseLayer(eCapability.getChild("Layer")));

		return ci;
	}

	/**
	 * Sets Request
	 */
	public void setRequest(WMSRequest request)
	{
		_request = request;
	}

	/**
	 * Returns Request
	 */
	public WMSRequest getRequest()
	{
		return _request;
	}
//
//	/**
//	 * Sets Exception
//	 *
//	 * @param    Exception           a  WMSException
//	 */
//	public void setException(WMSException exception)
//	{
//		_exception = exception;
//	}
//
//	/**
//	 * Returns Exception
//	 *
//	 * @return    a  WMSException
//	 */
//	public WMSException getException()
//	{
//		return _exception;
//	}
//
//	/**
//	 * Sets ExtendedCapabilities
//	 *
//	 * @param    ExtendedCapabilitiesa  List<WMSExtendedCapabilities>
//	 */
//	public void setExtendedCapabilities(List<WMSExtendedCapabilities> extendedCapabilities)
//	{
//		_extendedCapabilities = extendedCapabilities;
//	}
//
//	/**
//	 * Returns ExtendedCapabilities
//	 *
//	 * @return    a  List<WMSExtendedCapabilities>
//	 */
//	public List<WMSExtendedCapabilities> getExtendedCapabilities()
//	{
//		return _extendedCapabilities;
//	}


	public void setLayer(WMSLayer layer)
	{
		_layer = layer;
	}

	public WMSLayer getLayer()
	{
		return _layer;
	}

}


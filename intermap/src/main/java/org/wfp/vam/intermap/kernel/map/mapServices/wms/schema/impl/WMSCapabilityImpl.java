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


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
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSDCPType;

/**
 * @author ETj
 */
public class WMSDCPTypeImpl implements WMSDCPType
{
	/** Shortcut info. Not a real MO, because hierarchy is quite simple here */
	private String _httpGetHref = null;
	/** Shortcut info. Not a real MO, because hierarchy is quite simple here */
	private String _httpPostHref = null;

	private WMSDCPTypeImpl()
	{}

	public static WMSDCPType newInstance()
	{
		return new WMSDCPTypeImpl();
	}

	public static WMSDCPType parse(Element eDCPType)
	{
		WMSDCPTypeImpl dti = new WMSDCPTypeImpl();

		Element eHTTP = eDCPType.getChild("HTTP");


		Element eGet = eHTTP.getChild("Get"); // required
		String gor100 = eGet.getAttributeValue("onlineResource"); // WMS 1.0.0 - required
		if(gor100 != null)
			dti.setHttpGetHref(gor100);
		else
		{
			Element eGetOR = eGet.getChild("OnlineResource");
			dti.setHttpGetHref(WMSFactory.parseOnlineResource(eGetOR).getHref());
		}

		Element ePost = eHTTP.getChild("Post"); // optional
		if(ePost != null)
		{
			String por100 = ePost.getAttributeValue("onlineResource"); // WMS 1.0.0 - required
			if(por100 != null)
				dti.setHttpPostHref(por100);
			else
			{
				Element ePostOR = ePost.getChild("OnlineResource");
				dti.setHttpPostHref(WMSFactory.parseOnlineResource(ePostOR).getHref());
			}
		}

		return dti;
	}

	/**
	 * Sets HttpGetOR
	 */
	public void setHttpGetHref(String httpGetHref)
	{
		_httpGetHref = httpGetHref;
	}

	/**
	 * Returns HttpGetOR
	 */
	public String getHttpGetHref()
	{
		return _httpGetHref;
	}

	/**
	 * Sets HttpPostOR
	 */
	public void setHttpPostHref(String httpPostHref)
	{
		_httpPostHref = httpPostHref;
	}

	/**
	 * Returns HttpPostOR
	 */
	public String getHttpPostHref()
	{
		return _httpPostHref;
	}


}


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
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSOnlineResource;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSService;

/**
 * @author ETj
 */
public class WMSServiceImpl implements WMSService
{
	public final static String NAME_WMS = "OGC:WMS";

	private String _name = "WMS";
	private String _title = null; // 1..1
	private String _abstract;
	private WMSOnlineResource _onlineResource = null;

	// TODO keywordlist, contactInformation, fees, accessconstraints, Layerlimit, maxwidth, maxheight

	private WMSServiceImpl()
	{}

	static public WMSService newInstance()
	{
		return new WMSServiceImpl();
	}

	public static WMSService parse(Element eService)
	{
		WMSServiceImpl service = new WMSServiceImpl();

		service.setName(eService.getChildText("Name"));
		service.setTitle(eService.getChildText("Title"));
		service.setAbstract(eService.getChildText("Abstract"));
		service.setOnlineResource(eService.getChild("OnlineResource"));

		return service;
	}

	public void setName(String name)
	{
		if( ! NAME_WMS.equals(name))
			System.out.println("*** Expected value '" + NAME_WMS + "' for element 'Name'. Found '"+name+"'");
//			throw new IllegalArgumentException("Name element must be '"+NAME_WMS+"'");

		_name = name;
	}

	/**
	 * Returns Name
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Sets Title
	 */
	public void setTitle(String title)
	{
		_title = title;
	}

	/**
	 * Returns Title
	 */
	public String getTitle()
	{
		return _title;
	}

	/**
	 * Sets Abstract
	 */
	public void setAbstract(String abs)
	{
		_abstract = abs;
	}

	/**
	 * Returns Abstract
	 */
	public String getAbstract()
	{
		return _abstract;
	}

	private void setOnlineResource(Element onlineResource)
	{
		WMSOnlineResource wor = WMSFactory.parseOnlineResource(onlineResource);
		setOnlineResource(wor);
	}

	/**
	 * Sets GetOnlineResource
	 */
	public void setOnlineResource(WMSOnlineResource onlineResource)
	{
		_onlineResource = onlineResource;
	}

	/**
	 * Returns GetOnlineResource
	 */
	public WMSOnlineResource getOnlineResource()
	{
		return _onlineResource;
	}

}


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
//==============================================================================

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.om;

/**
 * @author ETj
 */
public class WMCServer
{
	public final static String SERVICE_OGC_WMS = "OGC:WMS";

	private String _service = SERVICE_OGC_WMS; // req
	private String _version = null; //req
	private String _title = null; // optional

	private WMCOnlineResource _onlineResource = null;


	private WMCServer()
	{}

	public static WMCServer newInstance()
	{
		return new WMCServer();
	}

	/***************************************************************************
	 * OnlineResource
	 */
	public void setOnlineResource(WMCOnlineResource onlineResource)
	{
		_onlineResource = onlineResource;
	}

	public WMCOnlineResource getOnlineResource()
	{
		return _onlineResource;
	}

	/***************************************************************************
	 * Service
	 */
	public void setService(String service)
	{
//		if(! SERVICE_OGC_WMS.equals(service) )
//			System.out.println("WARNING: setting OGC server service to non-standard value '"+service+"'");

		_service = service;
	}

	public String getService()
	{
		return _service;
	}

	/***************************************************************************
	 * Version
	 */
	public void setVersion(String version)
	{
		_version = version;
	}

	public String getVersion()
	{
		return _version;
	}

	/***************************************************************************
	 * Title
	 */
	public void setTitle(String title)
	{
		_title = title;
	}

	public String getTitle()
	{
		return _title;
	}

}

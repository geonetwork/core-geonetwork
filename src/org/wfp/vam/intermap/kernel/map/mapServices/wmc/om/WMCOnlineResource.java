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
public class WMCOnlineResource 
{
	private String _type = "simple"; // fixed
	private String _href = null;

	private WMCOnlineResource()
	{}

	public static WMCOnlineResource newInstance()
	{
		return new WMCOnlineResource();
	}

	public void setHref(String href)
	{
		_href = href;
	}

	public String getHref()
	{
		return _href;
	}

	public String getType() {
		return _type;
	}	

}

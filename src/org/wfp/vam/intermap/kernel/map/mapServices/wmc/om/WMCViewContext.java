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
public class WMCViewContext 
{
	// Attributes
	private String _version = "1.1.0";
	private String _id = null;

	// Child nodes
	private WMCGeneral 	_general = null;
	private WMCLayerList _layerList = null;

	private WMCViewContext()
	{
	}

	public static WMCViewContext newInstance()
	{
		return new WMCViewContext();
	}

	/***************************************************************************
	 * LayerList
	 */
	public void setLayerList(WMCLayerList layerList)
	{
		_layerList = layerList;
	}

	public WMCLayerList getLayerList()
	{
		return _layerList;
	}

	/***************************************************************************
	 * General
	 */
	public void setGeneral(WMCGeneral general)
	{
		_general = general;
	}

	public WMCGeneral getGeneral()
	{
		return _general;
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
	 * Id
	 */
	public void setId(String id)
	{
		_id = id;
	}

	public String getId()
	{
		return _id;
	}
}

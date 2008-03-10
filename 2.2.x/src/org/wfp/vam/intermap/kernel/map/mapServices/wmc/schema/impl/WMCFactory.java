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

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl;

import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.*;

import org.jdom.Element;

/**
 * @author ETj
 */
public class WMCFactory
{
	public static WMCOnlineResource parseOnlineResource(Element eor)
	{
		return WMCOnlineResourceImpl.parse(eor);
	}

	public static WMCServer parseServer(Element eServ)
	{
		return WMCServerImpl.parse(eServ);
	}

	public static WMCLayer parseLayer(Element el)
	{
		return WMCLayerImpl.parse(el);
	}

	public static WMCExtension parseExtension(Element eext)
	{
		return WMCExtensionImpl.parse(eext);
	}

	public static WMCBoundingBox parseBoundingBox(Element ebb)
	{
		return WMCBoundingBoxImpl.parse(ebb);
	}

	public static WMCWindow parseWindow(Element ewin)
	{
		return WMCWindowImpl.parse(ewin);
	}

	public static WMCLayerList parseLayerList(Element ell)
	{
		return WMCLayerListImpl.parse(ell);
	}

	public static WMCGeneral parseGeneral(Element eg)
	{
		return WMCGeneralImpl.parse(eg);
	}

	static public WMCViewContext parseViewContext(Element vc)
	{
		return WMCViewContextImpl.parse(vc);
	}

	static public WMCViewContext createWMCViewContext()
	{
		return WMCViewContextImpl.newInstance();
	}

	static public WMCLayer createWMCLayer()
	{
		return WMCLayerImpl.newInstance();
	}
}


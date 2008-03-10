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
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSLegendURL;

/**
 * @author ETj
 */
public class WMSLegendURLImpl extends WMSURLAbs implements WMSLegendURL
{
	private int _width  = -1;
	private int _height = -1;

	private WMSLegendURLImpl()
	{}

	public static WMSLegendURL newWMSLegendURL()
	{
		return new WMSLegendURLImpl();
	}

	public static WMSLegendURL parse(Element eLegendURL)
	{
		WMSLegendURLImpl legendURL = new WMSLegendURLImpl();

		WMSURLAbs.parse(legendURL, eLegendURL);

		String sw = eLegendURL.getAttributeValue("width");
		if(sw != null)
			legendURL.setWidth(Integer.parseInt(sw));

		String sh = eLegendURL.getAttributeValue("height");
		if(sh != null)
			legendURL.setHeight(Integer.parseInt(sh));

		return legendURL;
	}


	/**
	 * Sets Width
	 */
	public void setWidth(int width)
	{
		_width = width;
	}

	/**
	 * Returns Width
	 */
	public int getWidth()
	{
		return _width;
	}

	/**
	 * Sets Height
	 */
	public void setHeight(int height)
	{
		_height = height;
	}

	/**
	 * Returns Height
	 */
	public int getHeight()
	{
		return _height;
	}

}


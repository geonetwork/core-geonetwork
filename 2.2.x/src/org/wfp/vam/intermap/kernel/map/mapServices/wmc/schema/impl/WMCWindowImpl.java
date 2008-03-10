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

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCWindow;

/**
 * @author ETj
 */
public class WMCWindowImpl implements WMCWindow
{
	private int _width  = -1;
	private int _height = -1;

	private WMCWindowImpl()
	{}

	public static WMCWindow newInstance()
	{
		return new WMCWindowImpl();
	}

	/**
	 * Method parse
	 */
	public static WMCWindow parse(Element ewin)
	{
		WMCWindowImpl win = new WMCWindowImpl();

		win.setWidth(Integer.parseInt(ewin.getAttributeValue("width")));
		win.setHeight(Integer.parseInt(ewin.getAttributeValue("height")));

		return win;
	}

	/***************************************************************************
	 * Method setHeight
	 */
	public void setHeight(int height)
	{
		_height = height;
	}

	public int getHeight()
	{
		return _height;
	}

	/***************************************************************************
	 * Method setWidth
	 */
	public void setWidth(int width)
	{
		_width = width;
	}

	public int getWidth()
	{
		return _width;
	}

	/***************************************************************************
	 */
	public Element toElement(String name)
	{
		if( _width == -1 )
			throw new IllegalStateException(name + "/@Width is missing");

		if( _height == -1 )
			throw new IllegalStateException(name + "/@Height is missing");

		return new Element(name)
			.setAttribute("width", ""+_width)
			.setAttribute("height", ""+_height);
	}

}


/**
 * WindowType.java
 *
 */

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


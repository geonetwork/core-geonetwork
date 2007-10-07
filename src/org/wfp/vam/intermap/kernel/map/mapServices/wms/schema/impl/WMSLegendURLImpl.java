/**
 * WMSLegendURLImpl.java
 *
 */

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


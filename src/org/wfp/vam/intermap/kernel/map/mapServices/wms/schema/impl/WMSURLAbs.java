/**
 * WMSURL.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSFormat;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSOnlineResource;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSURLif;

/**
 * @author ETj
 */
public abstract class WMSURLAbs implements WMSURLif
{
	protected WMSFormat _format = null;
	protected WMSOnlineResource _onlineResource = null;

	protected static void parse(WMSURLAbs url, Element eURL)
	{
		url.setFormat(eURL.getChildText("Format"));
		url.setOnlineResource(WMSFactory.parseOnlineResource(eURL.getChild("OnlineResource")));
	}

	/**
	 * Sets Format
	 */
	public void setFormat(String format)
	{
		WMSFormat oformat = WMSFormat.parse(format);
		setFormat(oformat);
	}

	public void setFormat(WMSFormat format)
	{
		_format = format;
	}

	/**
	 * Returns Format
	 */
	public WMSFormat getFormat()
	{
		return _format;
	}

	/**
	 * Sets OnlineResource
	 */
	public void setOnlineResource(WMSOnlineResource onlineResource)
	{
		_onlineResource = onlineResource;
	}

	/**
	 * Returns OnlineResource
	 */
	public WMSOnlineResource getOnlineResource()
	{
		return _onlineResource;
	}

}


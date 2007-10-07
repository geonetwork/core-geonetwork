/**
 * WMSStyleSheetURLImpl.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;


import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSStyleURL;

/**
 * @author ETj
 */
public class WMSStyleURLImpl extends WMSURLAbs implements WMSStyleURL
{
	private WMSStyleURLImpl()
	{}

	public static WMSStyleURLImpl newInstance()
	{
		return new WMSStyleURLImpl();
	}

	public static WMSStyleURLImpl parse(Element eURL)
	{
		WMSStyleURLImpl url = new WMSStyleURLImpl();
		WMSURLAbs.parse(url, eURL);
		return url;
	}
}


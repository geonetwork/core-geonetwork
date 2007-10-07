/**
 * WMSStyleSheetURLImpl.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSStyleSheetURL;

/**
 * @author ETj
 */
public class WMSStyleSheetURLImpl extends WMSURLAbs implements WMSStyleSheetURL
{
	private WMSStyleSheetURLImpl()
	{}

	public static WMSStyleSheetURLImpl newInstance()
	{
		return new WMSStyleSheetURLImpl();
	}

	public static WMSStyleSheetURLImpl parse(Element eURL)
	{
		WMSStyleSheetURLImpl url = new WMSStyleSheetURLImpl();
		WMSURLAbs.parse(url, eURL);
		return url;
	}
}


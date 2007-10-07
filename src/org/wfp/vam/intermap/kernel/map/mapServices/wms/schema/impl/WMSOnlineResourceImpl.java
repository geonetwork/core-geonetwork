/**
 * OnlineResourceType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSOnlineResource;

/**
 * @author ETj
 */
public class WMSOnlineResourceImpl implements WMSOnlineResource
{
	private String _type = "simple"; // fixed
	private String _href = null;

	private WMSOnlineResourceImpl()
	{}

	public static WMSOnlineResource newInstance()
	{
		return new WMSOnlineResourceImpl();
	}

	/**
	 * Method parse
	 */
	public static WMSOnlineResource parse(Element onlineResource)
	{
		WMSOnlineResourceImpl wor = new WMSOnlineResourceImpl();

		wor.setHref(onlineResource.getAttributeValue("href", WMSNamespaceRegistry.getXLink()));

		return wor;
	}

	public void setHref(String href)
	{
		_href = href;
	}

	public String getHref()
	{
		return _href;
	}

	public Element toElement(String name)
	{
		Element ret = new Element(name)
			.setAttribute("type", _type, WMSNamespaceRegistry.getXLink());

		if( _href != null)
			ret.setAttribute("href", _href, WMSNamespaceRegistry.getXLink());

		return ret;
	}

}


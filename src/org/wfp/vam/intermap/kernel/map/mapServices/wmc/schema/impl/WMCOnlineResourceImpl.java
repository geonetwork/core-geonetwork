/**
 * OnlineResourceType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl;

import org.jdom.Element;
import org.jdom.Namespace;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCOnlineResource;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl.WMSNamespaceRegistry;

/**
 * @author ETj
 */
public class WMCOnlineResourceImpl implements WMCOnlineResource
{
	private String _type = "simple"; // fixed
	private String _href = null;

	private WMCOnlineResourceImpl()
	{}

	public static WMCOnlineResource newInstance()
	{
		return new WMCOnlineResourceImpl();
	}

	/**
	 * Method parse
	 */
	public static WMCOnlineResource parse(Element onlineResource)
	{
		WMCOnlineResource wor = newInstance();

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
		Namespace nsXLink = Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink");

		Element ret = new Element(name)
			.setAttribute("type", _type, nsXLink);

		if( _href != null)
			ret.setAttribute("href", _href, nsXLink);

		return ret;
	}

}


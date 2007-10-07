/**
 * DCPTypeImpl.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSDCPType;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSOnlineResource;

/**
 * @author ETj
 */
public class WMSDCPTypeImpl implements WMSDCPType
{
	/** Shortcut info. Not a real MO, because hierarchy is quite simple here */
	private String _httpGetHref = null;
	/** Shortcut info. Not a real MO, because hierarchy is quite simple here */
	private String _httpPostHref = null;

	private WMSDCPTypeImpl()
	{}

	public static WMSDCPType newInstance()
	{
		return new WMSDCPTypeImpl();
	}

	public static WMSDCPType parse(Element eDCPType)
	{
		WMSDCPTypeImpl dti = new WMSDCPTypeImpl();

		Element eHTTP = eDCPType.getChild("HTTP");


		Element eGet = eHTTP.getChild("Get"); // required
		String gor100 = eGet.getAttributeValue("onlineResource"); // WMS 1.0.0 - required
		if(gor100 != null)
			dti.setHttpGetHref(gor100);
		else
		{
			Element eGetOR = eGet.getChild("OnlineResource");
			dti.setHttpGetHref(WMSFactory.parseOnlineResource(eGetOR).getHref());
		}

		Element ePost = eHTTP.getChild("Post"); // optional
		if(ePost != null)
		{
			String por100 = ePost.getAttributeValue("onlineResource"); // WMS 1.0.0 - required
			if(por100 != null)
				dti.setHttpPostHref(por100);
			else
			{
				Element ePostOR = ePost.getChild("OnlineResource");
				dti.setHttpPostHref(WMSFactory.parseOnlineResource(ePostOR).getHref());
			}
		}

		return dti;
	}

	/**
	 * Sets HttpGetOR
	 */
	public void setHttpGetHref(String httpGetHref)
	{
		_httpGetHref = httpGetHref;
	}

	/**
	 * Returns HttpGetOR
	 */
	public String getHttpGetHref()
	{
		return _httpGetHref;
	}

	/**
	 * Sets HttpPostOR
	 */
	public void setHttpPostHref(String httpPostHref)
	{
		_httpPostHref = httpPostHref;
	}

	/**
	 * Returns HttpPostOR
	 */
	public String getHttpPostHref()
	{
		return _httpPostHref;
	}


}


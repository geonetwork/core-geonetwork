/**
 * WMSService.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSOnlineResource;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSService;

/**
 * @author ETj
 */
public class WMSServiceImpl implements WMSService
{
	public final static String NAME_WMS = "OGC:WMS";

	private String _name = "WMS";
	private String _title = null; // 1..1
	private String _abstract;
	private WMSOnlineResource _onlineResource = null;

	// TODO keywordlist, contactInformation, fees, accessconstraints, Layerlimit, maxwidth, maxheight

	private WMSServiceImpl()
	{}

	static public WMSService newInstance()
	{
		return new WMSServiceImpl();
	}

	public static WMSService parse(Element eService)
	{
		WMSServiceImpl service = new WMSServiceImpl();

		service.setName(eService.getChildText("Name"));
		service.setTitle(eService.getChildText("Title"));
		service.setAbstract(eService.getChildText("Abstract"));
		service.setOnlineResource(eService.getChild("OnlineResource"));

		return service;
	}

	public void setName(String name)
	{
		if( ! NAME_WMS.equals(name))
			System.out.println("*** Expected value '" + NAME_WMS + "' for element 'Name'. Found '"+name+"'");
//			throw new IllegalArgumentException("Name element must be '"+NAME_WMS+"'");

		_name = name;
	}

	/**
	 * Returns Name
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Sets Title
	 */
	public void setTitle(String title)
	{
		_title = title;
	}

	/**
	 * Returns Title
	 */
	public String getTitle()
	{
		return _title;
	}

	/**
	 * Sets Abstract
	 */
	public void setAbstract(String abs)
	{
		_abstract = abs;
	}

	/**
	 * Returns Abstract
	 */
	public String getAbstract()
	{
		return _abstract;
	}

	private void setOnlineResource(Element onlineResource)
	{
		WMSOnlineResource wor = WMSFactory.parseOnlineResource(onlineResource);
		setOnlineResource(wor);
	}

	/**
	 * Sets GetOnlineResource
	 */
	public void setOnlineResource(WMSOnlineResource onlineResource)
	{
		_onlineResource = onlineResource;
	}

	/**
	 * Returns GetOnlineResource
	 */
	public WMSOnlineResource getOnlineResource()
	{
		return _onlineResource;
	}

}


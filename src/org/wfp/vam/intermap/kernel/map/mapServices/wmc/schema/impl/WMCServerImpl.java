/**
 * ServerType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl;

import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCOnlineResource;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCServer;

/**
 * @author ETj
 */
public class WMCServerImpl implements WMCServer
{
	private String _service = null; // req
	private String _version = null; //req
	private String _title = null; // optional

	private WMCOnlineResource _onlineResource = null;


	private WMCServerImpl()
	{}

	public static WMCServer newInstance()
	{
		return new WMCServerImpl();
	}


	public static WMCServer parse(Element eserv)
	{
		WMCServerImpl serv = new WMCServerImpl();

		serv.setService(eserv.getAttributeValue("service"));
		serv.setVersion(eserv.getAttributeValue("version"));
		serv.setTitle(eserv.getAttributeValue("title"));

		serv.setOnlineResource(WMCFactory.parseOnlineResource(eserv.getChild("OnlineResource")));

		return serv;
	}

	/***************************************************************************
	 * Method addNewOnlineResource
	 */
	public WMCOnlineResource addNewOnlineResource()
	{
		if(_onlineResource != null)
			throw new IllegalStateException("An OnlineResource element already exists");

		_onlineResource = WMCOnlineResourceImpl.newInstance();

		return _onlineResource;
	}

	/**
	 * Sets OnlineResource
	 */
	public void setOnlineResource(WMCOnlineResource onlineResource)
	{
		_onlineResource = onlineResource;
	}

	/**
	 * Returns OnlineResource
	 */
	public WMCOnlineResource getOnlineResource()
	{
		return _onlineResource;
	}

	/***************************************************************************
	 * Sets Service
	 */
	public void setService(String service)
	{
		_service = service;
	}

	/**
	 * Returns Service
	 */
	public String getService()
	{
		return _service;
	}

	/***************************************************************************
	 * Sets Version
	 */
	public void setVersion(String version)
	{
		_version = version;
	}

	/**
	 * Returns Version
	 */
	public String getVersion()
	{
		return _version;
	}

	/***************************************************************************
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


	/***************************************************************************
	 * Method toElement
	 */
	public Element toElement(String name)
	{
		if(_onlineResource == null)
			throw new IllegalStateException(name + "/OnlineResource is missing");

		if(_service == null)
			throw new IllegalStateException(name + "/service is missing");

		if(_version == null)
			throw new IllegalStateException(name + "/version is missing");

		Element ret = new Element(name)
			.setAttribute("service", _service)
			.setAttribute("version", _version)
			.addContent(_onlineResource.toElement("OnlineResource"));

		if (_title != null)
			ret.setAttribute("title", _title);

		return ret;
	}

}


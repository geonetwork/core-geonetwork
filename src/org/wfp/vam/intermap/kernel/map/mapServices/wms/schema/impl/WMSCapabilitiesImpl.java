/**
 * WMSCapabilitiesDocument.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSCapabilities;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSCapability;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSService;


/**
 * @author ETj
 */
public class WMSCapabilitiesImpl implements WMSCapabilities
{
	private WMSVer _version = DEFVERSION;
	private String _updateSequence = null;

	private WMSService _service = null;
	private WMSCapability _capability = null;

	private WMSCapabilitiesImpl()
	{
	}

	public static WMSCapabilities newInstance()
	{
		return new WMSCapabilitiesImpl();
	}

	/**
	 * Method parse
	 */
	public static WMSCapabilities parse(Element capa) throws JDOMException
	{
		WMSCapabilitiesImpl capabilities = new WMSCapabilitiesImpl();

		capabilities.setVersion(capa.getAttributeValue("version"));
		capabilities.setUpdateSequence(capa.getAttributeValue("updateSequence"));

		capabilities.setService(WMSFactory.parseService(capa.getChild("Service")));
		capabilities.setCapability(WMSFactory.parseCapability(capa.getChild("Capability")));

		return capabilities;
	}

	/**
	 * Method setService
	 */
	public void setService(WMSService service)
	{
		_service = service;
	}

	public WMSService getService()
	{
		return _service;
	}

	/**
	 * Sets Capability
	 */
	public void setCapability(WMSCapability capability)
	{
		_capability = capability;
	}

	/**
	 * Returns Capability
	 */
	public WMSCapability getCapability()
	{
		return _capability;
	}


	/**
	 * Sets Version
	 */
	public void setVersion(String version)
	{
		// We'll try to deal nicely with any version
		WMSVer v = WMSVer.parse(version);
		if( v == null)
		{
			System.out.println("WMSCapabilities: ERROR: unknown version '"+version+"'");
		}

		setVersion(v);
	}

	public void setVersion(WMSVer version)
	{
		_version = version;
	}

	/**
	 * Returns Version
	 */
	public WMSVer getVersion()
	{
		return _version;
	}

	/**
	 * Sets UpdateSequence
	 */
	public void setUpdateSequence(String updateSequence)
	{
		_updateSequence = updateSequence;
	}

	/**
	 * Returns UpdateSequence
	 */
	public String getUpdateSequence()
	{
		return _updateSequence;
	}


}


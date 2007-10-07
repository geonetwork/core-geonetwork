/**
 * ViewContextType.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl;

import org.jdom.Document;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCGeneral;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCLayerList;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCViewContext;

/**
 * @author ETj
 */
public class WMCViewContextImpl implements WMCViewContext
{
	// Attributes
	private String _version = "1.1.0";
	private String _id = null;

	// Child nodes
	private WMCGeneral 	_general = null;
	private WMCLayerList _layerList = null;

	private WMCViewContextImpl()
	{
	}

	/**
	 * Method parse
	 */
	public static WMCViewContext parse(Element evc)
	{
		WMCViewContextImpl vc = new WMCViewContextImpl();

		vc.setVersion(evc.getAttributeValue("version"));
		vc.setId(evc.getAttributeValue("id"));

		vc.setGeneral(WMCFactory.parseGeneral(evc.getChild("General")));
		vc.setLayerList(WMCFactory.parseLayerList(evc.getChild("LayerList")));

		return vc;
	}

	/**
	 * Method newInstance
	 */
	public static WMCViewContext newInstance()
	{
		return new WMCViewContextImpl();
	}

	/**
	 * Method addNewLayerList
	 */
	public WMCLayerList addNewLayerList()
	{
		if(_layerList != null)
			throw new IllegalStateException("A LayerList element already exists");

		_layerList = WMCLayerListImpl.newInstance();
		return _layerList;
	}

	public void setLayerList(WMCLayerList layerList)
	{
		_layerList = layerList;
	}

	public WMCLayerList getLayerList()
	{
		return _layerList;
	}

	/***************************************************************************
	 * Method addNewGeneral
	 */
	public WMCGeneral addNewGeneral()
	{
		if(_general != null)
			throw new IllegalStateException("A General element already exists");

		_general = WMCGeneralImpl.newInstance();
		return _general;
	}

	public void setGeneral(WMCGeneral general)
	{
		_general = general;
	}

	public WMCGeneral getGeneral()
	{
		return _general;
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
	 * Sets Id
	 */
	public void setId(String id)
	{
		_id = id;
	}

	/**
	 * Returns Id
	 */
	public String getId()
	{
		return _id;
	}

	public Document getContextDocument()
	{
		Element e = toElement();
		return new Document(e);
	}

	public Element toElement()
	{
		if(_general == null)
			throw new IllegalStateException("ViewContext/General is missing");

		if(_layerList == null)
			throw new IllegalStateException("ViewContext/LayerList is missing");

		if(_id == null)
			throw new IllegalStateException("ViewContext/@id is missing");

		return new Element("ViewContext")
			.setAttribute("version", _version)
			.setAttribute("id", _id)
			.addContent(_general.toElement("General"))
			.addContent(_layerList.toElement("LayerList"));
	}
}


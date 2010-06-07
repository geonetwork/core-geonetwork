//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
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

        Namespace NS_WMC = Namespace.getNamespace("http://www.opengis.net/context");
        Namespace NS_XSI = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        Namespace NS_XLINK = Namespace.getNamespace("xlink","http://www.w3.org/1999/xlink");

        Element viewContext =  new Element("ViewContext",NS_WMC);

        viewContext.setAttribute("version", _version)
            .setAttribute("id", _id)
            .setAttribute("schemaLocation", "http://www.opengis.net/context http://schemas.opengis.net/context/1.1.0/context.xsd", NS_XSI)
            .addContent(_general.toElement("General"))
            .addContent(_layerList.toElement("LayerList"));

        viewContext.addNamespaceDeclaration(NS_XLINK);
        viewContext.addNamespaceDeclaration(NS_XSI);

        return viewContext;
	}
}


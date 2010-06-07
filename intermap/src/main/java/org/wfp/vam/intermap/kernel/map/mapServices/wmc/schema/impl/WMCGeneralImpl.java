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


import org.jdom.Element;
import org.jdom.Namespace;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCBoundingBox;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCExtension;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCGeneral;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCWindow;

/**
 * @author ETj
 */
public class WMCGeneralImpl implements WMCGeneral
{
	private WMCWindow _window = null; // 0..1
	private WMCBoundingBox _boundingBox = null; // 1..1
	private String _title = null; // 1..1
//	private WMCKeywordList _keywordList = null; // TODO
	private String _abstract = null;
//	private WMCUrl _logoURL = null; // TODO
//	private WMCUrl _descriptionURL = null; // TODO
//	private WMCContactInformation _contactInformation = null; // TODO
	private WMCExtension _extension = null;


	private WMCGeneralImpl()
	{
	}

	/**
	 * Method newInstance
	 */
	public static WMCGeneral newInstance()
	{
		return new WMCGeneralImpl();
	}

	/**
	 * Method parse
	 */
	public static WMCGeneral parse(Element eg)
	{
		WMCGeneralImpl general = new WMCGeneralImpl();

		general.setWindow(WMCFactory.parseWindow(eg.getChild("Window")));
		general.setBoundingBox(WMCFactory.parseBoundingBox(eg.getChild("BoundingBox")));
		general.setTitle(eg.getChildText("Title"));
		general.setAbstract(eg.getChildText("Abstract"));

		Element eext = eg.getChild("Extension");
		if(eext != null)
			general.setExtension(WMCFactory.parseExtension(eext));

		return general;
	}

	/***************************************************************************
	 * Window
	 */
	public WMCWindow addNewWindow()
	{
		if(_window != null)
			throw new IllegalStateException("A Window element already exists");

		_window = WMCWindowImpl.newInstance();

		return _window;
	}

	/**
	 * Sets Window
	 */
	public void setWindow(WMCWindow window)
	{
		_window = window;
	}

	/**
	 * Returns Window
	 */
	public WMCWindow getWindow()
	{
		return _window;
	}

	/***************************************************************************
	 * BoundingBox
	 */
	public WMCBoundingBox addNewBoundingBox()
	{
		if(_boundingBox != null)
			throw new IllegalStateException("A BoundingBox element already exists");

		_boundingBox = WMCBoundingBoxImpl.newInstance();
		return _boundingBox;
	}

	/**
	 * Sets BoundingBox
	 */
	public void setBoundingBox(WMCBoundingBox boundingBox)
	{
		_boundingBox = boundingBox;
	}

	/**
	 * Returns BoundingBox
	 */
	public WMCBoundingBox getBoundingBox()
	{
		return _boundingBox;
	}


	/***************************************************************************
	 * Title
	 */
	public void setTitle(String title)
	{
		if(_title != null)
			throw new IllegalStateException("A Title element already exists");

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
	 * Abstract
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

	/***************************************************************************
	 * Extension
	 */
	public WMCExtension addNewExtension()
	{
		if(_extension != null)
			throw new IllegalStateException("An Extension element already exists");

		_extension = WMCExtensionImpl.newInstance();

		return _extension;
	}

	/**
	 * Sets Extension
	 */
	public void setExtension(WMCExtension extension)
	{
		_extension = extension;
	}

	/**
	 * Returns Extension
	 */
	public WMCExtension getExtension()
	{
		return _extension;
	}


	/***************************************************************************
	 * Method toElement
	 */
	public Element toElement(String name)
	{
	    if(_title == null)
			throw new IllegalStateException(name + "/Title is missing");

		if(_boundingBox == null)
			throw new IllegalStateException(name + "/BoundingBox is missing");

        Namespace NS_WMC = Namespace.getNamespace("http://www.opengis.net/context");
		Element ret = new Element(name, NS_WMC);

        if( _window != null)
			ret.addContent(_window.toElement("Window"));

        ret.addContent(_boundingBox.toElement("BoundingBox"));
	    ret.addContent(new Element("Title", NS_WMC).setText(_title));


		// TODO add keywordlist

		if(_abstract != null )
			ret.addContent(new Element("Abstract").setText(_abstract));

		// TODO add logourl
		// TODO add descriptionurl
		// TODO add contactinformation

		if(_extension != null )
			ret.addContent(_extension.toElement("Extension"));

		return ret;
	}

}


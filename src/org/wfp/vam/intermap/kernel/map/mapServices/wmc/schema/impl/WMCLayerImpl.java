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
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCExtension;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCLayer;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCServer;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl.Utils;

/**
 * @author ETj
 */
public class WMCLayerImpl implements WMCLayer
{
	private boolean _queryable;
	private boolean _hidden;

	private WMCServer _server = null; // 1..1
	private String _name = null; // 1..1
	private String _title = null; // 1..1
	private String _abstract = null;
//	private WMCurl _dataUrl = null;
//	private WMCurl _metadataUrl = null;
	private String _SRS = null;
//	private WMCDimensionList;
//	private WMCFormatList;
//	private WMCStyleList;
//	private SLDMinScaleDenominator;
//	private SLDMaxScaleDenominator;
	private WMCExtension _extension = null;


	private WMCLayerImpl()
	{}

	public static WMCLayerImpl newInstance()
	{
		return new WMCLayerImpl();
	}

	public static WMCLayer parse(Element el)
	{
		WMCLayerImpl layer = new WMCLayerImpl();

		layer.setQueryable(Utils.getBooleanAttrib(el.getAttributeValue("queryable"), false));
		layer.setHidden(Utils.getBooleanAttrib(el.getAttributeValue("hidden"), false));

		layer.setServer(WMCFactory.parseServer(el.getChild("Server")));
		layer.setName(el.getChildText("Name"));
		layer.setTitle(el.getChildText("Title"));
		layer.setAbstract(el.getChildText("Abstract"));
		layer.setSRS(el.getChildText("SRS"));

		Element eext = el.getChild("Extension");
		if(eext != null)
			layer.setExtension(WMCFactory.parseExtension(eext));

		return layer;
	}

	/***************************************************************************
	 * Server
	 */
	public void setServer(WMCServer server)
	{
		_server = server;
	}

	/**
	 * Returns Server
	 */
	public WMCServer getServer()
	{
		return _server;
	}

	/**
	 * Method addNewServer
	 */
	public WMCServer addNewServer()
	{
		if(_server != null)
			throw new IllegalStateException("A Server element already exists");

		_server = WMCServerImpl.newInstance();

		return _server;
	}

	/***************************************************************************
	 * Sets Name
	 */
	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Returns Name
	 */
	public String getName()
	{
		return _name;
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

	/***************************************************************************
	 * Sets SRS
	 */
	public void setSRS(String sRS)
	{
		_SRS = sRS;
	}

	/**
	 * Returns SRS
	 */
	public String getSRS()
	{
		return _SRS;
	}

	/***************************************************************************
	 * Sets Queryable
	 */
	public void setQueryable(boolean queryable)
	{
		_queryable = queryable;
	}

	/**
	 * Returns Queryable
	 */
	public boolean isQueryable()
	{
		return _queryable;
	}

	/***************************************************************************
	 * Sets Hidden
	 */
	public void setHidden(boolean hidden)
	{
		_hidden = hidden;
	}

	/**
	 * Returns Hidden
	 */
	public boolean isHidden()
	{
		return _hidden;
	}

	/***************************************************************************
	 * Method addNewExtension
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
	 */
	public Element toElement(String name)
	{
        if(_server == null)
            throw new IllegalStateException(name + "/Server is missing");

        if(_name == null)
            throw new IllegalStateException(name + "/Name is missing");

        if(_title == null)
            throw new IllegalStateException(name + "/Title is missing");

        Namespace NS_WMC = Namespace.getNamespace("http://www.opengis.net/context");

        Element ret = new Element(name, NS_WMC)
            .setAttribute("queryable", _queryable?"true":"false")
            .setAttribute("hidden",  	_hidden?"true":"false")
            .addContent(_server.toElement("Server"))
            .addContent(new Element("Name", NS_WMC).setText(_name))
            .addContent(new Element("Title", NS_WMC).setText(_title));

        if (_abstract != null)
            ret.addContent(new Element("Abstract").setText(_abstract));

//		if( _dataURL != null)		// TODO
//		if( _metadataURL != null)

        if (_SRS != null)
            ret.addContent(new Element("SRS").setText(_SRS));

//		if( _dimensionList != null) // TODO
//		if( _formatList != null)
//		if( _styleList != null)
//		if( _minscale != null)
//		if( _maxscale != null)

        if(_extension != null )
            ret.addContent(_extension.toElement("Extension"));

        return ret;

	}
}


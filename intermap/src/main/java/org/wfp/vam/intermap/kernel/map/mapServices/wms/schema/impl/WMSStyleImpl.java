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

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSLegendURL;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSStyle;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSStyleSheetURL;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type.WMSStyleURL;

/**
 * @author ETj
 */
public class WMSStyleImpl implements WMSStyle
{
	private String _name;  // 1..1
	private String _title; // 1..1
	private String _abstract; // 0..1
	private List<WMSLegendURL> _legendUrlList = new ArrayList<WMSLegendURL>(); // 0..n
	private WMSStyleSheetURL _styleSheetURL = null; // 0..1
	private WMSStyleURL _styleURL = null; // 0..1

	private WMSStyleImpl()
	{}

	public static WMSStyle newInstance()
	{
		return new WMSStyleImpl();
	}

	public static WMSStyle parse(Element eStyle)
	{
		WMSStyleImpl style = new WMSStyleImpl();

		style.setTitle(eStyle.getChildText("Title"));
		style.setName(eStyle.getChildText("Name"));
		style.setAbstract(eStyle.getChildText("Abstract"));
		for(Element eLegendUrl: (List<Element>)eStyle.getChildren("LegendURL"))
			style.addLegendURL(WMSFactory.parseLegendURL(eLegendUrl));

		Element essu = eStyle.getChild("StyleSheetURL");
		if(essu != null)
			style.setStyleSheetURL(WMSStyleSheetURLImpl.parse(essu)); // fixme use factory
		Element esu = eStyle.getChild("StyleURL");
		if(esu != null)
			style.setStyleURL(WMSStyleURLImpl.parse(esu)); // fixme use factory

		return style;
	}

	/**
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


	public void addLegendURL(WMSLegendURL legendUrl)
	{
		_legendUrlList.add(legendUrl);
	}

	public WMSLegendURL getLegendURL(int index)
	{
		if(index < 0 || _legendUrlList.size()-1 < index)
			return null;
		else
			return _legendUrlList.get(0);
	}

	public Iterable<WMSLegendURL> getLegendURLIterator()
	{
		return new Iterable<WMSLegendURL>()
		{
			public Iterator<WMSLegendURL> iterator()
			{
				return _legendUrlList.iterator();
			}
		};
	}

	/**
	 * Sets StyleSheetURL
	 */
	public void setStyleSheetURL(WMSStyleSheetURL styleSheetURL)
	{
		_styleSheetURL = styleSheetURL;
	}

	/**
	 * Returns StyleSheetURL
	 */
	public WMSStyleSheetURL getStyleSheetURL()
	{
		return _styleSheetURL;
	}

	/**
	 * Sets StyleURL
	 */
	public void setStyleURL(WMSStyleURL styleURL)
	{
		_styleURL = styleURL;
	}

	/**
	 * Returns StyleURL
	 */
	public WMSStyleURL getStyleURL()
	{
		return _styleURL;
	}

}


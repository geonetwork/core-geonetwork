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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCLayer;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCLayerList;

/**
 * @author ETj
 */
public class WMCLayerListImpl implements WMCLayerList
{
	private List<WMCLayer> _list = new ArrayList<WMCLayer>();

	private WMCLayerListImpl()
	{}

	/**
	 * Method newInstance
	 */
	public static WMCLayerList newInstance()
	{
		return new WMCLayerListImpl();
	}

	/**
	 * Method parse
	 */
	public static WMCLayerList parse(Element ell)
	{
		WMCLayerListImpl ll = new WMCLayerListImpl();

		for(Element el: (List<Element>)ell.getChildren("Layer"))
			ll.addLayer(WMCFactory.parseLayer(el));

		return ll;
	}


	public WMCLayer addNewLayer()
	{
		WMCLayer layer = WMCLayerImpl.newInstance();
		addLayer(layer);
		return layer;
	}

	public void addLayer(WMCLayer layer)
	{
		_list.add(layer);
	}

	/**
	 * @deprecated use iterator() directly
	 */
	public Iterable<WMCLayer> getLayerIterator()
	{
		return new Iterable<WMCLayer>()
		{
			public Iterator<WMCLayer> iterator()
			{
				return _list.iterator();
			}
		};
	}

	public Iterator<WMCLayer> iterator()
	{
		return _list.iterator();
	}

	public Element toElement(String name)
	{
		if( _list.isEmpty() )
			throw new IllegalStateException(name + " is empty");

		Element ret = new Element(name);

		for(WMCLayer layer: _list)
			ret.addContent(layer.toElement("Layer"));

		return ret;
	}

}


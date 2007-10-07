/**
 * LayerListType.java
 *
 */

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


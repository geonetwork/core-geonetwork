
package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl;

import java.util.List;
import org.jdom.Element;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.WMCExtension;


/**
 * @author ETj
 */
public class WMCExtensionImpl implements WMCExtension
{
	private Element _root = new Element("root");;

	private WMCExtensionImpl()
	{}

	public static WMCExtension newInstance()
	{
		return new WMCExtensionImpl();
	}

	public static WMCExtension parse(Element eext)
	{
		WMCExtension ext = new WMCExtensionImpl();

		for(Element e: (List<Element>)eext.getChildren())
			ext.add((Element)e.clone());

		return ext;
	}

	public void add(Element e)
	{
		_root.addContent(e);
	}

	public Element getChild(String name)
	{
		return _root.getChild(name);
	}

	public Element toElement(String name)
	{
		Element ret = (Element)_root.clone();
		ret.setName(name);
		return ret;
	}
}


/**
 * WMCFactory.java
 *
 */

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.impl;

import org.wfp.vam.intermap.kernel.map.mapServices.wmc.schema.type.*;

import org.jdom.Element;

/**
 * @author ETj
 */
public class WMCFactory
{
	public static WMCOnlineResource parseOnlineResource(Element eor)
	{
		return WMCOnlineResourceImpl.parse(eor);
	}

	public static WMCServer parseServer(Element eServ)
	{
		return WMCServerImpl.parse(eServ);
	}

	public static WMCLayer parseLayer(Element el)
	{
		return WMCLayerImpl.parse(el);
	}

	public static WMCExtension parseExtension(Element eext)
	{
		return WMCExtensionImpl.parse(eext);
	}

	public static WMCBoundingBox parseBoundingBox(Element ebb)
	{
		return WMCBoundingBoxImpl.parse(ebb);
	}

	public static WMCWindow parseWindow(Element ewin)
	{
		return WMCWindowImpl.parse(ewin);
	}

	public static WMCLayerList parseLayerList(Element ell)
	{
		return WMCLayerListImpl.parse(ell);
	}

	public static WMCGeneral parseGeneral(Element eg)
	{
		return WMCGeneralImpl.parse(eg);
	}

	static public WMCViewContext parseViewContext(Element vc)
	{
		return WMCViewContextImpl.parse(vc);
	}

	static public WMCViewContext createWMCViewContext()
	{
		return WMCViewContextImpl.newInstance();
	}

	static public WMCLayer createWMCLayer()
	{
		return WMCLayerImpl.newInstance();
	}
}


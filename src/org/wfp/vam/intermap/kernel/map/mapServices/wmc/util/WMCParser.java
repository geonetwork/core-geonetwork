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
//==============================================================================

package org.wfp.vam.intermap.kernel.map.mapServices.wmc.util;

import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.*;

import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.impl.Utils;

/**
 * Parses a WMC document expressed in jdom Elements. <P>
 * There are many issues about namespaces, so this class tries to overcome a bit of them.
 * OGC documentation refers to <I>"http://www.opengeospatial.net/context"</I>.
 *    This namespace is conveniently stored in <CODE>WMC.WMCNS</CODE>.<BR>
 * Some time also <I>"http://www.opengis.net/context"</I> is referred in some docs, so
 *    there's also the related <CODE>WMC.OGWMCNS</CODE> constant.<BR>
 * You can set your desired ns in the parser by using <CODE>setNamespace()</CODE>.
 * <P>
 * Also, if you need maximum flexibility, you may set lenientParsing to true.
 * In that case, the context tree parsing will be tried with the WMCNS, then
 * with the OGWMCNS, and at last with the no ns.
 *
 *
 * @author etj
 */
public class WMCParser {

	private Namespace namespace = WMC.WMCNS;
	private boolean lenientParsing = false;

	/**
	 * Sets the namespace for the context document.<P>
	 * You may want to use WMC constants WMCNS and OGWMCNS.
	 * If you set it to null, then no ns will be set.
	 */
	public void setNamespace(Namespace namespace) {
		if(namespace == null)
			this.namespace = Namespace.NO_NAMESPACE;
		else
			this.namespace = namespace;
	}

	/**
	 * When set to <B>false</B>, the parsing will only be tried on the set namespace.<BR>
	 * When set to <B>true</B>, the context tree parsing will be tried with
	 * the set ns, then WMCNS, then with the OGWMCNS, and at last with no ns.
	 */
	public void setLenientParsing(boolean lenientParsing) {
		this.lenientParsing = lenientParsing;
	}

	public WMCViewContext parseViewContext(Document wmcDoc)
	{
		Element root = wmcDoc.getRootElement();
		return parseViewContext(root);
	}

	public WMCViewContext parseViewContext(Element evc)
	{
		WMCViewContext vc = WMCViewContext.newInstance();

		vc.setVersion(evc.getAttributeValue("version"));
		vc.setId(evc.getAttributeValue("id"));

		Element general = getChild(evc, "General");
		if(general==null)
			throw new IllegalArgumentException("Missing mandatory <General> element.");
		vc.setGeneral(parseGeneral(general));

		Element layerList = getChild(evc, "LayerList");
		if(layerList==null)
			throw new IllegalArgumentException("Missing mandatory <LayerList> element.");
		vc.setLayerList(parseLayerList(layerList));

		return vc;
	}

	public WMCGeneral parseGeneral(Element eg)
	{
		WMCGeneral general = WMCGeneral.newInstance();

		Element window = getChild(eg, "Window");
		if(window!=null)
			general.setWindow(parseWindow(window));
		general.setBoundingBox(parseBoundingBox(getChild(eg, "BoundingBox")));
		general.setTitle(getChildText(eg, "Title"));
		general.setAbstract(getChildText(eg, "Abstract"));

		Element eext = getChild(eg, "Extension");
		if(eext != null)
			general.setExtension(parseExtension(eext));

		return general;
	}

	public WMCServer parseServer(Element eserv)
	{
		WMCServer serv = WMCServer.newInstance();

		serv.setService(eserv.getAttributeValue("service"));
		serv.setVersion(eserv.getAttributeValue("version"));
		serv.setTitle(eserv.getAttributeValue("title"));

		serv.setOnlineResource(parseOnlineResource(getChild(eserv, "OnlineResource")));

		return serv;
	}

	public WMCExtension parseExtension(Element eext)
	{
		WMCExtension ext = WMCExtension.newInstance();
		XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());

		for(Element eChild: (List<Element>)eext.getChildren())
		{
			ext.add(eChild.getName(), outputter.outputString(eChild));
		}

		return ext;
	}

	public WMCBoundingBox parseBoundingBox(Element ebb)
	{
		WMCBoundingBox bb = WMCBoundingBox.newInstance();

		bb.setSRS(ebb.getAttributeValue("SRS"));
		bb.setMinx(Double.parseDouble(ebb.getAttributeValue("minx")));
		bb.setMiny(Double.parseDouble(ebb.getAttributeValue("miny")));
		bb.setMaxx(Double.parseDouble(ebb.getAttributeValue("maxx")));
		bb.setMaxy(Double.parseDouble(ebb.getAttributeValue("maxy")));

		return bb;
	}

	public WMCWindow parseWindow(Element ewin)
	{
		WMCWindow win = WMCWindow.newInstance();

		win.setWidth(Integer.parseInt(ewin.getAttributeValue("width")));
		win.setHeight(Integer.parseInt(ewin.getAttributeValue("height")));

		return win;
	}

	public WMCLayerList parseLayerList(Element ell)
	{
		WMCLayerList ll = WMCLayerList.newInstance();

		for(Element el: getChildren(ell, "Layer"))
			ll.addLayer(parseLayer(el));

		return ll;
	}

	public WMCLayer parseLayer(Element el)
	{
		WMCLayer layer = WMCLayer.newInstance();

		layer.setQueryable(Utils.getBooleanAttrib(el.getAttributeValue("queryable"), false));
		layer.setHidden(Utils.getBooleanAttrib(el.getAttributeValue("hidden"), false));

		layer.setServer(parseServer(getChild(el, "Server")));
		layer.setName(getChildText(el, "Name"));
		layer.setTitle(getChildText(el, "Title"));
		layer.setAbstract(getChildText(el, "Abstract"));
		layer.setSRS(getChildText(el, "SRS"));
		layer.setFormatList(parseFormatList(getChild(el, "FormatList")));
		layer.setStyleList(parseStyleList(getChild(el, "StyleList")));

		Element eext = getChild(el, "Extension");
		if(eext != null)
			layer.setExtension(parseExtension(eext));

		return layer;
	}

	public WMCFormatList parseFormatList(Element ell)
	{
		WMCFormatList ll = WMCFormatList.newInstance();

		if(ell != null)
		{
			for(Element el: getChildren(ell, "Format"))
				ll.addFormat(parseFormat(el));
		}

		return ll;
	}

	public WMCFormat parseFormat(Element eformat)
	{
		WMCFormat format = WMCFormat.newInstance();

		format.setCurrent(Utils.getBooleanAttrib(eformat.getAttributeValue("current"), false));
		format.setFormat(eformat.getText());

		return format;
	}

	public WMCStyleList parseStyleList(Element ell)
	{
		WMCStyleList ll = WMCStyleList.newInstance();

		if(ell != null)
		{
			for(Element el: getChildren(ell, "Style"))
				ll.addStyle(parseStyle(el));
		}

		return ll;
	}

	public WMCStyle parseStyle(Element estyle)
	{
		WMCStyle style = WMCStyle.newInstance();
		style.setCurrent(Utils.getBooleanAttrib(estyle.getAttributeValue("current"), false));

		WMCStyle.Choice0 c0 = parseStyleChoice0(estyle);
		if(c0 != null)
		{
			style.setChoice(c0);
		}
		else
		{
			WMCStyle.Choice1 c1 = parseStyleChoice1(estyle);
			if(c1 != null)
				style.setChoice(c1);
			else
				throw new IllegalArgumentException("Can't parse Style");
		}

		return style;
	}

	private WMCStyle.Choice0 parseStyleChoice0(Element eStyleChoice0) {
		WMCStyle.Choice0 choice = new WMCStyle.Choice0();

		// try a mandatory element
		String name = getChildText(eStyleChoice0, "Name");
		if(name == null)
			return null;

		choice.setName(name);
		choice.setTitle(getChildText(eStyleChoice0, "Title"));
		choice.setAbstract(getChildText(eStyleChoice0, "Abstract"));

		Element legendURL = getChild(eStyleChoice0, "LegendURL");
		if(legendURL != null)
			choice.setLegendURL(parseURL(legendURL));

		return choice;
	}

	private WMCStyle.Choice1 parseStyleChoice1(Element eStyleChoice1) {
		WMCStyle.Choice1 choice = new WMCStyle.Choice1();

		// look for a mandatory element
		Element esld = getChild(eStyleChoice1, "SLD");
		if(esld == null)
			return null;

		choice.setSld(parseSLD(esld));

		return choice;
	}


	private WMCSLD parseSLD(Element element)
	{
		WMCSLD sld = WMCSLD.newInstance();

		sld.setName(getChildText(element, "Name"));
		sld.setTitle(getChildText(element, "Title"));

		WMCSLD.Choice0 c0 = parseSLDChoice0(element);
		if(c0 != null)
		{
			sld.setChoice0(c0);
			return sld;
		}
		else
		{
			// TODO
			Logger.getLogger(getClass()).warn("SLD parsing not implemented yet");
			return null;
		}
	}

	private WMCSLD.Choice0 parseSLDChoice0(Element eStyleChoice0) {
		WMCSLD.Choice0 choice = new WMCSLD.Choice0();

		// look for a mandatory element
		Element onlineResource = getChild(eStyleChoice0, "OnlineResource");
		if(onlineResource == null)
			return null;

		choice.setOnlineResource(parseOnlineResource(onlineResource));

		return choice;
	}


	public WMCURL parseURL(Element element)
	{
		WMCURL url = WMCURL.newInstance();

		String sw = element.getAttributeValue("width");
		if(sw != null)
			url.setWidth(Integer.parseInt(sw));

		String sh = element.getAttributeValue("height");
		if(sh != null)
			url.setHeight(Integer.parseInt(sh));

		url.setFormat(element.getAttributeValue("format"));
		url.setOnlineResource(parseOnlineResource(getChild(element, "OnlineResource")));
		return url;
	}

	public WMCOnlineResource parseOnlineResource(Element onlineResource)
	{
		WMCOnlineResource wor = WMCOnlineResource.newInstance();
		wor.setHref(onlineResource.getAttributeValue("href", WMC.XLINKNS));
		return wor;
	}

	//---

	private Element getChild(Element parent, String name)
	{
		if(lenientParsing) {
			Element child = parent.getChild(name, namespace);
			if(child == null)
				child = parent.getChild(name, WMC.WMCNS);
			if(child == null) {
				child = parent.getChild(name, WMC.OGWMCNS);
				if(child!=null)
					Logger.getLogger(getClass()).info("leniently accepting bad namespace");
			}
			if(child == null) {
				child = parent.getChild(name);
				if(child!=null)
					Logger.getLogger(getClass()).info("leniently accepting empty namespace");
			}
			return child;
		}
		else {
			return parent.getChild(name, namespace);
		}
	}

	private String getChildText(Element parent, String name) {
		Element child = getChild(parent, name);
		return child == null? null : child.getText();
	}

	private List<Element> getChildren(Element parent, String name) {
		if(lenientParsing) {
			List children = parent.getChildren(name, namespace);
			if(children.isEmpty())
				children = parent.getChildren(name, WMC.WMCNS);
			if(children.isEmpty()) {
				children = parent.getChildren(name, WMC.OGWMCNS);
				if( ! children.isEmpty() )
					Logger.getLogger(getClass()).info("leniently accepting bad namespace");
			}
			if(children.isEmpty()) {
				children = parent.getChildren(name);
				if( ! children.isEmpty() )
					Logger.getLogger(getClass()).info("leniently accepting empty namespace");
			}
			return children;
		}
		else {
			return parent.getChildren(name, namespace);
		}
	}


	private static void debugChildren(Element e)
	{
		String name = e.getName();
		String ns = e.getNamespace().toString();
		System.out.println("DUMPING element '"+name+"' NS:" + ns );
		XMLOutputter o = new XMLOutputter(Format.getPrettyFormat());
		for(Element child : (List<Element>)e.getChildren()) {
			try {
				System.out.println(" child " + child.getName());
				o.output(child, System.out);
			} catch(IOException ex) {
				Logger.getLogger(WMCParser.class).error("error dumping " + e.getName(), ex);
			}
		}
	}

}

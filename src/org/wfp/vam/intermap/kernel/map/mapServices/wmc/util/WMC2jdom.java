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

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMC;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCBoundingBox;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCExtension;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCFormat;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCFormatList;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCGeneral;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCLayer;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCLayerList;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCOnlineResource;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCServer;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCViewContext;
import org.wfp.vam.intermap.kernel.map.mapServices.wmc.om.WMCWindow;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author etj
 */
public class WMC2jdom {
	
	private Namespace namespace = WMC.WMCNS;

	public WMC2jdom() {
	}

	public WMC2jdom(Namespace namespace) {
		setNamespace(namespace);
	}

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
		
	class WMCElement extends Element
	{
		WMCElement(String name)
		{
			super(name, WMC2jdom.this.namespace);
		}
	}
	
	public Element toElement(WMCViewContext context)
	{
		if(context.getGeneral() == null)
			throw new IllegalStateException("ViewContext/General is missing");

		if(context.getLayerList() == null)
			throw new IllegalStateException("ViewContext/LayerList is missing");

		if(context.getId() == null)
			throw new IllegalStateException("ViewContext/@id is missing");

		Element vc = new WMCElement("ViewContext")
			.setAttribute("version", context.getVersion())
			.setAttribute("id", context.getId())
			.addContent(toElement(context.getGeneral(), "General"))
			.addContent(toElement(context.getLayerList(), "LayerList"));
		vc.addNamespaceDeclaration(WMC.XLINKNS);
		return vc;
	}	
	
	public Element toElement(WMCGeneral gen, String name)
	{
		if(gen.getTitle() == null)
			throw new IllegalStateException(name + "/Title is missing");

		if(gen.getBoundingBox() == null)
			throw new IllegalStateException(name + "/BoundingBox is missing");

		Element ret = new WMCElement(name)
			.addContent(new WMCElement("Title").setText(gen.getTitle()))
			.addContent(toElement(gen.getBoundingBox(), "BoundingBox"));

		if( gen.getWindow() != null)
			ret.addContent(toElement(gen.getWindow(), "Window"));

		// TODO add keywordlist

		if(gen.getAbstract() != null )
			ret.addContent(new WMCElement("Abstract").setText(gen.getAbstract()));

		// TODO add logourl
		// TODO add descriptionurl
		// TODO add contactinformation

		if(gen.getExtension() != null )
			ret.addContent(toElement(gen.getExtension(), "Extension"));

		return ret;
	}
		
	public Element toElement(WMCWindow win, String name)
	{
		if( win.getWidth() == -1 )
			throw new IllegalStateException(name + "/@Width is missing");

		if( win.getHeight() == -1 )
			throw new IllegalStateException(name + "/@Height is missing");

		return new WMCElement(name)
			.setAttribute("width", ""+ win.getWidth())
			.setAttribute("height", ""+ win.getHeight());
	}
	
	public Element toElement(WMCServer server, String name)
	{
		if(server.getOnlineResource() == null)
			throw new IllegalStateException(name + "/OnlineResource is missing");

		if(server.getService() == null)
			throw new IllegalStateException(name + "/service is missing");

		if(server.getVersion() == null)
			throw new IllegalStateException(name + "/version is missing");

		Element ret = new WMCElement(name)
			.setAttribute("service", server.getService())
			.setAttribute("version", server.getVersion())
			.addContent(toElement(server.getOnlineResource(), "OnlineResource"));

		if (server.getTitle() != null)
			ret.setAttribute("title", server.getTitle());

		return ret;
	}
	
	public Element toElement(WMCLayerList ll, String name)
	{
		if( ll.isEmpty() )
			throw new IllegalStateException(name + " is empty");

		Element ret = new WMCElement(name);

		for(WMCLayer layer: ll.getLayerIterator())
			ret.addContent(toElement(layer, "Layer"));

		return ret;
	}
	
	
	public Element toElement(WMCLayer layer, String name)
	{
		if(layer.getServer() == null)
			throw new IllegalStateException(name + "/Server is missing");

		if(layer.getName() == null)
			throw new IllegalStateException(name + "/Name is missing");

		if(layer.getTitle() == null)
			throw new IllegalStateException(name + "/Title is missing");

		Element ret = new WMCElement(name)
			.setAttribute("queryable", layer.isQueryable()?"true":"false")
			.setAttribute("hidden",	   layer.isHidden()?"true":"false")
			.addContent(new WMCElement("Name").setText(layer.getName()))
			.addContent(new WMCElement("Title").setText(layer.getTitle()))
			.addContent(toElement(layer.getServer(), "Server"));

		if (layer.getAbstract() != null)
			ret.addContent(new WMCElement("Abstract").setText(layer.getAbstract()));

//		if( _dataURL != null)		// TODO
//		if( _metadataURL != null)

		if (layer.getSRS() != null)
			ret.addContent(new WMCElement("SRS").setText(layer.getSRS()));

//		if( _dimensionList != null)
		
		if( layer.getFormatList() != null) 
			if( ! layer.getFormatList().isEmpty() )
				ret.addContent(toElement(layer.getFormatList(), "FormatList"));
//		if( _styleList != null)
//		if( _minscale != null)
//		if( _maxscale != null)

		if( layer.getExtension() != null )
			ret.addContent(toElement(layer.getExtension(), "Extension"));

		return ret;
	}
	
	public Element toElement(WMCOnlineResource or, String name)
	{
		Element ret = new WMCElement(name)
			.setAttribute("type", or.getType(), WMC.XLINKNS);

		if( or.getHref() != null)
			ret.setAttribute("href", or.getHref(), WMC.XLINKNS);

		return ret;
	}

	
	public Element toElement(WMCBoundingBox bb, String name)
	{
		if(bb.getSRS() == null)
			throw new IllegalStateException(name + "/SRS is missing");
		if(bb.getMinx() == Float.NaN)
			throw new IllegalStateException(name + "/minx is missing");
		if(bb.getMiny() == Float.NaN)
			throw new IllegalStateException(name + "/miny is missing");
		if(bb.getMaxx() == Float.NaN)
			throw new IllegalStateException(name + "/maxx is missing");
		if(bb.getMaxy() == Float.NaN)
			throw new IllegalStateException(name + "/maxy is missing");

		return new WMCElement(name)
			.setAttribute("SRS", bb.getSRS())
			.setAttribute("minx", ""+bb.getMinx())
			.setAttribute("miny", ""+bb.getMiny())
			.setAttribute("maxx", ""+bb.getMaxx())
			.setAttribute("maxy", ""+bb.getMaxy());
	}

	public Element toElement(WMCFormat format, String name)
	{
		Element ret = new WMCElement(name);
		
		if(format.isCurrent())
			ret.setAttribute("current", "true");

		ret.setText(format.getFormat());

		return ret;
	}
	
	public Element toElement(WMCFormatList fl, String name)
	{
		if( fl.isEmpty() )
			throw new IllegalStateException(name + " is empty");

		Element ret = new WMCElement(name);

		for(WMCFormat format: fl.getFormatIterator())
			ret.addContent(toElement(format, "Format"));

		return ret;
	}
	
	public Element toElement(WMCExtension ext, String name) 
	{
		Element ret = new WMCElement(name);
		
		SAXBuilder builder = new SAXBuilder();		
		
		for(String sxml: ext.getExtensionsIterator())
		{
			try {
				Document doc = builder.build(new StringReader(sxml));
				Element elem = doc.getRootElement();
				ret.addContent(elem.detach());
			} catch(JDOMException ex) {
				Logger.getLogger(WMC2jdom.class.getName()).log(Level.SEVERE, null, ex);
				throw new IllegalStateException("Bad Extension", ex);
			} catch(IOException ex) {
				Logger.getLogger(WMC2jdom.class.getName()).log(Level.SEVERE, null, ex);
				throw new IllegalStateException("Bad Extension", ex);
			}
		}
		
		return ret;
	}

}

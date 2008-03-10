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

package org.wfp.vam.intermap.kernel.map.mapServices.wms.schema.type;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;

/**
 * @author ETj
 */
public enum WMSFormat
{
	TEXT_PLAIN("text/plain")
	,TEXT_HTML("text/html")
	,TEXT_SWF("text/swf")
	,TEXT_XHTML("text/xhtml")
	,TEXT_XML("text/xml")

	,IMAGE_BMP("image/bmp")
	,IMAGE_GIF("image/gif")
	,IMAGE_JPG("image/jpg")
	,IMAGE_JPEG("image/jpeg")
	,IMAGE_PNG("image/png")
	,IMAGE_SWF("image/swf")
	,IMAGE_TIFF("image/tiff")
	,IMAGE_WBMP("image/wbmp")

	,APP_GML("application/vnd.ogc.gml")
	,APP_WMS("application/vnd.ogc.wms_xml")
	,APP_WMSERR("application/vnd.ogc.se_xml")


	,v100_GIF("GIF")
	,v100_JPEG("JPEG")
	,v100_PNG("PNG")
	,v100_PPM("PPM")
	,v100_TIFF("TIFF")
	,v100_GEOTIFF("GeoTIFF")
	,v100_WEBCGM("WebCGM")
	,v100_SVG("SVG")
	,v100_WMSXML("WMS_XML")
	,v100_GML1("GML.1")
	,v100_GML2("GML.2")
	,v100_GML3("GML.3")
	,v100_WBMP("WBMP")
	,v100_MIME("MIME")
	,v100_INIMAGE("INIMAGE")
	,v100_BLANK("BLANK")
	;

	private final String _type;

	private WMSFormat(String type)
	{
		_type = type;
	}

	public String toString()
	{
		return _type;
	}

	public static WMSFormat parse(String type)
	{
		for(WMSFormat format: WMSFormat.values())
			if(format.toString().equals(type))
				return format;
		System.out.println("WARNING: WMSFormat: type '"+type+"' is not defined internally. Please update the internal mimetype list.");
		return null;
	}

	public static List<WMSFormat> parse(Element eFormat)
	{
		List<WMSFormat> ret = new ArrayList<WMSFormat>();

		List<Element> formatList = (List<Element>)eFormat.getChildren();
		if(formatList.size() > 0) // then this has format WMS 1.0.0
		{
			for(Element e: formatList)
			{
				ret.add(parse(e.getName()));
			}
		}
		else // ver > 1.0.0
		{
			ret.add(parse(eFormat.getText()));
		}

		return ret;
	}

}


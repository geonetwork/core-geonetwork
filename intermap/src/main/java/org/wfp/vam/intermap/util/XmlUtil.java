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

package org.wfp.vam.intermap.util;

import org.apache.xpath.XPathAPI;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.jdom.output.DOMOutputter;

/**
 * @author ETj: Decompiled by Jad v1.5.8e.
 */
public class XmlUtil
{
	public static Element getElement(Element xml, String xpath)
        throws Exception
    {
        DOMOutputter outputter = new DOMOutputter();
        org.w3c.dom.Document w3cDoc = outputter.output(new Document((Element)xml.clone()));
        org.w3c.dom.Node result = XPathAPI.selectSingleNode(w3cDoc, xpath);
        if(result instanceof org.w3c.dom.Element)
        {
            DOMBuilder builder = new DOMBuilder();
            return builder.build((org.w3c.dom.Element)result);
        }
		else
        {
            return null;
        }
    }
}


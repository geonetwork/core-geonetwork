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

import java.io.File;
import java.io.OutputStream;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.transform.JDOMResult;
import org.jdom.transform.JDOMSource;

/**
 * @author ETj: Decompiled by Jad v1.5.8e.
 */
public class XmlTransformer
{
    public static Element transform(Element xml, String styleSheet)
        throws Exception
    {
        return transform(xml, ((Source) (new StreamSource(styleSheet))));
    }

    public static Element transform(Element xml, Element styleSheet)
        throws Exception
    {
        return transform(xml, ((Source) (new JDOMSource(new Document((Element)styleSheet.detach())))));
    }

    public static Element transform(Element xml, Source srcSheet)
        throws Exception
    {
        TransformerFactory transFact = TransformerFactory.newInstance();
        Source srcXml = new JDOMSource(new Document((Element)xml.detach()));
        JDOMResult resXml = new JDOMResult();
        Transformer t = transFact.newTransformer(srcSheet);
        t.transform(srcXml, resXml);
        return (Element)resXml.getDocument().getRootElement().detach();
    }

    public static void transform(Element xml, String styleSheet, OutputStream out)
        throws Exception
    {
        transform(xml, ((Source) (new StreamSource(new File(styleSheet)))), out);
    }

    public static void transform(Element xml, Element styleSheet, OutputStream out)
        throws Exception
    {
        transform(xml, ((Source) (new JDOMSource(new Document((Element)styleSheet.detach())))), out);
    }

    public static void transform(Element xml, Source srcSheet, OutputStream out)
        throws Exception
    {
        TransformerFactory transFact = TransformerFactory.newInstance();
        Source srcXml = new JDOMSource(new Document((Element)xml.detach()));
        javax.xml.transform.Result resStream = new StreamResult(out);
        Transformer t = transFact.newTransformer(srcSheet);
        t.transform(srcXml, resStream);
    }

}


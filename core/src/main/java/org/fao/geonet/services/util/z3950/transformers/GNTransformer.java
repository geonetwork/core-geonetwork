/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

//=============================================================================
//=== This program is free software; you can redistribute it and/or modify
//=== it under the terms of the GNU General Public License as published by
//=== the Free Software Foundation; either version 2 of the License, or (at
//=== your option) any later version.
//===
//=== This program is distributed in the hope that it will be useful, but
//=== WITHOUT ANY WARRANTY; without even the implied warranty of
//=== MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//=== General Public License for more details.
//===
//=== You should have received a copy of the GNU General Public License
//=== along with this program; if not, write to the Free Software
//=== Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//=== Contact: Jeroen Ticheler email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.services.util.z3950.transformers;

import jeeves.constants.Jeeves;

import org.fao.geonet.ContextContainer;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.guiservices.schemas.GetSchemaInfo;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.input.DOMBuilder;
import org.jzkit.search.util.RecordConversion.FragmentTransformationException;
import org.jzkit.search.util.RecordConversion.FragmentTransformer;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class GNTransformer extends FragmentTransformer {

    private Path stylesheetPath;
    private DocumentBuilder htmldb = null;

    @SuppressWarnings("rawtypes")
    public GNTransformer(String from, String to, Map properties, Map context, ApplicationContext ctx) {
        super(from, to, properties, context, ctx);
        this.ctx = ctx;

        String stylesheet = (String) properties.get("Sheet");
        if (stylesheet == null) {
            Log.error(Geonet.SEARCH_ENGINE, "Failed to get name of stylesheet from properties - looking for property with name 'Sheet' - found instead: " + properties);
            return;
        }

        try {
            stylesheetPath = IO.toPath(ctx.getResource(stylesheet).getURI());
            if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                Log.debug(Geonet.SEARCH_ENGINE, "Stylesheet for " + from + " to " + to + " is " + stylesheet);
        } catch (Exception e) {
            Log.error(Geonet.SEARCH_ENGINE, "Problem with stylesheet: " + stylesheet, e);
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
            dbf.setIgnoringComments(false);
            dbf.setIgnoringElementContentWhitespace(false);
            dbf.setExpandEntityReferences(false);
            htmldb = dbf.newDocumentBuilder();
        } catch (Exception e) {
            Log.error(Geonet.SEARCH_ENGINE, e.getMessage(), e);
        }
    }


    public Document transform(Document input, @SuppressWarnings("rawtypes") Map additional_properties) throws FragmentTransformationException {

        DOMBuilder builder = new DOMBuilder();
        org.jdom.Document jdomDoc = builder.build(input);

        ContextContainer cnt = (ContextContainer) ctx.getBean("ContextGateway");

        // now transform using stylesheet passed in

        org.jdom.Element elem = null;
        try {
            // Call GetSchemaInfo to place schema titles and codelists into
            // xpath /root/gui/schemas/{} for xsl transformation to use
            org.jdom.Element root = new org.jdom.Element(Jeeves.Elem.ROOT);
            org.jdom.Element gui = new org.jdom.Element(Jeeves.Elem.GUI);
            GetSchemaInfo gsi = new GetSchemaInfo();
            gui.addContent(gsi.exec(new org.jdom.Element(Jeeves.Elem.REQUEST), cnt.getSrvctx()));
            root.addContent(gui);

            org.jdom.Element metadata = new org.jdom.Element(Geonet.Elem.METADATA);
            metadata.addContent(jdomDoc.detachRootElement());
            root.addContent(metadata);
            elem = Xml.transform(root, stylesheetPath);

        } catch (Exception e) {
            throw new FragmentTransformationException(e.getMessage());
        }

        // give back a DOM document with html as text (suits jzkit which expects
        // html output to be text because parsing may not work)
        Document output = null;
        try {
            output = htmldb.newDocument();
            Element root = output.createElement("HTML");
            root.appendChild(output.createTextNode(Xml.getString(elem)));
            output.appendChild(root);
        } catch (Exception e) {
            throw new FragmentTransformationException(e.getMessage());
        }

        return output;
    }
}

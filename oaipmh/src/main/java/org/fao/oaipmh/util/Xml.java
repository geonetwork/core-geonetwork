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

package org.fao.oaipmh.util;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.SAXOutputter;
import org.xml.sax.SAXException;

//=============================================================================

/**
 * This is a portion of the jeeves.utils.Xml class and is replicated here just to avoid the jeeves
 * jar
 */
public class Xml {
    // ---------------------------------------------------------------------------
    // ---
    // --- API methods
    // ---
    // ---------------------------------------------------------------------------

    private static SchemaFactory factory = SchemaFactory
        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    // ---------------------------------------------------------------------------
    private static SAXOutputter so;

    // ---------------------------------------------------------------------------
    // ---
    // --- Variables
    // ---
    // ---------------------------------------------------------------------------

    /**
     * Loads an xml stream and returns its root node (validates the xml with a dtd)
     */

    public static Element loadStream(InputStream input) throws IOException,
        JDOMException {
        SAXBuilder builder = new SAXBuilder();
        Document jdoc = builder.build(input);

        return (Element) jdoc.getRootElement().detach();
    }

    /**
     * Converts an xml element to a string
     */

    // public static String getString(Element data)
    // {
    // XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    //
    // return outputter.outputString(data);
    // }

    // ---------------------------------------------------------------------------

    // public static String getString(Document data)
    // {
    // XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
    //
    // return outputter.outputString(data);
    // }

    // ---------------------------------------------------------------------------
    public static void validate(Element xml) throws IOException, SAXException,
        JDOMException {
        // NOTE: Create a schema object without schema file name so that schemas
        // will be obtained from whatever locations are provided in the document

        Schema schema = factory.newSchema();
        ValidatorHandler vh = schema.newValidatorHandler();
        so = new SAXOutputter(vh);
        so.output(xml);
    }

}

// =============================================================================


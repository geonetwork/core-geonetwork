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

package org.fao.geonet.csw.common.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.kernel.SchemaManager;

import static org.fao.geonet.utils.Xml.getSAXBuilder;

//=============================================================================

/**
 * This is a portion of the jeeves.utils.Xml class and is replicated here just to avoid the jeeves
 * jar
 */

public class Xml {
    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------

    /**
     * Loads an xml file and returns its root node (validates the xml with a dtd)
     */

    public static Element loadString(String data, boolean validate)
        throws IOException, JDOMException {
        SAXBuilder builder = getSAXBuilder(validate);
        Document jdoc = builder.build(new StringReader(data));

        return (Element) jdoc.getRootElement().detach();
    }

    //--------------------------------------------------------------------------

    /**
     * Loads an xml stream and returns its root node (validates the xml with a dtd)
     */

    public static Element loadStream(InputStream input) throws IOException, JDOMException {
        SAXBuilder builder = getSAXBuilder(false);
        Document jdoc = builder.build(input);
        return (Element) jdoc.getRootElement().detach();
    }

    //---------------------------------------------------------------------------

    /**
     * Converts an xml element to a string
     */

    public static String getString(Element data) {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

        return outputter.outputString(data);
    }

    //---------------------------------------------------------------------------

    public static String getString(Document data) {
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

        return outputter.outputString(data);
    }

    /**
     * Applies stylesheet according to ElementSetName and schema.
     *
     * @param context        Service context
     * @param schemaManager  schemamanager
     * @param schema         schema
     * @param result         result
     * @param outputSchema   requested OutputSchema
     * @param elementSetName requested ElementSetName
     * @param resultType     requested ResultTYpe
     * @param id             metadata id
     * @return metadata
     * @throws InvalidParameterValueEx hmm
     */
    public static Element applyElementSetName(ServiceContext context, SchemaManager schemaManager, String schema,
                                               Element result, String outputSchema, ElementSetName elementSetName,
                                               ResultType resultType, String id, String displayLanguage) throws InvalidParameterValueEx {
        Path schemaDir = schemaManager.getSchemaCSWPresentDir(schema);
        Path styleSheet = schemaDir.resolve(outputSchema + "-" + elementSetName + ".xsl");
        Path styleSheetWithoutElementSet = schemaDir.resolve(outputSchema + ".xsl");

        if (!Files.exists(styleSheet) && !Files.exists(styleSheetWithoutElementSet)) {
            throw new InvalidParameterValueEx("OutputSchema",
                String.format(
                    "OutputSchema '%s' not supported for metadata with '%s' (%s).\nCorresponding XSL transformation '%s' (or '%s') does not exist for this schema.\nThe record will not be returned in response.",
                    outputSchema, id, schema, styleSheet.getFileName(), styleSheetWithoutElementSet.getFileName()));
        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("lang", displayLanguage);

            Path xslFile = Files.exists(styleSheet) ? styleSheet : styleSheetWithoutElementSet;
            try {
                result = org.fao.geonet.utils.Xml.transform(result, xslFile, params);
            } catch (Exception e) {
                String msg = String.format(
                    "Error occurred while transforming metadata with id '%s' using '%s'.",
                    id, xslFile.getFileName());
                context.error(msg);
                context.error("  (C) StackTrace:\n" + Util.getStackTrace(e));
                throw new InvalidParameterValueEx("OutputSchema", msg);
            }
            return result;
        }
    }

}

//=============================================================================


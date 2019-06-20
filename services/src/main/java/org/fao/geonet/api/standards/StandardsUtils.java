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

package org.fao.geonet.api.standards;

import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlFile;
import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.util.Set;

/**
 * Created by francois on 23/06/16.
 */
public class StandardsUtils {
    public static Element getCodelist(String codelist, SchemaManager schemaManager,
                                      String schema,
                                      String parent, String xpath,
                                      String isoType, ServiceContext context, String displayIf)
        throws Exception {
        return getCodelistOrLabel(codelist, schemaManager, schema,
                                    parent, xpath, isoType, context, displayIf, "codelists.xml");
    }

    public static Element getLabel(String element, SchemaManager schemaManager,
                                   String schema,
                                   String parent, String xpath,
                                   String isoType, String displayIf, ServiceContext context)
        throws Exception {
        return getCodelistOrLabel(element, schemaManager, schema,
                                    parent, xpath, isoType, context, displayIf, "labels.xml");
    }

    private static Element getCodelistOrLabel(String element, SchemaManager schemaManager,
                                              String schema, String parent, String xpath,
                                              String isoType, ServiceContext context,
                                              String displayIf, String fileName) throws Exception {
        String elementName = StandardsUtils.findNamespace(element, schemaManager, schema);
        Element e = StandardsUtils.getHelp(schemaManager, fileName,
            schema, elementName, parent, xpath, isoType, displayIf, context);
        if (e == null) {
            if (schema.startsWith("iso19139.")) {
                e = StandardsUtils.getHelp(schemaManager, fileName,
                    "iso19139", elementName, parent, xpath, isoType, displayIf, context);
            }
            if (e == null) {
                throw new ResourceNotFoundException(String.format(
                    "Element '%s' from schema '%s' not found in '%s'.",
                    elementName, schema, fileName));
            }
        }
        return e;
    }


    public static Element getHelp(SchemaManager scm, String fileName, String schema,
                                  String name, String parent, String xpath, String isoType, String displayIf, ServiceContext context)
        throws Exception {

        XmlFile xf = scm.getSchemaInfo(schema).get(fileName);

        if (xf == null) {
            throw new OperationAbortedEx("File not found for : " + schema + "/" + fileName);
        }

        Element entries = xf.exec(new Element("junk"), context);

        Element result = checkEntries(scm, schema, entries, xpath, name, isoType, displayIf, true);
        if (result == null) {
            result = checkEntries(scm, schema, entries, parent, name, isoType, displayIf, true);
        }
        if (result == null) {
            result = checkEntries(scm, schema, entries, xpath, name, isoType, displayIf, false);
        }
        if (result == null) {
            result = checkEntries(scm, schema, entries, parent, name, isoType, displayIf, false);
        }

        if (result == null) {
            // get schemas that this schema depends on and check whether the
            // help/label exists in those - stop at the first one found
            Set<String> dependentSchemas = scm.getDependencies(schema);
            for (String baseSchema : dependentSchemas) {
                result = getHelp(scm, fileName, baseSchema, name, parent, xpath, isoType, displayIf, context);
                if (result != null) break;
            }
        }

        return result;
    }

    private static Element checkEntries(SchemaManager scm, String schema, Element entries, String context,
                                        String name, String isoType, String displayIf, boolean requireContextMatch) throws OperationAbortedEx {

        for (Object o : entries.getChildren()) {
            Element currElem = (Element) o;
            String currName = currElem.getAttributeValue("name");
            String aliasName = currElem.getAttributeValue("alias");
            String currContext = currElem.getAttributeValue("context");
            String displayIfAttribute = currElem.getAttributeValue("displayIf");

            currName = findNamespace(currName, scm, schema);

            if (currName == null) {
                Log.warning(Geonet.SCHEMA_MANAGER, "Namespace prefix for element " +
                    currElem.getAttributeValue("name") +
                    " not found in " + schema + " schema namespaces." +
                    "Check the element namespace or remove it " +
                    "from the labels.xml file.");
                continue;
            }

            if (!currName.equals(name)) {
                if (aliasName == null || !aliasName.equals(name)) {
                    continue;
                }
            }

            // If a context attribute is provided check if there is a match
            // Context can be parent element name or an xpath
            if (currContext != null && (context != null || isoType != null)) {
                // XPath context are supposed to use same namespace prefix
                if (!currContext.contains("/")) {
                    currContext = findNamespace(currContext, scm, schema);

                    // Use namespace prefix also for context
                    if ((context != null) && (!context.contains("/"))) {
                        context = findNamespace(context, scm, schema);
                    }
                }

                if (
                    (context != null && context.equals(currContext)) ||
                    (isoType != null && isoType.equals(currContext))) {
                    return (Element) currElem.clone();
                }
            } else if (displayIf != null && displayIf.equals(displayIfAttribute)) {
                // A codelist may be uniquely defined with a displayIf attribute
                // eg.
                // <codelist name="gmd:CI_RoleCode" alias="roleCode"
                //           displayIf="ancestor::node()[name()='gmd:MD_Metadata' and contains(gmd:metadataStandardName/gco:CharacterString, 'ISO 19139, MyOcean profile')]">

                return (Element) currElem.clone();
            } if (!requireContextMatch && displayIfAttribute == null) {
                // Return an element not matching any context attribute
                // or displayIf condition. Usually the default value of the standard.
                return (Element) currElem.clone();
            }
        }

        return null; // no match found

    }

    public static String findNamespace(String name, SchemaManager scm, String schema) {
        int pos = name.indexOf(':');

        if (pos == -1) {
            return name;
        }
        String prefix = name.substring(0, pos);

        String nsURI = scm.getNamespaceURI(schema, prefix);

        if (nsURI == null) {
            return null;
        }

        return nsURI + name.substring(pos);
    }

    public static Element buildError(Element elem, String error) {
        elem = (Element) elem.clone();
        elem.setAttribute("error", error);

        return elem;
    }
}

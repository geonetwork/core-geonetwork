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

package org.fao.geonet.services.schema;

import jeeves.exceptions.BadParameterEx;
import jeeves.exceptions.OperationAbortedEx;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlFile;
import jeeves.utils.Util;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.jdom.Element;

//=============================================================================

/**
 * Service retrieving information for an element or a codelist from schema localized files (ie.
 * labels.xml and codelist.xml).
 * 
 * 
 * <p>
 * Example to retrieve codelist information:
 * 
 * <pre>
 * {@code
 * <request>
 *   <codelist schema="iso19139" name="gmd:CI_RoleCode"/>
 * </request>
 * }
 * </pre>
 * 
 * </p>
 * 
 * 
 * <p>
 * Example to retrieve element information:
 * 
 * <pre>
 * {@code
 * <request>
 *   <element schema="iso19139" name="gmd:identificationInfo" 
 *     context="gmd:MD_Metadata" 
 *     fullContext="gmd:MD_Metadata/gmd:identificationInfo" 
 *     isoType="" />
 * </request>
 * }
 * </pre>
 * 
 * </p>
 * 
 */
public class Info implements Service {
    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    public void init(String appPath, ServiceConfig params) throws Exception {
        ;
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SchemaManager scm = gc.getSchemamanager();

        Element response = new Element("response");

        for (Object o : params.getChildren()) {
            Element elem = (Element) o;
            String name = elem.getName();

            if (name.equals("element")) {
                response.addContent(handleElement(scm, elem, context));
            } else if (name.equals("codelist")) {
                response.addContent(handleCodelist(scm, elem, context));
            } else {
                throw new BadParameterEx("element", name);
            }
        }

        return response;
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Private methods
    // ---
    // --------------------------------------------------------------------------

    private Element handleElement(SchemaManager scm, Element elem, ServiceContext context)
            throws Exception {
        return handleObject(scm, elem, "labels.xml", context);
    }

    // --------------------------------------------------------------------------

    private Element handleCodelist(SchemaManager scm, Element elem, ServiceContext context)
            throws Exception {
        return handleObject(scm, elem, "codelists.xml", context);
    }

    // --------------------------------------------------------------------------

    private Element handleObject(SchemaManager scm, Element elem, String fileName,
            ServiceContext servContext) throws Exception {
        String schema = Util.getAttrib(elem, "schema");
        String name = Util.getAttrib(elem, "name");
        String parent = Util.getAttrib(elem, "context", "");
        String isoType = Util.getAttrib(elem, "isoType", "");
        String xpath = Util.getAttrib(elem, "fullContext", "");

        name = findNamespace(name, scm, schema);
        parent = findNamespace(parent, scm, schema);
        isoType = findNamespace(isoType, scm, schema);

        if (name == null) {
            return buildError(elem, UNKNOWN_NAMESPACE);
        }

        if (!scm.existsSchema(schema)) {
            return buildError(elem, UNKNOWN_SCHEMA);
        }

        return getHelp(scm, elem, fileName, schema, name, parent, xpath, isoType, servContext);
    }

    // --------------------------------------------------------------------------

    public static Element getHelp(SchemaManager scm, Element elem, String fileName, String schema,
            String name, String parent, String xpath, String isoType, ServiceContext context)
            throws Exception {

        XmlFile xf = scm.getSchemaInfo(schema).get(fileName);

        if (xf == null) {
            throw new OperationAbortedEx("File not found for : " + schema + "/" + fileName);
        }

        Element entries = xf.exec(new Element("junk"), context);

        Element result = checkEntries(scm, schema, entries, xpath, name, isoType, true);
        if (result == null) {
            result = checkEntries(scm, schema, entries, parent, name, isoType, true);
        }
        if (result == null) {
        	result = checkEntries(scm, schema, entries, xpath, name, isoType, false);
        }
        if (result == null) {
        	result = checkEntries(scm, schema, entries, parent, name, isoType, false);
        }
        if (result == null) {
            if (schema.contains("iso19139") && !(schema.equals("iso19139"))) {
                result = getHelp(scm, elem, fileName, "iso19139", name, parent, xpath, isoType,
                        context);
            } else {
                return buildError(elem, NOT_FOUND);
            }
        }

        
        return result;
    }

    // --------------------------------------------------------------------------

    private static Element checkEntries(SchemaManager scm, String schema, Element entries, String context,
            String name, String isoType, boolean requireContextMatch) throws OperationAbortedEx {

        for (Object o : entries.getChildren()) {
            Element currElem = (Element) o;
            String currName = currElem.getAttributeValue("name");
            String currContext = currElem.getAttributeValue("context");

            currName = findNamespace(currName, scm, schema);
            
            if (currName == null) {
                throw new OperationAbortedEx("No namespace found for : " + currName);
            }

            if(!currName.equals(name)) {
            	continue;
            }
            
        	if (currContext != null && (context != null || isoType != null)) {
        		// XPath context are supposed to use same namespace prefix
        		if (!currContext.contains("/")) {
        			currContext = findNamespace(currContext, scm, schema);
        		}
        		
        		if (context.equals(currContext) || isoType.equals(currContext)) {
        			return (Element) currElem.clone();
        		}
        	} else if (!requireContextMatch){
        		return (Element) currElem.clone();
        	}
        }

        return null; // no match found

    }

    // --------------------------------------------------------------------------

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

    // --------------------------------------------------------------------------

    private static Element buildError(Element elem, String error) {
        elem = (Element) elem.clone();
        elem.setAttribute("error", error);

        return elem;
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Variables
    // ---
    // --------------------------------------------------------------------------

    private static final String UNKNOWN_SCHEMA = "unknown-schema";

    private static final String UNKNOWN_NAMESPACE = "unknown-namespace";

    private static final String NOT_FOUND = "not-found";
}

// =============================================================================


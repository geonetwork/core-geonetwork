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

import org.fao.geonet.api.standards.StandardsUtils;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.jdom.Element;

import java.nio.file.Path;

//=============================================================================

/**
 * Service retrieving information for an element or a codelist from schema localized files (ie.
 * labels.xml and codelist.xml).
 *
 *
 * <p> Example to retrieve codelist information:
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
 * <p> Example to retrieve element information:
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
 */
@Deprecated
public class Info implements Service {
    // --------------------------------------------------------------------------
    // ---
    // --- Init
    // ---
    // --------------------------------------------------------------------------

    private static final String UNKNOWN_SCHEMA = "unknown-schema";

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------
    private static final String UNKNOWN_NAMESPACE = "unknown-namespace";

    // --------------------------------------------------------------------------
    // ---
    // --- Private methods
    // ---
    // --------------------------------------------------------------------------
    private static final String NOT_FOUND = "not-found";

    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
        ;
    }

    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SchemaManager scm = gc.getBean(SchemaManager.class);

        Element response = new Element("response");

        for (Object o : params.getChildren()) {
            Element elem = (Element) o;
            String name = elem.getName();

            if (name.equals("element")) {
                response.addContent(handleElement(scm, elem, context));
            } else if (name.equals("codelist")) {
                response.addContent(handleCodelist(scm, elem, context));
//            } else {
//                throw new BadParameterEx("element", name);
            }
        }

        return response;
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Variables
    // ---
    // --------------------------------------------------------------------------

    private Element handleElement(SchemaManager scm, Element elem, ServiceContext context)
        throws Exception {
        return handleObject(scm, elem, "labels.xml", context);
    }

    private Element handleCodelist(SchemaManager scm, Element elem, ServiceContext context)
        throws Exception {
        return handleObject(scm, elem, "codelists.xml", context);
    }

    private Element handleObject(SchemaManager scm, Element elem, String fileName,
                                 ServiceContext servContext) throws Exception {
        String schema = Util.getAttrib(elem, "schema");
        String name = Util.getAttrib(elem, "name");
        String parent = Util.getAttrib(elem, "context", "");
        String isoType = Util.getAttrib(elem, "isoType", "");
        String xpath = Util.getAttrib(elem, "fullContext", "");

        name = StandardsUtils.findNamespace(name, scm, schema);
        parent = StandardsUtils.findNamespace(parent, scm, schema);
        isoType = StandardsUtils.findNamespace(isoType, scm, schema);

        if (name == null) {
            return StandardsUtils.buildError(elem, UNKNOWN_NAMESPACE);
        }

        if (!scm.existsSchema(schema)) {
            return StandardsUtils.buildError(elem, UNKNOWN_SCHEMA);
        }

        Element result = StandardsUtils.getHelp(scm, fileName, schema, name, parent, xpath, isoType, null, servContext);
        // if not found then return an error
        if (result == null) {
            return StandardsUtils.buildError(elem, NOT_FOUND);
        } else {
            return result;
        }
    }
}

// =============================================================================


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

package org.fao.geonet.csw.common;

import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.jdom.Namespace;

import java.util.Iterator;
import java.util.Map;

public enum OutputSchema {
    DEFAULT("csw");

    private String schema;

    private OutputSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Check that outputSchema is known by local catalogue instance based on schema plugin declared
     * output schema.
     * <p/>
     * <p/>
     * ===================== OGC 07-006 10.8.4.5: The outputSchema parameter is used to indicate the
     * schema of the output that is generated in response to a GetRecords request. The default value
     * for this parameter shall be: http://www.opengis.net/cat/csw/2.0.2 indicating that the schema
     * for the core returnable properties (as defined in Subclause 10.2.5) shall be used.
     * Application profiles may define additional values for outputSchema, but all profiles shall
     * support the value http://www.opengis.net/cat/csw/2.0.2. Although the value of this parameter
     * can be any URI, any additional values defined for the outputSchema parameter should be the
     * target namespace of the additionally supported output schemas and should include a version
     * number. For example, the value urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5 might be used to
     * indicate an ebRIM v2.5 output schema, while the value urn:oasis:names:tc:ebxml-regrep:rim:xsd:3.0
     * might be used to indicate an ebRIM v3.0 output schema.
     * <p/>
     * The list of supported output schemas shall be advertised in the capabilities document of the
     * service using the Parameter element as outlined in OGC 05-008. =========== OGC 07-045: If
     * available, it must support http://www.opengis.net/cat/csw/2.0.2 and
     * http://www.isotc211.org/2005/gmd. Default value is http://www.opengis.net/cat/csw/2.0.2.
     * <p/>
     * Check {@link SchemaPlugin#getCswTypeNames()} to load dynamically output schema.
     *
     * @param schema requested outputSchema
     * @return return the schema prefix.
     * @throws InvalidParameterValueEx hmm
     */
    public static String parse(String schema, SchemaManager schemaManager) throws InvalidParameterValueEx {
        if (schema == null) return "csw";

        // For backward compatibility
        if (schema.equals("Record")) return "csw";
        if (schema.equals("IsoRecord")) return "gmd";
        if (schema.equals("csw:Record")) return "csw";
        if (schema.equals("csw:IsoRecord")) return "gmd";
        if (schema.equals("own")) return "own";

        Map<String, String> typenames = schemaManager.getOutputSchemas();
        for (Map.Entry<String, String> entry : typenames.entrySet()) {
            String ns = entry.getValue();
            if (schema.equals(ns)) {
                return entry.getKey();
            }
        }

        throw new InvalidParameterValueEx("outputSchema",
            String.format("'%s' output schema is not valid. Supported values are %s",
                schema,
                schemaManager.getListOfOutputSchemaURI().toString()));
    }

    public String toString() {
        return schema;
    }
}

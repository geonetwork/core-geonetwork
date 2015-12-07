package org.fao.geonet.csw.common;

import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.jdom.Namespace;

import java.util.Iterator;
import java.util.Map;

public enum OutputSchema {
    DEFAULT("csw");

    private OutputSchema(String schema) {
        this.schema = schema;
    }

    public String toString() {
        return schema;
    }

    /**
     * Check that outputSchema is known by local catalogue instance
     * based on schema plugin declared output schema.
     * <p/>
     * <p/>
     * =====================
     * OGC 07-006 10.8.4.5:
     * The outputSchema parameter is used to indicate the schema of the output that is generated in response to a
     * GetRecords request. The default value for this parameter shall be:
     * http://www.opengis.net/cat/csw/2.0.2
     * indicating that the schema for the core returnable properties (as defined in Subclause 10.2.5) shall be used.
     * Application profiles may define additional values for outputSchema, but all profiles shall support the value
     * http://www.opengis.net/cat/csw/2.0.2.
     * Although the value of this parameter can be any URI, any additional values defined for the outputSchema parameter
     * should be the target namespace of the additionally supported output schemas and should include a version number.
     * For example, the value
     * urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5
     * might be used to indicate an ebRIM v2.5 output schema, while the value
     * urn:oasis:names:tc:ebxml-regrep:rim:xsd:3.0
     * might be used to indicate an ebRIM v3.0 output schema.
     * <p/>
     * The list of supported output schemas shall be advertised in the capabilities document of the service using the
     * Parameter element as outlined in OGC 05-008.
     * ===========
     * OGC 07-045:
     * If available, it must support
     * http://www.opengis.net/cat/csw/2.0.2
     * and
     * http://www.isotc211.org/2005/gmd.
     * Default value is
     * http://www.opengis.net/cat/csw/2.0.2.
     * <p/>
     * Check {@link SchemaPlugin#getCswTypeNames()} to load dynamically
     * output schema.
     *
     * @param schema        requested outputSchema
     * @param schemaManager
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

        Map<String, Namespace> typenames = schemaManager.getHmSchemasTypenames();
        Iterator<String> iterator = typenames.keySet().iterator();
        while (iterator.hasNext()) {
            String typeName = iterator.next();
            Namespace ns = typenames.get(typeName);
            if (schema.equals(ns.getURI())) {
                return ns.getPrefix();
            }
        }

        throw new InvalidParameterValueEx("outputSchema",
                String.format("'%s' schema is not valid. Supported values are %s",
                        schema,
                        schemaManager.getListOfOutputSchemaURI().toString()));
    }

    private String schema;
}
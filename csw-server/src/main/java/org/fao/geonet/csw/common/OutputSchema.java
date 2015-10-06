package org.fao.geonet.csw.common;

import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;

public enum OutputSchema
{
	OGC_CORE("Record"), ISO_PROFILE("IsoRecord"), OWN("own"), DCAT("dcat");

	//------------------------------------------------------------------------

	private OutputSchema(String schema) { this.schema = schema;}

	//------------------------------------------------------------------------

	public String toString() { return schema; }

	//------------------------------------------------------------------------
	/**
	 * Check that outputSchema is known by local catalogue instance.
	 * 
	 * TODO : register new outputSchema when profile are loaded.
     *
     * =====================
     * OGC 07-006 10.8.4.5:
     * The outputSchema parameter is used to indicate the schema of the output that is generated in response to a
     * GetRecords request. The default value for this parameter shall be:
     *    http://www.opengis.net/cat/csw/2.0.2
     * indicating that the schema for the core returnable properties (as defined in Subclause 10.2.5) shall be used.
     * Application profiles may define additional values for outputSchema, but all profiles shall support the value
     *    http://www.opengis.net/cat/csw/2.0.2.
     * Although the value of this parameter can be any URI, any additional values defined for the outputSchema parameter
     * should be the target namespace of the additionally supported output schemas and should include a version number.
     * For example, the value
     *    urn:oasis:names:tc:ebxml-regrep:rim:xsd:2.5
     * might be used to indicate an ebRIM v2.5 output schema, while the value
     *    urn:oasis:names:tc:ebxml-regrep:rim:xsd:3.0
     * might be used to indicate an ebRIM v3.0 output schema.
     *
     * The list of supported output schemas shall be advertised in the capabilities document of the service using the
     * Parameter element as outlined in OGC 05-008.
     * ===========
     * OGC 07-045:
     * If available, it must support
     *    http://www.opengis.net/cat/csw/2.0.2
     * and
     *    http://www.isotc211.org/2005/gmd.
     * Default value is
     *    http://www.opengis.net/cat/csw/2.0.2.
     *
     * @param schema requested outputSchema
     * @return either Record or IsoRecord (GN internal representation)
     * @throws InvalidParameterValueEx hmm
     */
	public static OutputSchema parse(String schema) throws InvalidParameterValueEx
	{
		if (schema == null)						return OGC_CORE;
        // TODO heikki: seems to me the following two values are invalid and should be rejected
		if (schema.equals("csw:Record"))		return OGC_CORE;
		if (schema.equals("csw:IsoRecord")) return ISO_PROFILE;
		
		if (schema.equals(Csw.NAMESPACE_CSW.getURI())) return OGC_CORE;
		if (schema.equals(Csw.NAMESPACE_GMD.getURI())) return ISO_PROFILE;
		
		if (schema.equals(OWN.toString())) return OWN;
		if (schema.equals(DCAT.toString())) return DCAT;

		throw new InvalidParameterValueEx("outputSchema", schema);
	}

	//------------------------------------------------------------------------

	private String schema;
}
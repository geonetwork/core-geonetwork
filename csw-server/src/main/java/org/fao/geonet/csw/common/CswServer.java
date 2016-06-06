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

package org.fao.geonet.csw.common;

import org.fao.geonet.utils.Log;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.jdom.Namespace;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//=============================================================================

/**
 * Class to parse GetCapabilities document.
 */
public class CswServer {
    public static final String GET_RECORDS = "GetRecords";
    public static final String GET_RECORD_BY_ID = "GetRecordById";
    private Map<String, CswOperation> operations = new HashMap<String, CswOperation>();

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    private List<String> logs = new ArrayList<String>();

    //---------------------------------------------------------------------------
    private String preferredServerVersion = Csw.CSW_VERSION;

    /**
     * Constructor.
     */
    public CswServer(Element capab) {
        parse(capab);
    }

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    public void parse(Element capab) {
        logs.clear();
        operations.clear();

        parseVersions(capab);
        parseOperations(capab);
    }

    public CswOperation getOperation(String name) {
        return operations.get(name);
    }

    public String getPreferredServerVersion() {
        return preferredServerVersion;
    }

    /**
     * Get available operations in the GetCapabilities document.
     */
    private void parseOperations(Element capabil) {
        Element operMd = capabil.getChild("OperationsMetadata", Csw.NAMESPACE_OWS);

        if (operMd == null)
            log("Missing 'ows:OperationsMetadata' element");

        else
            for (Object e : operMd.getChildren()) {
                Element elem = (Element) e;

                if ("Operation".equals(elem.getName())) {
                    CswOperation oper = extractOperation(elem);

                    if (oper != null)
                        operations.put(oper.getName(), oper);
                }
            }
    }

    //---------------------------------------------------------------------------

    /**
     * Get operations name and properties needed for futur operation calls.
     */
    private CswOperation extractOperation(Element oper) {
        String name = oper.getAttributeValue("name");

        if (name == null) {
            log("Operation has no 'name' attribute");
            return null;
        }

        CswOperation op = new CswOperation();
        op.setName(name);

        @SuppressWarnings("unchecked")
        List<Element> dcp = oper.getChildren("DCP", Csw.NAMESPACE_OWS);
        evaluateUrl(dcp, op);

        @SuppressWarnings("unchecked")
        List<Element> parameters = oper.getChildren("Parameter", Csw.NAMESPACE_OWS);
        log("Found " + parameters.size() + " parameters for operation: " + name);
        List<Element> outputSchemas = null;
        List<Element> typeNames = null;
        List<Element> outputFormats = null;
        List<Element> constraintLanguages = null;

        for (Element parameter : parameters) {
            String parameterName = parameter.getAttributeValue("name");
            log("Processing parameter: " + parameterName);
            if (parameterName != null && parameterName.equalsIgnoreCase("outputSchema")) {
                @SuppressWarnings("unchecked")
                List<Element> tmp = parameter.getChildren("Value", Csw.NAMESPACE_OWS);
                outputSchemas = tmp;
                log("Found " + outputSchemas.size() + " outputSchemas for operation: " + name);
            }

            // CSW 07-045 spec sometime use typenames or typename for the GetRecord type name parameter:
            // * With 's' in Table 29
            // * Without 's' p114 in GetCapabiltiies examples and in figure 10 'getRecords service="CSW", typeName="gmd:MD_Metadata"'
            // Type name is used in both GetRecords (probably with typenames) and DescribeRecord (problaby with typename) operation
            // so check for both parameters
            if (parameterName != null &&
                (parameterName.equalsIgnoreCase("typeNames") || parameterName.equalsIgnoreCase("typeName"))
                ) {
                @SuppressWarnings("unchecked")
                List<Element> tmp = parameter.getChildren("Value", Csw.NAMESPACE_OWS);
                typeNames = tmp;
                log("Found " + typeNames.size() + " typeNames for operation: " + name);
            }

            if (parameterName != null && parameterName.equalsIgnoreCase("outputFormat")) {
                @SuppressWarnings("unchecked")
                List<Element> tmp = parameter.getChildren("Value", Csw.NAMESPACE_OWS);
                outputFormats = tmp;
                log("Found " + outputFormats.size() + " outputFormats for operation: " + name);
            }

            if (parameterName != null &&
                parameterName.equalsIgnoreCase("CONSTRAINTLANGUAGE")) {
                @SuppressWarnings("unchecked")
                List<Element> tmp = parameter.getChildren("Value", Csw.NAMESPACE_OWS);
                constraintLanguages = tmp;
                log("Found " + constraintLanguages.size() + " constraintLanguage for operation: " + name);
            }
        }

        if (outputSchemas != null) {
            for (Element outputSchema : outputSchemas) {
                String outputSchemaValue = outputSchema.getValue();
                log("Adding outputSchema: " + outputSchemaValue + " to operation: " + name);
                op.getOutputSchemaList().add(outputSchemaValue);
            }
            op.choosePreferredOutputSchema();
        } else {
            log("No outputSchema for operation: " + name);
        }

        if (constraintLanguages != null) {
            for (Element constraintLanguage : constraintLanguages) {
                String constraintLanguageValue = constraintLanguage.getValue().toLowerCase();
                log("Adding constraintLanguage : " + constraintLanguageValue + " to operation: " + name);
                if ("cql".equals(constraintLanguageValue)) {
                    log(" Some implementation use CQL instead of CQL_TEXT for the CQL constraint language value.");
                    constraintLanguageValue = "cql_text";
                }
                op.getConstraintLanguage().add(constraintLanguageValue);
            }
        } else {
            log("No constraintLanguage for operation: " + name);
        }

        if (typeNames != null) {
            for (Element typeName : typeNames) {
                String typeNameValue = typeName.getValue();
                log("Adding typeName: " + typeNameValue + " to operation: " + name);
                TypeName tn = TypeName.getTypeName(typeNameValue);
                if (tn != null) {
                    op.getTypeNamesList().add(typeNameValue);
                } else {
                    log("  Unsupported typeName found: " + typeNameValue + ".");
                }
            }
        } else {
            log("No typeNames for operation: " + name);
        }

        if (outputFormats != null) {
            for (Element outputFormat : outputFormats) {
                String outputFormatValue = outputFormat.getValue();
                log("Adding outputFormat: " + outputFormatValue + " to operation: " + name);
                op.getOutputFormatList().add(outputFormatValue);
            }
            op.choosePreferredOutputFormat();
        } else {
            op.setPreferredOutputFormat(Csw.OUTPUT_FORMAT_APPLICATION_XML);
            log("No outputFormat for operation: " + name);
        }

        op.setPreferredServerVersion(preferredServerVersion);

        return op;
    }

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    /**
     * Gets server supported versions.
     */
    private void parseVersions(Element capabil) {
        List<String> serverVersions = new ArrayList<String>();
        Element serviceIdentificationMd = capabil.getChild("ServiceIdentification", Csw.NAMESPACE_OWS);

        if (serviceIdentificationMd == null) {
            log("Missing 'ows:ServiceTypeVersion' element");
        } else {
            @SuppressWarnings(value = "unchecked")
            List<Element> serviceIdentificationMdElems = serviceIdentificationMd.getChildren();
            for (Element value : serviceIdentificationMdElems) {
                String valueName = value.getName();
                log("Processing value: " + valueName);
                if (valueName != null && valueName.equalsIgnoreCase("ServiceTypeVersion")) {
                    serverVersions.add(value.getValue());
                }
            }
        }

        // Select default CSW supported version
        if (serverVersions.isEmpty()) serverVersions.add(Csw.CSW_VERSION);

        List<String> preferenceVersions = new ArrayList<String>();
        preferenceVersions.add(Csw.CSW_VERSION);
        preferenceVersions.add("2.0.1");
        preferenceVersions.add("2.0.0");

        for (String nextBest : preferenceVersions) {
            if (serverVersions.contains(nextBest)) {
                preferredServerVersion = nextBest;
                break;
            }
        }

    }

    /**
     * Search for valid POST or GET URL and check that service is available using GET method or
     * POST/XML.
     *
     * SOAP services are not supported (TODO ?).
     */
    private void evaluateUrl(List<Element> dcps, CswOperation op) {
        if (dcps == null) {
            log("Missing 'ows:DCP' element in operation");
            return;
        }

        Namespace ns = Namespace.getNamespace("http://www.w3.org/1999/xlink");

        for (Element dcp : dcps) {
            Element http = dcp.getChild("HTTP", Csw.NAMESPACE_OWS);

            if (http == null) {
                log("Missing 'ows:HTTP' element in operation/DCP");
                continue;
            }

            // GET method
            Element getUrl = http.getChild("Get", Csw.NAMESPACE_OWS);

            if (getUrl == null) {
                log("No GET url found in current DCP. Checking POST ...");
            } else {
                String tmpGetUrl = getUrl.getAttributeValue("href", ns);

                if (tmpGetUrl != null && op.getGetUrl() == null) {
                    try {
                        op.setGetUrl(new URL(tmpGetUrl));
                        log("Found URL (GET method): " + tmpGetUrl);
                    } catch (MalformedURLException e) {
                        log("Malformed 'xlink:href' attribute in operation's http method");
                    }
                }
            }

            // POST method
            @SuppressWarnings(value = "unchecked")
            List<Element> postUrlList = http.getChildren("Post", Csw.NAMESPACE_OWS);

            for (Element postUrl : postUrlList) {
                if (postUrl == null) {
                    log("No POST url found in current DCP.");
                } else {
                    String tmpPostUrl = postUrl.getAttributeValue("href", ns);

                    if (tmpPostUrl == null) {
                        log("Missing 'xlink:href' attribute in operation's http method");
                    } else {
                        if (op.getPostUrl() == null) {
                            // PostEncoding could return a SOAP service address. Not supported
                            Element methodConstraint = postUrl.getChild("Constraint", Csw.NAMESPACE_OWS);

                            if (methodConstraint != null) {
                                Element value = methodConstraint.getChild("Value", Csw.NAMESPACE_OWS);
                                if (value != null && value.getText().equals("SOAP")) {
                                    log("The URL " + tmpPostUrl + " using POST/SOAP method is not supported for harvesting.");
                                    continue;
                                }
                            }

                            try {
                                op.setPostUrl(new URL(tmpPostUrl));
                                log("Found URL (POST method):" + tmpPostUrl);
                                break;
                            } catch (MalformedURLException e) {
                                log("Malformed 'xlink:href' attribute in operation's http method");
                            }
                        }
                    }
                }
            }
        }
    }

    private void log(String message) {
        logs.add(message);
        if (Log.isDebugEnabled(Geonet.HARVEST_MAN))
            Log.debug(Geonet.HARVEST_MAN, message);
    }
    //---------------------------------------------------------------------------
}

//=============================================================================



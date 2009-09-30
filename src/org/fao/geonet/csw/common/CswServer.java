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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jeeves.utils.Log;


import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.jdom.Namespace;

//=============================================================================

/**
 * Class to parse GetCapabilities document.
 */
public class CswServer
{
	public static final String GET_RECORDS      = "GetRecords";
	public static final String GET_RECORD_BY_ID = "GetRecordById";

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public CswServer(Element capab)
	{
		parse(capab);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void parse(Element capab)
	{
		logs      .clear();
		operations.clear();

        parseVersions(capab);
		parseOperations(capab);
	}

	//---------------------------------------------------------------------------

	public CswOperation getOperation(String name) { return operations.get(name); }

    public String getPreferredServerVersion() { return preferredServerVersion; }
    
	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------
	/**
	 * Get available operations in the GetCapabilities document 
	 */
	private void parseOperations(Element capabil)
	{
		Element operMd = capabil.getChild("OperationsMetadata", Csw.NAMESPACE_OWS);

		if (operMd == null)
			log("Missing 'ows:OperationsMetadata' element");

		else
			for(Object e : operMd.getChildren())
			{
				Element elem = (Element) e;

				if ("Operation".equals(elem.getName()))
				{
					CswOperation oper = extractOperation(elem);

					if (oper != null)
						operations.put(oper.name, oper);
				}
			}
	}

	//---------------------------------------------------------------------------

	/**
	 * Get operations name and properties needed for futur operation calls. 
	 */
	private CswOperation extractOperation(Element oper)
	{
		String name = oper.getAttributeValue("name");

		if (name == null)
		{
			log("Operation has no 'name' attribute");
			return null;
		}


		CswOperation op = new CswOperation();
		op.name   = name;
		
		List<Element> dcp = oper.getChildren("DCP", Csw.NAMESPACE_OWS);
		evaluateUrl(dcp, op);

		
		List<Element> parameters = oper.getChildren("Parameter", Csw.NAMESPACE_OWS);
		log("Found " + parameters.size() + " parameters for operation: " + name);
		List<Element> outputSchemas = null ;
		for(Iterator<Element> i = parameters.iterator();i.hasNext();) {
			Element parameter = i.next();
			String parameterName = parameter.getAttributeValue("name"); 
			log("Processing parameter: " + parameterName);
			if(parameterName != null && parameterName.equals("outputSchema")) {	// CHECKME : case sensitive ?
				Element outputSchemaListing = parameter;
				outputSchemas = outputSchemaListing.getChildren("Value", Csw.NAMESPACE_OWS);
				log("Found " + outputSchemas.size() + " outputSchemas for operation: " + name);
			}
		}
		
		if(outputSchemas != null) {
			for(Iterator<Element> i = outputSchemas.iterator(); i.hasNext();) {
				Element outputSchema = i.next();
				String outputSchemaValue = outputSchema.getValue(); 
				log("Adding outputSchema: " + outputSchemaValue + " to operation: "+ name);
				op.outputSchemaList.add(outputSchemaValue);				
			}
			op.choosePreferredOutputSchema();
		}
		else {
			log("No outputSchema for operation: " + name);
		}

        op.preferredServerVersion = preferredServerVersion;

		return op;
	}

     /**
     * Get server supported versions
     */
    private void parseVersions(Element capabil)
    {
        List<String> serverVersions = new ArrayList<String>();
        Element serviceIdentificationMd = capabil.getChild("ServiceIdentification", Csw.NAMESPACE_OWS);

        if (serviceIdentificationMd == null) {
            log("Missing 'ows:ServiceTypeVersion' element");
        } else {

            List<Element> serviceIdentificationMdElems = serviceIdentificationMd.getChildren();
            for(Iterator<Element> i = serviceIdentificationMdElems.iterator();i.hasNext();) {
                Element value = i.next();
                String valueName = value.getName();
                log("Processing value: " + valueName);
                if(valueName != null && valueName.equalsIgnoreCase("ServiceTypeVersion")) {
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

		for(Iterator<String> i = preferenceVersions.iterator(); i.hasNext();){
			String nextBest = i.next();
			if(serverVersions.contains(nextBest)) {
				preferredServerVersion = nextBest;
				break;
			}
		}

    }

    
	//---------------------------------------------------------------------------
	/**
	 * Search for valid POST or GET
	 * URL and check that service is available
	 * using GET method or POST/XML.
	 * 
	 * SOAP services are not supported (TODO ?).
	 */
	private void evaluateUrl(List<Element> dcps, CswOperation op)
	{
		if (dcps == null)
		{
			log("Missing 'ows:DCP' element in operation");
			return;
		}
		
		Namespace ns = Namespace.getNamespace("http://www.w3.org/1999/xlink");
		
		for (Element dcp : dcps) {
			Element http = dcp.getChild("HTTP", Csw.NAMESPACE_OWS);
			
			if (http == null) {
				log ("Missing 'ows:HTTP' element in operation/DCP");
				continue;
			}
		
			// GET method
			Element getUrl = http.getChild("Get",  Csw.NAMESPACE_OWS);
			
			if (getUrl == null) {
				log ("No GET url found in current DCP. Checking POST ...");
			} else {
				String tmpGetUrl = getUrl.getAttributeValue("href", ns);

				if (tmpGetUrl != null && op.getUrl == null) {
					try	{
						op.getUrl = new URL(tmpGetUrl);
						log ("Found URL (GET method): " + tmpGetUrl);
					} catch (MalformedURLException e) {
						log ("Malformed 'xlink:href' attribute in operation's http method");
					}
				}
			}
			
			
			
			// POST method
			Element postUrl = http.getChild("Post", Csw.NAMESPACE_OWS);

			if (postUrl == null) {
				log ("No POST url found in current DCP.");
			} else {
				String tmpPostUrl = postUrl.getAttributeValue("href", ns);
			
				if (tmpPostUrl == null) {
					log("Missing 'xlink:href' attribute in operation's http method");
				} else {
					if (op.postUrl == null) {
						// PostEncoding could return a SOAP service address. Not supported
						Element methodConstraint = postUrl.getChild("Constraint", Csw.NAMESPACE_OWS);
						
						if (methodConstraint != null) {
							Element value = methodConstraint.getChild("Value", Csw.NAMESPACE_OWS);
							if (value != null && value.getText().equals("SOAP")) {
								log ("The URL " + tmpPostUrl + " using POST/SOAP method is not supported for harvesting.");
								continue;
							}
						}
	
						try	{
							op.postUrl = new URL(tmpPostUrl);
							log ("Found URL (POST method):" + tmpPostUrl);
						} catch (MalformedURLException e) {
							log ("Malformed 'xlink:href' attribute in operation's http method");
						}
					}
				}
			}
		}
	}

	//---------------------------------------------------------------------------

	private void log(String message)
	{
		logs.add(message);
		Log.debug(Geonet.HARVEST_MAN, message);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	private Map<String, CswOperation> operations = new HashMap<String, CswOperation>();

	private List<String> logs = new ArrayList<String>();

    private String preferredServerVersion = Csw.CSW_VERSION;

	//---------------------------------------------------------------------------
}

//=============================================================================



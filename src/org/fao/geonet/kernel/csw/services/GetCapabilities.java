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

package org.fao.geonet.kernel.csw.services;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.MissingParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.exceptions.VersionNegotiationFailedEx;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.getrecords.FieldMapper;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

//=============================================================================

public class GetCapabilities extends AbstractOperation implements CatalogService
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public GetCapabilities() {}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getName() { return "GetCapabilities"; }

	//---------------------------------------------------------------------------

	public Element execute(Element request, ServiceContext context) throws CatalogException
	{
		if (!checkService(request))
			throw new MissingParameterValueEx("service");

		checkService(request);
		checkAcceptVersions(request);

		//--- return capabilities

		String FS   = File.separator;
		String file = context.getAppPath() +"xml"+ FS +"csw"+ FS +"capabilities.xml";

		try
		{
			Element capabilities = Xml.loadFile(file);
			substitute(context, capabilities);
			setKeywords(capabilities, context);
			setOperationsParameters(capabilities);
			handleSections(request, capabilities);

			return capabilities;
		}
		catch (Exception e)
		{
			Log.error(Geonet.CSW, "Cannot load/process capabilities");
			Log.error(Geonet.CSW, " (C) StackTrace\n"+ Util.getStackTrace(e));

			throw new NoApplicableCodeEx("Cannot load/process capabilities");
		}
	}

	//---------------------------------------------------------------------------

	public Element adaptGetRequest(Map<String, String> params)
	{
		String service    = params.get("service");
		String sections   = params.get("sections");
		String sequence   = params.get("updatesequence");
		String acceptVers = params.get("acceptversions");
		String acceptForm = params.get("acceptformats");

		Element request = new Element(getName(), Csw.NAMESPACE_CSW);

		setAttrib(request, "service",        service);
		setAttrib(request, "updateSequence", sequence);

		fill(request, "AcceptVersions", "Version",      acceptVers, Csw.NAMESPACE_OWS);
		fill(request, "Sections",       "Section",      sections,   Csw.NAMESPACE_OWS);
		fill(request, "AcceptFormats",  "OutputFormat", acceptForm, Csw.NAMESPACE_OWS);

		return request;
	}
	
	//---------------------------------------------------------------------------
	
	public Element retrieveValues(String parameterName) throws CatalogException {
		// TODO 
		return null;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private void checkAcceptVersions(Element request) throws CatalogException
	{
		Element versions = request.getChild("AcceptVersions", Csw.NAMESPACE_OWS);

		if (versions == null)
			return;

		Iterator<Element> i = versions.getChildren().iterator();

		StringBuffer sb = new StringBuffer();

		while (i.hasNext())
		{
			Element version = (Element) i.next();

			if (version.getText().equals(Csw.CSW_VERSION))
				return;

			sb.append(version.getText());

			if (i.hasNext())
				sb.append(",");
		}

		throw new VersionNegotiationFailedEx(sb.toString());
	}

	//---------------------------------------------------------------------------

	private void handleSections(Element request, Element capabilities)
	{
		Element sections = request.getChild("Sections", Csw.NAMESPACE_OWS);

		if (sections == null)
			return;

		//--- handle 'section' parameters

		HashSet<String> hsSections = new HashSet<String>();

		Iterator<Element> i = sections.getChildren().iterator();

		while(i.hasNext())
		{
			Element section = (Element) i.next();
			String sectionName = section.getText();
			// Handle recognized section names only, others are ignored. Case Sensitive.
			if (sectionName.equals(Csw.SECTION_SI) || sectionName.equals(Csw.SECTION_SP)
					|| sectionName.equals(Csw.SECTION_OM) || sectionName.equals(Csw.SECTION_FC))
				hsSections.add(sectionName);
		}

		// Unrecognized section names are ignored
		if (hsSections.size() == 0)
			return;

		//--- remove not requested sections

		if (!hsSections.contains("ServiceIdentification"))
			capabilities.getChild("ServiceIdentification", Csw.NAMESPACE_OWS).detach();

		if (!hsSections.contains("ServiceProvider"))
			capabilities.getChild("ServiceProvider", Csw.NAMESPACE_OWS).detach();

		if (!hsSections.contains("OperationsMetadata"))
			capabilities.getChild("OperationsMetadata", Csw.NAMESPACE_OWS).detach();

		if (!hsSections.contains("Filter_Capabilities"))
			capabilities.getChild("Filter_Capabilities", Csw.NAMESPACE_OGC).detach();
	}

	//---------------------------------------------------------------------------

	private void substitute(ServiceContext context, Element capab) throws Exception
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		HashMap<String, String> vars = new HashMap<String, String>();

		vars.put("$HOST",    sm.getValue("system/server/host"));
		vars.put("$PORT",    sm.getValue("system/server/port"));
		vars.put("$SERVLET", context.getBaseUrl());
		
		// Set CSW contact information
		vars.put("$IND_NAME", sm.getValue("system/csw/individualName"));
		vars.put("$POS_NAME", sm.getValue("system/csw/positionName"));
		vars.put("$VOICE", sm.getValue("system/csw/contactInfo/phone/voice"));
		vars.put("$FACSCIMILE", sm.getValue("system/csw/contactInfo/phone/facsimile"));
		vars.put("$DEL_POINT", sm.getValue("system/csw/contactInfo/address/deliveryPoint"));
		vars.put("$CITY", sm.getValue("system/csw/contactInfo/address/city"));
		vars.put("$ADMIN_AREA", sm.getValue("system/csw/contactInfo/address/administrativeArea"));
		vars.put("$POSTAL_CODE", sm.getValue("system/csw/contactInfo/address/postalCode"));
		vars.put("$COUNTRY", sm.getValue("system/csw/contactInfo/address/country"));
		vars.put("$EMAIL", sm.getValue("system/csw/contactInfo/address/email"));
		vars.put("$HOUROFSERVICE", sm.getValue("system/csw/contactInfo/hoursOfService"));
		vars.put("$CONTACT_INSTRUCTION", sm.getValue("system/csw/contactInfo/contactInstructions"));
		vars.put("$ROLE", sm.getValue("system/csw/role"));
		vars.put("$TITLE", sm.getValue("system/csw/title"));
		vars.put("$ABSTRACT", sm.getValue("system/csw/abstract"));
		vars.put("$FEES", sm.getValue("system/csw/fees"));
		vars.put("$ACCESS_CONSTRAINTS", sm.getValue("system/csw/accessConstraints"));

		Lib.element.substitute(capab, vars);
	}
	
	//---------------------------------------------------------------------------
	
	/**
	 * Define keyword section of the GetCapabilities
	 * document according to catalogue content. Reading 
	 * Lucene index, most popular keywords are added 
	 * to the document.
	 */
	private void setKeywords (Element capabilities, ServiceContext context) {
		List<Element> keywords = capabilities.getChild("ServiceIdentification",
				Csw.NAMESPACE_OWS).getChildren("Keywords", Csw.NAMESPACE_OWS);

		List<Element> values = null;
		String[] properties = {"keyword"};
		try {
			values = GetDomain.handlePropertyName(properties, context, true);
		} catch (Exception e) {
            Log.error(Geonet.CSW, "Error getting domain value for specified PropertyName : " + e);
			// If GetDomain operation failed, just add nothing to the capabilities document template.            
            return;
        }
		
		for (Element k : keywords) {
			Element keyword = null;
			int cpt = 0;
			for (Element v : values) {
				keyword = new Element("Keyword", Csw.NAMESPACE_OWS);
				keyword.setText(v.getText());
				k.addContent(keyword);
				cpt++;
				if (cpt == CatalogConfiguration.getNumberOfKeywords())
					break;
			}
			// Add <ows:Type>theme</ows:Type>
			k.addContent(new Element("Type", Csw.NAMESPACE_OWS).setText("theme"));
			break; // only for first Keywords element in case of several.
		}
	}
	
	//---------------------------------------------------------------------------
	
	private void setOperationsParameters(Element capabilities) {

		List<Element> operations = capabilities.getChild("OperationsMetadata",
				Csw.NAMESPACE_OWS).getChildren("Operation", Csw.NAMESPACE_OWS);

		for (Element op : operations) {
			if (op.getAttributeValue(Csw.ConfigFile.Operation.Attr.NAME)
					.equals(Csw.ConfigFile.Operation.Attr.Value.GET_RECORDS)) {
				fillGetRecordsParams(op);
				continue;
			}
			if (op.getAttributeValue(Csw.ConfigFile.Operation.Attr.NAME)
					.equals(Csw.ConfigFile.Operation.Attr.Value.DESCRIBE_RECORD)) {
				fillDescribeRecordTypenames(op);
				continue;
			}
		}
	}
	
	//---------------------------------------------------------------------------
	
	private void fillDescribeRecordTypenames(Element op) {
		Element parameter = new Element("Parameter", Csw.NAMESPACE_OWS)
			.setAttribute("name", "typeName");
		
		Set<String> typenames = CatalogConfiguration.getDescribeRecordTypename().keySet();
		for (String typename : typenames) {
			parameter.addContent(new Element("Value", Csw.NAMESPACE_OWS)
				.setText(typename));
		}

		// Add Parameter node before constraint node if exist
		Element constraintNode = op.getChild("Constraint", Csw.NAMESPACE_OWS);
		if (constraintNode != null)
			op.addContent(op.indexOf(constraintNode) - 1, parameter);
		else
			op.addContent(parameter);
	}
	
	//---------------------------------------------------------------------------

	private void fillGetRecordsParams(Element op) {
		Set<String> isoQueryableMap = FieldMapper
				.getPropertiesByType(Csw.ISO_QUERYABLES);
		Element isoConstraint = new Element("Constraint", Csw.NAMESPACE_OWS)
				.setAttribute("name", Csw.ISO_QUERYABLES);
		
		for (String params : isoQueryableMap) {
			isoConstraint.addContent(new Element("Value", Csw.NAMESPACE_OWS)
					.setText(params));
		}

		Set<String> additionalQueryableMap = FieldMapper
				.getPropertiesByType(Csw.ADDITIONAL_QUERYABLES);
		Element additionalConstraint = new Element("Constraint",
				Csw.NAMESPACE_OWS).setAttribute("name",
				Csw.ADDITIONAL_QUERYABLES);
		
		for (String params : additionalQueryableMap) {
			additionalConstraint.addContent(new Element("Value",
					Csw.NAMESPACE_OWS).setText(params));
		}
		op.addContent(isoConstraint);
		op.addContent(additionalConstraint);
	}

}

//=============================================================================


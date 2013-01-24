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

import jeeves.resources.dbms.Dbms;
import jeeves.server.ConfigurationOverrides;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.exceptions.VersionNegotiationFailedEx;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.domain.CswCapabilitiesInfo;
import org.fao.geonet.kernel.csw.services.getrecords.FieldMapper;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//=============================================================================

public class GetCapabilities extends AbstractOperation implements CatalogService
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	private LuceneConfig _luceneConfig;

    public GetCapabilities(LuceneConfig luceneConfig) {
            this._luceneConfig = luceneConfig;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getName() { return "GetCapabilities"; }

	//---------------------------------------------------------------------------

	public Element execute(Element request, ServiceContext context) throws CatalogException {

		checkService(request);
		checkAcceptVersions(request);

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        boolean inspireEnabled = gc.getSettingManager().getValueAsBool("system/inspire/enable", false);


		//--- return capabilities

		String FS   = File.separator;
		String file;

        if (inspireEnabled){
            file = context.getAppPath() +"xml"+ FS +"csw"+ FS +"capabilities_inspire.xml";
        } else {
            file = context.getAppPath() +"xml"+ FS +"csw"+ FS +"capabilities.xml";
        }

		try
		{
			Element capabilities = Xml.loadFile(file);
            ServletContext servletContext = null;
            if(context.getServlet() != null) {
                servletContext = context.getServlet().getServletContext();
            }
			ConfigurationOverrides.updateWithOverrides(file, servletContext, context.getAppPath(), capabilities);

            String cswServiceSpecificContraint = request.getChildText(Geonet.Elem.FILTER);
			setKeywords(capabilities, context, cswServiceSpecificContraint);
			setOperationsParameters(capabilities);

            Dbms dbms = (Dbms) context.getResourceManager().open (Geonet.Res.MAIN_DB);

            String currentLanguage = "";

            // INSPIRE: Use language parameter if available, otherwise use default (using context.getLanguage())            
            if (inspireEnabled){
                String isoLangParamValue = request.getAttributeValue("language");

                List<String> langs = Lib.local.getLanguagesInspire(dbms);

                if (isoLangParamValue != null) {
                    // Retrieve GN language id from Iso language id
                    if (langs.contains(isoLangParamValue)) {
                        currentLanguage = isoLangParamValue;

                    }
                }


                String defaultLanguage = Lib.local.getDefaultLanguage(dbms);

                if (StringUtils.isEmpty(currentLanguage)) {
                    currentLanguage = defaultLanguage;
                }

                setInspireLanguages(capabilities, langs, currentLanguage, defaultLanguage);
            } else {
                currentLanguage = context.getLanguage();
            }

            CswCapabilitiesInfo cswCapabilitiesInfo = gc.getDataManager().getCswCapabilitiesInfo(dbms, currentLanguage);

            // Retrieve contact data from users table
            String contactId = gc.getSettingManager().getValue("system/csw/contactId");
            if ((contactId == null) || (contactId.equals(""))) contactId = "-1";
            Element contact = dbms.select("SELECT * FROM Users WHERE id = ?", new Integer(contactId));

            substitute(context, capabilities, cswCapabilitiesInfo,  contact.getChild("record"), currentLanguage);

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
        String language   = params.get("language");

		Element request = new Element(getName(), Csw.NAMESPACE_CSW);

		setAttrib(request, "service",        service);
		setAttrib(request, "updateSequence", sequence);
        setAttrib(request, "language",       language);

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
			Element version = i.next();

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

        for (Object o : sections.getChildren()) {
            Element section = (Element) o;
            String sectionName = section.getText();
            // Handle recognized section names only, others are ignored. Case Sensitive.
            if (sectionName.equals(Csw.SECTION_SI) || sectionName.equals(Csw.SECTION_SP)
                    || sectionName.equals(Csw.SECTION_OM)) {
                hsSections.add(sectionName);
            }
        }

		//--- remove not requested sections

		if (!hsSections.contains("ServiceIdentification"))
			capabilities.getChild("ServiceIdentification", Csw.NAMESPACE_OWS).detach();

		if (!hsSections.contains("ServiceProvider"))
			capabilities.getChild("ServiceProvider", Csw.NAMESPACE_OWS).detach();

		if (!hsSections.contains("OperationsMetadata"))
			capabilities.getChild("OperationsMetadata", Csw.NAMESPACE_OWS).detach();

	}

	//---------------------------------------------------------------------------

	private void substitute(ServiceContext context, Element capab, CswCapabilitiesInfo cswCapabilitiesInfo, Element contact, String langId) throws Exception
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getSettingManager();

		HashMap<String, String> vars = new HashMap<String, String>();

        vars.put("$PROTOCOL", sm.getValue(Geonet.Settings.SERVER_PROTOCOL));
		vars.put("$HOST",    sm.getValue(Geonet.Settings.SERVER_HOST));
		String port = sm.getValue(Geonet.Settings.SERVER_PORT);
		vars.put("$PORT", "80".equals(port) ? "" : ":" + port);
        vars.put("$END-POINT", context.getService());

        String providerName = sm.getValue("system/site/organization");
        vars.put("$PROVIDER_NAME", StringUtils.isNotEmpty(providerName)?providerName:"GeoNetwork opensource");

        vars.put("$SERVLET", context.getBaseUrl());

		// Set CSW contact information
        if (contact != null) {
            vars.put("$IND_NAME", contact.getChild("name").getValue() + " " + contact.getChild("surname").getValue());
            vars.put("$ORG_NAME", contact.getChild("organisation").getValue());
            vars.put("$POS_NAME", contact.getChild("profile").getValue());
            vars.put("$VOICE", "");
            vars.put("$FACSCIMILE", "");
            vars.put("$DEL_POINT", contact.getChild("address").getValue());
            vars.put("$CITY", contact.getChild("city").getValue());
            vars.put("$ADMIN_AREA", contact.getChild("state").getValue());
            vars.put("$POSTAL_CODE", contact.getChild("zip").getValue());
            vars.put("$COUNTRY", contact.getChild("country").getValue());
            vars.put("$EMAIL", contact.getChild("email").getValue());
            vars.put("$HOUROFSERVICE", "");
            vars.put("$CONTACT_INSTRUCTION","");
        } else {
            vars.put("$IND_NAME", "");
            vars.put("$ORG_NAME", "");
            vars.put("$POS_NAME", "");
            vars.put("$VOICE", "");
            vars.put("$FACSCIMILE", "");
            vars.put("$DEL_POINT", "");
            vars.put("$CITY", "");
            vars.put("$ADMIN_AREA", "");
            vars.put("$POSTAL_CODE", "");
            vars.put("$COUNTRY", "");
            vars.put("$EMAIL", "");
            vars.put("$HOUROFSERVICE", "");
            vars.put("$CONTACT_INSTRUCTION","");
        }

        vars.put("$TITLE", cswCapabilitiesInfo.getTitle());
        vars.put("$ABSTRACT", cswCapabilitiesInfo.getAbstract());
        vars.put("$FEES", cswCapabilitiesInfo.getFees());
        vars.put("$ACCESS_CONSTRAINTS", cswCapabilitiesInfo.getAccessConstraints());

        vars.put("$LOCALE", langId);
		
		Lib.element.substitute(capab, vars);
	}
	
	//---------------------------------------------------------------------------
	
    private void setInspireLanguages (Element capabilities, List<String> languages, String currLang, String defaultLang) {
        Element inspireExtCapabilities = capabilities.getChild("OperationsMetadata", Csw.NAMESPACE_OWS)
                .getChild("ExtendedCapabilities", Csw.NAMESPACE_INSPIRE_DS);


        Element inspireLanguages = inspireExtCapabilities.getChild("SupportedLanguages", Csw.NAMESPACE_INSPIRE_COM);

        if (defaultLang == null) defaultLang = "eng";

        try {
            // Add DefaultLanguage
            for(String lang : languages) {
                Element defaultLanguage;
                Element language;

                if (lang.equalsIgnoreCase(defaultLang)) {
                    defaultLanguage = new Element("DefaultLanguage", Csw.NAMESPACE_INSPIRE_COM);
                    language = new Element("Language", Csw.NAMESPACE_INSPIRE_COM);

                    language.setText(lang);
                    defaultLanguage.getChildren().add(language);
                    inspireLanguages.getChildren().add(defaultLanguage);

                    break;
                }
            }

            // Add list of supported languages
            for(String lang : languages) {
                Element supportedLanguage;
                Element language;

                if (!(lang.equalsIgnoreCase(defaultLang))) {
                    supportedLanguage = new Element("SupportedLanguage", Csw.NAMESPACE_INSPIRE_COM);
                    language = new Element("Language", Csw.NAMESPACE_INSPIRE_COM);

                    language.setText(lang);
                    supportedLanguage.getChildren().add(language);
                    inspireLanguages.getChildren().add(supportedLanguage);
                }


            }

            // Current language
            HashMap<String, String> vars = new HashMap<String, String>();
            if (languages.contains(currLang)) {
                vars.put("$INSPIRE_LOCALE", currLang);

            } else {      
                vars.put("$INSPIRE_LOCALE", defaultLang);
            }

            Lib.element.substitute(capabilities, vars);

        } catch (Exception ex) {
            // TODO: handle exception
            ex.printStackTrace();
        }

    }


	//---------------------------------------------------------------------------

	/**
	 * Define keyword section of the GetCapabilities
	 * document according to catalogue content. Reading 
	 * Lucene index, most popular keywords are added 
	 * to the document.
	 */
	private void setKeywords (Element capabilities, ServiceContext context, String cswServiceSpecificContraint) {
		List<Element> keywords = capabilities.getChild("ServiceIdentification", Csw.NAMESPACE_OWS).getChildren("Keywords", Csw.NAMESPACE_OWS);

		List<Element> values;
		String[] properties = {"keyword"};
		try {
			values = GetDomain.handlePropertyName(properties, context, true, CatalogConfiguration.getMaxNumberOfRecordsForKeywords(), cswServiceSpecificContraint, _luceneConfig);
		} catch (Exception e) {
            Log.error(Geonet.CSW, "Error getting domain value for specified PropertyName : " + e);
			// If GetDomain operation failed, just add nothing to the capabilities document template.            
            return;
        }

        for (int i = 0, keywordsSize = keywords.size(); i < keywordsSize; i++) {
            Element k = keywords.get(i);
            Element keyword;
            int cpt = 0;
            for (Element v : values) {
                keyword = new Element("Keyword", Csw.NAMESPACE_OWS);
                keyword.setText(v.getText());
                k.addContent(keyword);
                cpt++;
                if (cpt == CatalogConfiguration.getNumberOfKeywords()) {
                    break;
                }
            }
            // Add <ows:Type>theme</ows:Type>
            k.addContent(new Element("Type", Csw.NAMESPACE_OWS).setText("theme"));
            break; // only for first Keywords element in case of several.
        }
	}
	
	//---------------------------------------------------------------------------
	
	private void setOperationsParameters(Element capabilities) {

		List<Element> operations = capabilities.getChild("OperationsMetadata", Csw.NAMESPACE_OWS).getChildren("Operation", Csw.NAMESPACE_OWS);

		for (Element op : operations) {
			if (op.getAttributeValue(Csw.ConfigFile.Operation.Attr.NAME)
					.equals(Csw.ConfigFile.Operation.Attr.Value.GET_RECORDS)) {
				fillGetRecordsParams(op);
				continue;
			}
			if (op.getAttributeValue(Csw.ConfigFile.Operation.Attr.NAME)
					.equals(Csw.ConfigFile.Operation.Attr.Value.DESCRIBE_RECORD)) {
				fillDescribeRecordTypenames(op);
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


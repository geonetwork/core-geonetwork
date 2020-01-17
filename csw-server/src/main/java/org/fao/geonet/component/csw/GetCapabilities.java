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

package org.fao.geonet.component.csw;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.exceptions.VersionNegotiationFailedEx;
import org.fao.geonet.domain.Address;
import org.fao.geonet.domain.Language;
import org.fao.geonet.domain.Source;
import org.fao.geonet.domain.User;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.AbstractOperation;
import org.fao.geonet.kernel.csw.services.getrecords.FieldMapper;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.CswCapabilitiesInfo;
import org.fao.geonet.repository.CswCapabilitiesInfoFieldRepository;
import org.fao.geonet.repository.LanguageRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;

import jeeves.server.context.ServiceContext;
import jeeves.server.overrides.ConfigurationOverrides;

import static org.fao.geonet.kernel.setting.SettingManager.isPortRequired;

/**
 * TODO javadoc.
 */
@Component(CatalogService.BEAN_PREFIX + GetCapabilities.NAME)
public class GetCapabilities extends AbstractOperation implements CatalogService {
    static final String NAME = "GetCapabilities";
    @Autowired
    private LuceneConfig _luceneConfig;
    @Autowired
    private CatalogConfiguration _catalogConfig;
    @Autowired
    private FieldMapper _fieldMapper;
    @Autowired
    private SchemaManager _schemaManager;
    @Autowired
    private NodeInfo nodeinfo;
    @Autowired
    private SourceRepository sourceRepository;

    public String getName() {
        return NAME;
    }

    public Element execute(Element request, ServiceContext context) throws CatalogException {

        checkService(request);
        checkAcceptVersions(request);

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        boolean inspireEnabled = gc.getBean(SettingManager.class).getValueAsBool(Settings.SYSTEM_INSPIRE_ENABLE, false);


        //--- return capabilities

        Path file;

        if (inspireEnabled) {
            file = context.getAppPath().resolve("xml").resolve("csw").resolve("capabilities_inspire.xml");
        } else {
            file = context.getAppPath().resolve("xml").resolve("csw").resolve("capabilities.xml");
        }

        try {
            Element capabilities = Xml.loadFile(file);
            ServletContext servletContext = null;
            if (context.getServlet() != null) {
                servletContext = context.getServlet().getServletContext();
            }
            ConfigurationOverrides.DEFAULT.updateWithOverrides(file.toString(), servletContext, context.getAppPath(), capabilities);

            String cswServiceSpecificContraint = request.getChildText(Geonet.Elem.FILTER);
            setKeywords(capabilities, context, cswServiceSpecificContraint);
            setOperationsParameters(capabilities);

            String currentLanguage = "";

            // INSPIRE: Use language parameter if available, otherwise use default (using context.getLanguage())
            if (inspireEnabled) {
                String isoLangParamValue = request.getAttributeValue("language");


                final LanguageRepository languageRepository = context.getBean(LanguageRepository.class);
                List<Language> languageList = languageRepository.findAllByInspireFlag(true);

                List<String> langCodes = Lists.transform(languageList, new Function<Language, String>() {
                    @Nullable
                    @Override
                    public String apply(@Nonnull Language input) {
                        return input.getId();
                    }
                });

                if (isoLangParamValue != null) {
                    // Retrieve GN language id from Iso language id
                    if (langCodes.contains(isoLangParamValue)) {
                        currentLanguage = isoLangParamValue;
                    }
                }

                String defaultLanguageId = context.getLanguage();
                try {
                    Language defaultLanguage = languageRepository.findOneByDefaultLanguage();

                    if (StringUtils.isEmpty(currentLanguage)) {
                        currentLanguage = defaultLanguage.getId();
                        defaultLanguageId = defaultLanguage.getId();
                    }
                } catch (EmptyResultDataAccessException e) {
                    Log.error(Geonet.CSW, "No default language set in database languages table. " +
                        "You MUST set one default language (using isDefault column). " +
                        "Using session language as default. Error is: " + e.getMessage());
                    currentLanguage = context.getLanguage();
                }

                setInspireLanguages(capabilities, langCodes, currentLanguage, defaultLanguageId);
            } else {
                currentLanguage = context.getLanguage();
            }

            final CswCapabilitiesInfoFieldRepository infoRepository = context.getBean(CswCapabilitiesInfoFieldRepository.class);
            CswCapabilitiesInfo cswCapabilitiesInfo = infoRepository.findCswCapabilitiesInfo(currentLanguage);

            // Retrieve contact data from users table
            String contactId = gc.getBean(SettingManager.class).getValue(Settings.SYSTEM_CSW_CONTACT_ID);
            if ((contactId == null) || (contactId.equals(""))) {
                contactId = "-1";
            }
            User user = context.getBean(UserRepository.class).findOne(contactId);

            substitute(context, capabilities, cswCapabilitiesInfo, user, currentLanguage);

            handleSections(request, capabilities);

            //
            // in read-only mode, remove publication services from capabilities
            //
            if (gc.isReadOnly()) {
                capabilities = removePublicationServices(capabilities);
            }

            return capabilities;
        } catch (Exception e) {
            Log.error(Geonet.CSW, "Cannot load/process capabilities");
            Log.error(Geonet.CSW, " (C) StackTrace\n" + Util.getStackTrace(e));

            throw new NoApplicableCodeEx("Cannot load/process capabilities");
        }
    }

    /**
     * Removes CSW Harvest and CSW Transaction operations from Capabilities.
     *
     * @param capabilities the capabilities document
     * @return capabilities stripped of Harvest and Transaction
     */
    private Element removePublicationServices(Element capabilities) {
        Element operationsMetadata = capabilities.getChild(Csw.SECTION_OM, Csw.NAMESPACE_OWS);
        Element harvest = null;
        Element transaction = null;
        if (operationsMetadata != null) {
            @SuppressWarnings("unchecked")
            List<Element> operations = operationsMetadata.getChildren(Csw.OPERATION, Csw.NAMESPACE_OWS);
            for (Element operation : operations) {
                if (operation.getAttributeValue(Csw.ConfigFile.Operation.Attr.NAME).equals(Csw.ConfigFile.Operation.Attr.Value.TRANSACTION)) {
                    transaction = operation;
                } else if (operation.getAttributeValue(Csw.ConfigFile.Operation.Attr.NAME).equals(Csw.ConfigFile.Operation.Attr.Value.HARVEST)) {
                    harvest = operation;
                }
            }
            if (harvest != null) {
                operationsMetadata.removeContent(harvest);
            }
            if (transaction != null) {
                operationsMetadata.removeContent(transaction);
            }
        }
        return capabilities;
    }


    /**
     * TODO javadoc.
     */
    public Element adaptGetRequest(Map<String, String> params) {
        String service = params.get("service");
        String sections = params.get("sections");
        String sequence = params.get("updatesequence");
        String acceptVers = params.get("acceptversions");
        String acceptForm = params.get("acceptformats");
        String language = params.get("language");

        Element request = new Element(getName(), Csw.NAMESPACE_CSW);

        setAttrib(request, "service", service);
        setAttrib(request, "updateSequence", sequence);
        setAttrib(request, "language", language);

        fill(request, "AcceptVersions", "Version", acceptVers, Csw.NAMESPACE_OWS);
        fill(request, "Sections", "Section", sections, Csw.NAMESPACE_OWS);
        fill(request, "AcceptFormats", "OutputFormat", acceptForm, Csw.NAMESPACE_OWS);

        return request;
    }

    //---------------------------------------------------------------------------

    public Element retrieveValues(String parameterName) throws CatalogException {
        // TODO
        return null;
    }

    private void checkAcceptVersions(Element request) throws CatalogException {
        Element versions = request.getChild("AcceptVersions", Csw.NAMESPACE_OWS);

        if (versions == null)
            return;

        @SuppressWarnings("unchecked")
        Iterator<Element> i = versions.getChildren().iterator();

        StringBuffer sb = new StringBuffer();

        while (i.hasNext()) {
            Element version = i.next();

            if (version.getText().equals(Csw.CSW_VERSION))
                return;

            sb.append(version.getText());

            if (i.hasNext())
                sb.append(",");
        }

        throw new VersionNegotiationFailedEx(sb.toString());
    }

    /**
     * TODO javadoc.
     */
    private void handleSections(Element request, Element capabilities) {
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

    private void substitute(ServiceContext context, Element capab, CswCapabilitiesInfo cswCapabilitiesInfo, User contact, String langId) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        HashMap<String, String> vars = new HashMap<String, String>();

        String protocol = sm.getValue(Settings.SYSTEM_SERVER_PROTOCOL);
        vars.put("$PROTOCOL", protocol);
        vars.put("$HOST", sm.getValue(Settings.SYSTEM_SERVER_HOST));

        String insecureport = sm.getValue(Settings.SYSTEM_SERVER_PORT);
        String secureport = sm.getValue(Settings.SYSTEM_SERVER_SECURE_PORT);

        String port = "https".equals(protocol) ? secureport : insecureport;
        vars.put("$PORT", isPortRequired(protocol, port) ? ":" + port : "");
        vars.put("$END-POINT", context.getService());
        vars.put("$NODE_ID", context.getNodeId());

        String providerName = sm.getValue(Settings.SYSTEM_SITE_ORGANIZATION);
        vars.put("$PROVIDER_NAME", StringUtils.isNotEmpty(providerName) ? providerName : "GeoNetwork opensource");

        vars.put("$SERVLET", context.getBaseUrl());

        // Set CSW contact information
        if (contact != null) {
            vars.put("$IND_NAME", contact.getName() + " " + contact.getSurname());
            vars.put("$ORG_NAME", contact.getOrganisation());
            vars.put("$POS_NAME", contact.getProfile().name());
            vars.put("$VOICE", "");
            vars.put("$FACSCIMILE", "");
            final Address address = contact.getPrimaryAddress();
            vars.put("$DEL_POINT", address.getAddress());
            vars.put("$CITY", address.getCity());
            vars.put("$ADMIN_AREA", address.getState());
            vars.put("$POSTAL_CODE", address.getZip());
            vars.put("$COUNTRY", address.getCountry());
            vars.put("$EMAIL", contact.getEmail());
            vars.put("$HOUROFSERVICE", "");
            vars.put("$CONTACT_INSTRUCTION", "");
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
            vars.put("$CONTACT_INSTRUCTION", "");
        }
        boolean isTitleDefined = false;
        if (!NodeInfo.DEFAULT_NODE.equals(nodeinfo.getId())) {
            final Source source = sourceRepository.findOne(nodeinfo.getId());
            if (source != null) {
                vars.put("$TITLE", source.getLabelTranslations().get(langId));
                isTitleDefined = true;
            }
        }
        if (!isTitleDefined) {
            vars.put("$TITLE", cswCapabilitiesInfo.getTitle());
        }
        vars.put("$ABSTRACT", cswCapabilitiesInfo.getAbstract());
        vars.put("$FEES", cswCapabilitiesInfo.getFees());
        vars.put("$ACCESS_CONSTRAINTS", cswCapabilitiesInfo.getAccessConstraints());

        vars.put("$LOCALE", langId);

        Lib.element.substitute(capab, vars);
    }

    /**
     * TODO javadoc.
     */
    private void setInspireLanguages(Element capabilities, List<String> languages, String currLang, String defaultLang) {
        Element inspireExtCapabilities = capabilities.getChild("OperationsMetadata", Csw.NAMESPACE_OWS)
            .getChild("ExtendedCapabilities", Csw.NAMESPACE_INSPIRE_DS);


        Element inspireLanguages = inspireExtCapabilities.getChild("SupportedLanguages", Csw.NAMESPACE_INSPIRE_COM);

        if (defaultLang == null) defaultLang = "eng";

        try {
            // Add DefaultLanguage
            for (String lang : languages) {
                Element defaultLanguage;
                Element language;

                if (lang.equalsIgnoreCase(defaultLang)) {
                    defaultLanguage = new Element("DefaultLanguage", Csw.NAMESPACE_INSPIRE_COM);
                    language = new Element("Language", Csw.NAMESPACE_INSPIRE_COM);

                    language.setText(lang);
                    @SuppressWarnings("unchecked")
                    List<Element> defaultLangChildren = defaultLanguage.getChildren();
                    defaultLangChildren.add(language);
                    @SuppressWarnings("unchecked")
                    List<Element> inspireLanguagesChildren = inspireLanguages.getChildren();
                    inspireLanguagesChildren.add(defaultLanguage);

                    break;
                }
            }

            // Add list of supported languages
            for (String lang : languages) {
                Element supportedLanguage;
                Element language;

                if (!(lang.equalsIgnoreCase(defaultLang))) {
                    supportedLanguage = new Element("SupportedLanguage", Csw.NAMESPACE_INSPIRE_COM);
                    language = new Element("Language", Csw.NAMESPACE_INSPIRE_COM);

                    language.setText(lang);
                    @SuppressWarnings("unchecked")
                    List<Element> supportedLanguageChildren = supportedLanguage.getChildren();
                    supportedLanguageChildren.add(language);
                    @SuppressWarnings("unchecked")
                    List<Element> inspireLanguagesChildren = inspireLanguages.getChildren();
                    inspireLanguagesChildren.add(supportedLanguage);
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
            Log.error(Geonet.CSW, ex.getMessage(), ex);
        }

    }

    /**
     * Defines keyword section of the GetCapabilities document according to catalogue content.
     * Reading  Lucene index, most popular keywords are added to the document.
     */
    private void setKeywords(Element capabilities, ServiceContext context, String cswServiceSpecificContraint) {
        Element serviceIdentificationEl = capabilities.getChild("ServiceIdentification", Csw.NAMESPACE_OWS);
        @SuppressWarnings("unchecked")
        List<Element> keywords = serviceIdentificationEl.getChildren("Keywords", Csw.NAMESPACE_OWS);

        List<Element> values;
        String[] properties = {"keyword"};
        try {
            values = GetDomain.handlePropertyName(_catalogConfig, properties, context, true, _catalogConfig.getMaxNumberOfRecordsForKeywords(),
                cswServiceSpecificContraint, _luceneConfig);
        } catch (Exception e) {
            Log.error(Geonet.CSW, "Error getting domain value for specified PropertyName : " + e);
            // If GetDomain operation failed, just add nothing to the capabilities document template.
            return;
        }

        for (Element k : keywords) {
            Element keyword;
            int cpt = 0;
            for (Element v : values) {
                keyword = new Element("Keyword", Csw.NAMESPACE_OWS);
                keyword.setText(v.getText());
                k.addContent(keyword);
                cpt++;
                if (cpt == _catalogConfig.getNumberOfKeywords()) {
                    break;
                }
            }
            // Add <ows:Type>theme</ows:Type>
            k.addContent(new Element("Type", Csw.NAMESPACE_OWS).setText("theme"));
            break; // only for first Keywords element in case of several.
        }
    }

    /**
     * TODO javadoc.
     */
    private void setOperationsParameters(Element capabilities) {

        @SuppressWarnings("unchecked")
        List<Element> operations = capabilities.getChild("OperationsMetadata", Csw.NAMESPACE_OWS).getChildren("Operation", Csw.NAMESPACE_OWS);

        for (Element op : operations) {
            if (op.getAttributeValue(Csw.ConfigFile.Operation.Attr.NAME)
                .equals(Csw.ConfigFile.Operation.Attr.Value.GET_RECORDS)) {
                fillGetRecordsParams(op);
                continue;
            }
            if (op.getAttributeValue(Csw.ConfigFile.Operation.Attr.NAME)
                .equals(Csw.ConfigFile.Operation.Attr.Value.GET_RECORD_BY_ID)) {
                populateTypeNameAndOutputSchema(op);
                continue;
            }
            if (op.getAttributeValue(Csw.ConfigFile.Operation.Attr.NAME)
                .equals(Csw.ConfigFile.Operation.Attr.Value.DESCRIBE_RECORD)) {
                populateTypeNameAndOutputSchema(op);
            }
        }
    }

    /**
     * Based on loaded schema plugins populate the list of typeNames and outputSchema available in
     * that catalog.
     *
     * <pre>
     *   <ows:Parameter xmlns:gfc="http://www.isotc211.org/2005/gfc" name="outputSchema">
     *      <!-- Set depending on schema plugins -->
     *      <ows:Value>http://www.opengis.net/cat/csw/2.0.2</ows:Value>
     *      <ows:Value>http://www.isotc211.org/2005/gfc</ows:Value>
     *      <ows:Value>http://www.isotc211.org/2005/gmd</ows:Value>
     *    </ows:Parameter>
     *    <ows:Parameter xmlns:gfc="http://www.isotc211.org/2005/gfc" name="typeNames">
     *      <!-- Set depending on schema plugins -->
     *      <ows:Value>csw:Record</ows:Value>
     *      <ows:Value>gfc:FC_FeatureCatalogue</ows:Value>
     *      <ows:Value>gmd:MD_Metadata</ows:Value>
     *    </ows:Parameter>
     * </pre>
     */
    private void populateTypeNameAndOutputSchema(Element op) {
        Map<String, Namespace> typenames = _schemaManager.getHmSchemasTypenames();
        List<Element> operations = op.getChildren("Parameter", Csw.NAMESPACE_OWS);
        for (Element operation : operations) {
            if ("typeNames".equals(operation.getAttributeValue("name"))) {
                for (Map.Entry<String, Namespace> entry : typenames.entrySet()) {
                    String typeName = entry.getKey();
                    Namespace ns = entry.getValue();
                    String typename = typeName;
                    operation.addNamespaceDeclaration(ns);
                    operation.addContent(new Element("Value", Csw.NAMESPACE_OWS)
                        .setText(typename));
                }
            } else if ("outputSchema".equals(operation.getAttributeValue("name"))) {
                for (Map.Entry<String, Namespace> entry : typenames.entrySet()) {
                    Namespace ns = entry.getValue();
                    operation.addNamespaceDeclaration(ns);
                    operation.addContent(new Element("Value", Csw.NAMESPACE_OWS)
                        .setText(ns.getURI()));
                }
            }
        }
    }

    /**
     * TODO javadoc.
     */
    private void fillGetRecordsParams(Element op) {
        Set<String> isoQueryableMap = _fieldMapper
            .getPropertiesByType(Csw.ISO_QUERYABLES);
        Element isoConstraint = new Element("Constraint", Csw.NAMESPACE_OWS)
            .setAttribute("name", Csw.ISO_QUERYABLES);

        for (String params : isoQueryableMap) {
            isoConstraint.addContent(new Element("Value", Csw.NAMESPACE_OWS)
                .setText(params));
        }

        Set<String> additionalQueryableMap = _fieldMapper
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

        populateTypeNameAndOutputSchema(op);
    }

}

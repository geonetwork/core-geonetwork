//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
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

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.exceptions.VersionNegotiationFailedEx;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Source;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.csw.CatalogConfiguration;
import org.fao.geonet.kernel.csw.CatalogService;
import org.fao.geonet.kernel.csw.services.AbstractOperation;
import org.fao.geonet.kernel.csw.services.getrecords.FieldMapper;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fao.geonet.kernel.setting.SettingManager.isPortRequired;


/**
 * TODO javadoc.
 */
@Component(CatalogService.BEAN_PREFIX + GetCapabilities.NAME)
public class GetCapabilities extends AbstractOperation implements CatalogService {
    static final String NAME = "GetCapabilities";
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
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private IMetadataUtils metadataUtils;
    @Autowired
    private AccessManager accessManager;


    public String getName() {
        return NAME;
    }

    public Element execute(Element request, ServiceContext context) throws CatalogException {

        checkService(request);
        checkAcceptVersions(request);

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        boolean isFromRecord = false;

        String recordUuidToUseForCapability = gc.getBean(SettingManager.class).getValue(Settings.SYSTEM_CSW_CAPABILITY_RECORD_UUID);
        if (!NodeInfo.DEFAULT_NODE.equals(context.getNodeId())) {
            final Source source = sourceRepository.findById(nodeinfo.getId()).get();
            if(source.getServiceRecord() != null) {
                recordUuidToUseForCapability = source.getServiceRecord().toString();
            } else {
                recordUuidToUseForCapability = "-1";
            }
        }
        Element capabilities = null;
        String message = null;
        if (StringUtils.isNotEmpty(recordUuidToUseForCapability)
            && !"-1".equals(recordUuidToUseForCapability)) {
            Metadata record = metadataRepository.findOneByUuid(recordUuidToUseForCapability);
            if (record != null) {
                try {
                    if (accessManager.isVisibleToAll(String.valueOf(record.getId()))) {
                        String conversionFilename = "record-to-csw-capabilities.xsl";
                        String requestLanguage = request.getAttributeValue("language");
                        Map<String, Object> parameters = new HashMap<>();
                        parameters.put("outputLanguage", requestLanguage == null ? "" : requestLanguage);
                        Path conversion = context.getAppPath().resolve(Geonet.Path.CSW).resolve(conversionFilename);
                        capabilities = Xml.transform(record.getXmlData(false), conversion, parameters);
                        isFromRecord = true;
                    } else {
                        message = String.format(
                            "Record with UUID %s is not public and can't be used to build CSW GetCapabilities document. " +
                                "Choose another record or publish this one.", record.getUuid());
                        Log.warning(Geonet.CSW, message);
                    }
                } catch (Exception e) {
                    message = String.format(
                        "Error during retrieval of record with UUID %s. Error is: %s.", record.getUuid(), e.getMessage());
                    Log.warning(Geonet.CSW, message);
                }
            } else {
                // TODO: Add the message to the GetCapabilities doc ?
                message = String.format(
                    "Record with id %s is not available in the catalogue. Check the CSW configuration and choose an existing record.",
                    recordUuidToUseForCapability);
                Log.warning(Geonet.CSW, message);
            }
        }

        if (capabilities == null) {
            Path file = context.getAppPath().resolve("xml").resolve("csw").resolve("capabilities.xml");
            try {
                capabilities = Xml.loadFile(file);
                if (StringUtils.isNotEmpty(message)) {
                    capabilities.addContent(new Comment("WARNING: " + message));
                }
            } catch (JDOMException e) {
                Log.warning(Geonet.CSW, String.format("XML encoding error in file /xml/csw/capabilities.xml. Error is %s.", e.getMessage()));
            } catch (NoSuchFileException e) {
                Log.warning(Geonet.CSW, "File /xml/csw/capabilities.xml not found. Check catalogue config file.");
            }
        }

        if (capabilities == null) {
            throw new NoApplicableCodeEx("Failed to load capabilities from configuration files or from record. Check the CSW configuration.");
        }

        try {
            String cswServiceSpecificContraint = request.getChildText(Geonet.Elem.FILTER);

            if (!isFromRecord) {
                // TODOES: setKeywords(capabilities, context);
            }
            setOperationsParameters(capabilities);

            String currentLanguage = request.getAttributeValue("language");
            if (currentLanguage == null) {
                currentLanguage = context.getLanguage();
            }

            substitute(context, capabilities, currentLanguage);

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

    private void substitute(ServiceContext context, Element capab, String langId) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SettingManager sm = gc.getBean(SettingManager.class);

        Map<String, String> vars = new HashMap<String, String>();

        String protocol = sm.getValue(Settings.SYSTEM_SERVER_PROTOCOL);
        vars.put("$PROTOCOL", protocol);
        vars.put("$HOST", sm.getValue(Settings.SYSTEM_SERVER_HOST));

        Integer port;
        try {
            port = sm.getValueAsInt(Settings.SYSTEM_SERVER_PORT);
        } catch (NumberFormatException e) {
            port = null;
        }

        if (port == null) {
            if (protocol.equalsIgnoreCase(Geonet.HttpProtocol.HTTPS)) {
                port = Geonet.DefaultHttpPort.HTTPS;
            } else {
                port = Geonet.DefaultHttpPort.HTTP;
            }
        }

        vars.put("$PORT", isPortRequired(protocol, String.valueOf(port)) ? ":" + port : "");
        vars.put("$END-POINT", context.getService());
        vars.put("$NODE_ID", context.getNodeId());

        String providerName = sm.getValue(Settings.SYSTEM_SITE_ORGANIZATION);
        vars.put("$PROVIDER_NAME", StringUtils.isNotEmpty(providerName) ? providerName : "GeoNetwork opensource");

        vars.put("$SERVLET", context.getBaseUrl());

        boolean isTitleDefined = false;
        String sourceUuid = NodeInfo.DEFAULT_NODE.equals(nodeinfo.getId()) ? sm.getSiteId() : nodeinfo.getId();
        final Source source = sourceRepository.findById(sourceUuid).get();
        if (source != null) {
            vars.put("$TITLE", source.getLabelTranslations().get(langId));
            isTitleDefined = true;
        } else {
            vars.put("$TITLE", sm.getSiteName());
        }
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
            Map<String, String> vars = new HashMap<String, String>();
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
    private void setKeywords(Element capabilities, ServiceContext context) {
        Element serviceIdentificationEl = capabilities.getChild("ServiceIdentification", Csw.NAMESPACE_OWS);
        @SuppressWarnings("unchecked")
        List<Element> keywords = serviceIdentificationEl.getChildren("Keywords", Csw.NAMESPACE_OWS);

        List<Element> values;
        String[] properties = {"keyword"};
        try {
            values = GetDomain.handlePropertyName(_catalogConfig, properties, context, true, _catalogConfig.getMaxNumberOfRecordsForKeywords());
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

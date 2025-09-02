//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.datamanager.base;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_METADATA_VALIDATION_REMOVESCHEMALOCATION;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.SchematronValidator;
import org.fao.geonet.kernel.SchematronValidatorExternalMd;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlErrorHandler;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

public class BaseMetadataValidator implements org.fao.geonet.kernel.datamanager.IMetadataValidator, BaseErrorHandlerAttachingErrorToElem.ElementDecorator {
    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.DATA_MANAGER);

    @Autowired
    private IMetadataSchemaUtils metadataSchemaUtils;

    @Autowired
    private SchematronValidator schematronValidator;

    @Autowired
    // To validate external metadata that is not yet available in the catalogue: import/harvesters
    private SchematronValidatorExternalMd schematronValidatorExternalMd;

    @Autowired
    private MetadataValidationRepository validationRepository;

    @Autowired
    @Lazy
    private SettingManager settingManager;

    private IMetadataManager metadataManager;

    @Override
    public void setMetadataManager(IMetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }


    /**
     * Validates metadata against XSD and schematron files related to metadata schema throwing XSDValidationErrorEx if xsd errors or
     * SchematronValidationErrorEx if schematron rules fails.
     *
     * Used for metadata that is not yet in the catalogue like import/harvesting.
     */
    @Override
    public void validateExternalMetadata(String schema, Element xml, ServiceContext context, String fileName, Integer groupOwner) throws Exception {
        setNamespacePrefix(xml);

        XmlErrorHandler eh = new XmlErrorHandler();
        Element xsdErrors = validateInfo(schema, xml, eh);
        if (xsdErrors != null) {
            if (!fileName.equals(" ")) {
                throw new XSDValidationErrorEx("XSD Validation error(s):\n" + Xml.getString(xsdErrors) + "(in " + fileName + "): ", xsdErrors);
            } else {
                throw new XSDValidationErrorEx("XSD Validation error(s):\n" + Xml.getString(xsdErrors), xsdErrors);
            }
        }

        // --- Now do the schematron validation on this file - if there are errors
        // --- then we say what they are!
        // --- Note we have to use uuid here instead of id because we don't have an id...

        Element schemaTronReport = doSchemaTronForEditor(schema, xml, context.getLanguage(), groupOwner);
        xml.detach();
        if (schemaTronReport != null && schemaTronReport.getContent().size() > 0) {

            List<Namespace> theNSs = new ArrayList<Namespace>();
            theNSs.add(Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork"));
            theNSs.add(Namespace.getNamespace("svrl", "http://purl.oclc.org/dsdl/svrl"));

            List<?> informationalReports = Xml.selectNodes(schemaTronReport,
                "geonet:report[@geonet:required != '" + SchematronRequirement.REQUIRED + "']", theNSs);
            for (Object informationalReport : informationalReports) {
                ((Element) informationalReport).detach();
            }
            List<?> failedAssert = Xml.selectNodes(schemaTronReport,
                "geonet:report[@geonet:required = '" + SchematronRequirement.REQUIRED + "']/svrl:schematron-output/svrl:failed-assert",
                theNSs);

            List<?> failedSchematronVerification = Xml.selectNodes(schemaTronReport,
                "geonet:report[@geonet:required = '" + SchematronRequirement.REQUIRED + "']/geonet:schematronVerificationError",
                theNSs);

            if ((!failedAssert.isEmpty()) || (!failedSchematronVerification.isEmpty())) {
                StringBuilder errorReport = new StringBuilder();

                Iterator reports = schemaTronReport.getDescendants(ReportFinder);
                while (reports.hasNext()) {
                    Element report = (Element) reports.next();
                    Element schematronVerificationError = report.getChild("schematronVerificationError", Edit.NAMESPACE);


                    if (schematronVerificationError != null) {
                        errorReport.append("schematronVerificationError: " + schematronVerificationError.getTextTrim());
                    } else {
                        Iterator errors = report.getDescendants(ErrorFinder);
                        while (errors.hasNext()) {
                            Element err = (Element) errors.next();

                            StringBuilder msg = new StringBuilder();
                            String reportType;
                            if (err.getName().equals("failed-assert")) {
                                reportType = report.getAttributeValue("rule", Edit.NAMESPACE);
                                reportType = reportType == null ? "No name for rule" : reportType;

                                Iterator descendants = err.getDescendants();
                                while (descendants.hasNext()) {
                                    Object node = descendants.next();
                                    if (node instanceof Element) {
                                        String textTrim = ((Element) node).getTextTrim();
                                        msg.append(textTrim).append(" \n");
                                    }
                                }
                            } else {
                                reportType = "Xsd Error";
                                msg.append(err.getChildText("message", Edit.NAMESPACE));
                            }

                            if (msg.length() > 0) {
                                errorReport.append(reportType).append(':').append(msg);
                            }
                        }
                    }
                }

                throw new SchematronValidationErrorEx(
                    "Schematron errors detected for file " + fileName + " - " + errorReport + " for more details", schemaTronReport);
            }
        }

    }

    @Override
    public void setNamespacePrefix(final Element md) {
        // --- if the metadata has no namespace or already has a namespace then
        // --- we must skip this phase

        Namespace ns = md.getNamespace();
        if (ns != Namespace.NO_NAMESPACE && (md.getNamespacePrefix().equals(""))) {
            // --- set prefix for iso19139 metadata

            ns = Namespace.getNamespace("gmd", md.getNamespace().getURI());
            setNamespacePrefix(md, ns);
        }
    }

    @Override
    public void setNamespacePrefix(final Element md, final Namespace ns) {
        if (md.getNamespaceURI().equals(ns.getURI())) {
            md.setNamespace(ns);
        }

        Attribute xsiType = md.getAttribute("type", Namespaces.XSI);
        if (xsiType != null) {
            String xsiTypeValue = xsiType.getValue();

            if (StringUtils.isNotEmpty(xsiTypeValue) && !xsiTypeValue.contains(":")) {
                xsiType.setValue(ns.getPrefix() + ":" + xsiType.getValue());
            }
        }

        for (Object o : md.getChildren()) {
            setNamespacePrefix((Element) o, ns);
        }
    }

    /**
     * Use this validate method for XML documents with xsd validation.
     */
    @Override
    public void validate(String schema, Element md) throws Exception {

        if (Log.isTraceEnabled(Geonet.DATA_MANAGER)) {
            Log.trace(Geonet.DATA_MANAGER, "Validating record ");
            Log.trace(Geonet.DATA_MANAGER, (new org.jdom.output.XMLOutputter()).outputString(md));
        }

        XmlErrorHandler eh = new XmlErrorHandler();
        Element xsdErrors = validateInfo(schema, md, eh);
        if (xsdErrors != null) {
            throw new XSDValidationErrorEx("XSD Validation error(s):\n" + Xml.getString(xsdErrors), xsdErrors);
        }
    }

    private Element validateInfo(String schema, Element md, XmlErrorHandler eh) throws Exception {
        if (settingManager.getValueAsBool(SYSTEM_METADATA_VALIDATION_REMOVESCHEMALOCATION, false)) {
            md.removeAttribute("schemaLocation", Namespaces.XSI);
        }
        String schemaLoc = md.getAttributeValue("schemaLocation", Namespaces.XSI);
        LOGGER.debug("Extracted schemaLocation of {}", schemaLoc);
        boolean noChoiceButToUseSchemaLocation = schema == null;
        boolean isSchemaLocationDefinedInMd = schemaLoc != null && schemaLoc != "";

        if (noChoiceButToUseSchemaLocation || isSchemaLocationDefinedInMd) {
            return Xml.validateInfo(md, eh, schema);
        } else {
            return Xml.validateInfo(metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.SCHEMA), md, eh, schema);
        }
    }

    /**
     * Creates XML schematron report.
     */
    @Override
    public Element doSchemaTronForEditor(String schema, Element md, String lang, Integer groupOwner) throws Exception {
        // enumerate the metadata xml so that we can report any problems found
        // by the schematron_xml script to the geonetwork editor
        metadataManager.getEditLib().enumerateTree(md);

        // get an xml version of the schematron errors and return for error display
        Element schemaTronXmlReport = getSchemaTronXmlReport(schema, md, lang, null, groupOwner);

        // remove editing info added by enumerateTree
        metadataManager.getEditLib().removeEditingInfo(md);

        return schemaTronXmlReport;
    }

    /**
     * Valid the metadata record against its schema. For each error found,
     * one or more validation reports are added to the corresponding element
     * trying to find the element based on the xpath returned by the ErrorHandler.
     */
    private Element getXSDXmlReport(String schema, Element md, boolean forEditing) {
        // NOTE: this method assumes that enumerateTree has NOT been run on the metadata
        XmlErrorHandler errorHandler;
        if (forEditing) {
            errorHandler = new BaseErrorHandlerAttachingErrorToElem();
            ((BaseErrorHandlerAttachingErrorToElem) errorHandler).setElementDecorator(this);
        } else {
            errorHandler = new XmlErrorHandler();
        }
        errorHandler.setNs(Edit.NAMESPACE);
        try {
            Element xsdErrors = validateInfo(schema, md, errorHandler);
            if (forEditing) {
                ((BaseErrorHandlerAttachingErrorToElem) errorHandler).attachReports();
            }
            return xsdErrors;
        } catch (Exception e) {
            return JeevesException.toElement(e);
        }
    }

    public Element buildErrorReport(String type, String errorCode, String message, String xpath) {
        Element report = new Element(Edit.ValidationReport.VALIDATIONREPORT, Edit.NAMESPACE);
        report.setAttribute(Edit.ValidationReport.TYPE, type, Edit.NAMESPACE);
        report.setAttribute(Edit.ValidationReport.XPATH, xpath, Edit.NAMESPACE);
        report.setAttribute(Edit.ValidationReport.ERRORCODE, errorCode, Edit.NAMESPACE);
        report.setAttribute(Edit.ValidationReport.MESSAGE, message, Edit.NAMESPACE);
        return report;
    }

    /**
     * Creates XML schematron report for each set of rules defined in schema directory.
     */
    private Element getSchemaTronXmlReport(String schema, Element md, String lang, Map<String, Integer[]> valTypeAndStatus, Integer groupOwner)
            throws Exception {
        // NOTE: this method assumes that you've run enumerateTree on the metadata

        List<MetadataValidation> validations = new ArrayList<>();
        // Schematron report is composed of one or more report(s) for each set of rules.
        Element schemaTronXmlOut = schematronValidatorExternalMd.applyCustomSchematronRules(schema, md, lang, validations, groupOwner);

        for(Element report : (List<Element>) schemaTronXmlOut.getChildren()) {
            Element xmlReport = report.getChild("schematron-output", Namespaces.SVRL);

            if (xmlReport != null) {
                String ruleId = xmlReport.getAttributeValue("rule", Edit.NAMESPACE);

                // add results to persitent validation information
                int firedRules = 0;
                Iterator<?> firedRulesElems = xmlReport.getDescendants(new ElementFilter("fired-rule", Namespaces.SVRL));
                while (firedRulesElems.hasNext()) {
                    firedRulesElems.next();
                    firedRules++;
                }
                int invalidRules = 0;
                Iterator<?> faileAssertElements = xmlReport.getDescendants(new ElementFilter("failed-assert", Namespaces.SVRL));
                while (faileAssertElements.hasNext()) {
                    faileAssertElements.next();
                    invalidRules++;
                }
                Integer[] results = { invalidRules != 0 ? 0 : 1, firedRules, invalidRules };
                if (valTypeAndStatus != null) {
                    valTypeAndStatus.put(ruleId, results);
                }
            }

        }

        return schemaTronXmlOut;
    }

    /**
     * Used by services that need to validate metadata already existing in the catalogue.
     *
     * @param metadata metadata
     * @param lang     Language from context
     */
    @Override
    public Pair<Element, Boolean> doValidate(AbstractMetadata metadata, String lang) throws Exception {
        String schema = metadata.getDataInfo().getSchemaId();
        int metadataId = metadata.getId();
        Element errorReport = new Element("report", Edit.NAMESPACE);
        errorReport.setAttribute("id", String.valueOf(metadataId), Edit.NAMESPACE);

        Element md;
        try {
            md = metadata.getXmlData(false);
        } catch (IOException | JDOMException e) {
            return Pair.read(errorReport, false);
        }

        // Inflate the metadata so that it contains all the necessary information for validation.
        md = metadataManager.inflateMetadata(md, schema, lang);

        List<MetadataValidation> validations = new ArrayList<>();
        boolean valid = true;

        LOGGER.debug("Validating against XSD {}", schema);
        // do XSD validation, but in this case just counting errors number should be sufficient
        Element xsdErrors = getXSDXmlReport(schema, md, false);

        int xsdErrorCount = 0;
        if (xsdErrors != null && xsdErrors.getContent().size() > 0) {
            xsdErrorCount = xsdErrors.getContent().size();
        }
        if (xsdErrorCount > 0) {
            errorReport.addContent(xsdErrors);
            validations.add(new MetadataValidation().setId(new MetadataValidationId(metadataId, "xsd"))
                .setStatus(MetadataValidationStatus.INVALID).setRequired(true).setNumTests(xsdErrorCount)
                .setNumFailures(xsdErrorCount));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("  - XSD error: {}", Xml.getString(xsdErrors));
            }
            valid = false;
        } else {
            validations.add(new MetadataValidation().setId(new MetadataValidationId(metadataId, "xsd"))
                .setStatus(MetadataValidationStatus.VALID).setRequired(true).setNumTests(1).setNumFailures(0));
            LOGGER.debug("  - XSD Valid.");
        }
        try {
            metadataManager.getEditLib().enumerateTree(md);

            // Apply custom schematron rules
            Element schemaTronReport = applyCustomSchematronRules(schema, metadataId, md, lang, validations);
            if (schemaTronReport != null) {
                List<Namespace> theNSs = new ArrayList<Namespace>();
                theNSs.add(Namespace.getNamespace("geonet", "http://www.fao.org/geonetwork"));
                theNSs.add(Namespace.getNamespace("svrl", "http://purl.oclc.org/dsdl/svrl"));

                // Get all the errors
                List<?> errors = Xml.selectNodes(schemaTronReport,
                    "geonet:report[@geonet:required = '" + SchematronRequirement.REQUIRED + "']/svrl:schematron-output/svrl:failed-assert" +
                    " | geonet:report[@geonet:required = '" + SchematronRequirement.REQUIRED + "']/geonet:schematronVerificationError",
                    theNSs);

                // Get all the warnings
                List<?> warnings = Xml.selectNodes(schemaTronReport,
                    "geonet:report[@geonet:required = '" + SchematronRequirement.REPORT_ONLY + "']/svrl:schematron-output/svrl:failed-assert" +
                        " | geonet:report[@geonet:required = '" + SchematronRequirement.REPORT_ONLY + "']/geonet:schematronVerificationError",
                    theNSs);

                // If there are errors the report is not valid
                if (!errors.isEmpty()) {
                    valid = false;
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("  - Schematron error: {}", Xml.getString(schemaTronReport));
                    }
                } else if (!warnings.isEmpty()) {
                    LOGGER.debug("  - Schematron warning: {}", Xml.getString(schemaTronReport));
                }

                // Add the schematron report content if there are errors or warnings
                if (!errors.isEmpty() || !warnings.isEmpty()) {
                    errorReport.addContent(schemaTronReport);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Could not run schematron validation on metadata {}.", metadataId);
            LOGGER.error("Could not run schematron validation on metadata, exception", e);
            valid = false;
        } finally {
            metadataManager.getEditLib().removeEditingInfo(md);
        }

        saveValidationStatus(metadataId, validations);

        return Pair.read(errorReport, valid);
    }

    /**
     * Used by the validate embedded service to validate metadata already existing in the catalogue.
     * The validation report is stored in the session.
     *
     */
    @Override
    public Pair<Element, String> doValidate(UserSession session, String schema, String metadataId, Element md, String lang,
                                            boolean forEditing) throws Exception {
        int intMetadataId = Integer.parseInt(metadataId);
        String version = null;
        LOGGER.debug("Creating validation report for record #{} [schema: {}].", metadataId, schema);

        List<MetadataValidation> validations = new ArrayList<>();
        Element errorReport = new Element("report", Edit.NAMESPACE);
        errorReport.setAttribute("id", metadataId, Edit.NAMESPACE);

        // -- get an XSD validation report and add results to the metadata
        // -- as geonet:xsderror attributes on the affected elements
        Element xsdErrors = getXSDXmlReport(schema, md, forEditing);
        int xsdErrorCount = 0;
        if (xsdErrors != null) {
            xsdErrorCount = xsdErrors.getContent().size();
        }
        if (xsdErrorCount > 0) {
            errorReport.addContent(xsdErrors);
            validations.add(new MetadataValidation().setId(new MetadataValidationId(intMetadataId, "xsd"))
                .setStatus(MetadataValidationStatus.INVALID).setRequired(true).setNumTests(xsdErrorCount)
                .setNumFailures(xsdErrorCount));
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("  - XSD error: {}", Xml.getString(xsdErrors));
            }
        } else {
            validations.add(new MetadataValidation().setId(new MetadataValidationId(intMetadataId, "xsd"))
                .setStatus(MetadataValidationStatus.VALID).setRequired(true).setNumTests(1).setNumFailures(0));
            LOGGER.trace("Valid.");
        }

        // ...then schematrons
        Element error = null;
        if (forEditing) {
            LOGGER.debug("  - Schematron in editing mode.");
            // -- now expand the elements and add the geonet: elements
            metadataManager.getEditLib().expandElements(schema, md);
            version = metadataManager.getEditLib().getVersionForEditing(schema, metadataId, md);

            error = applyCustomSchematronRules(schema, intMetadataId, md, lang, validations);
        } else {
            // enumerate the metadata xml so that we can report any problems found by the schematron_xml script to the geonetwork editor
            metadataManager.getEditLib().enumerateTree(md);
            try {
                error = applyCustomSchematronRules(schema, intMetadataId, md, lang, validations);
            } catch (Exception e) {
                LOGGER.error("Could not run schematron validation on metadata {}.", metadataId);
                LOGGER.error("Could not run schematron validation on metadata, exception.", e);
            } finally {
                // remove editing info added by enumerateTree
                metadataManager.getEditLib().removeEditingInfo(md);
            }
        }

        if (error != null) {
            errorReport.addContent(error);
        }

        saveValidationStatus(intMetadataId, validations);

        return Pair.read(errorReport, version);
    }

    /**
     * Creates XML schematron report for each set of rules defined in schema directory. This method assumes that you've run enumerateTree on
     * the metadata
     * <p>
     * Returns null if no error on validation.
     */
    @Override
    public Element applyCustomSchematronRules(String schema, int metadataId, Element md, String lang,
                                              List<MetadataValidation> validations) {
        return schematronValidator.applyCustomSchematronRules(schema, metadataId, md, lang, validations);
    }

    /**
     * Saves validation status information into the database for the current record.
     *
     * @param id          the metadata record internal identifier
     * @param validations the validation reports for each type of validation and schematron validation
     */
    @Transactional
    void saveValidationStatus(int id, List<MetadataValidation> validations) {
        try {
            validationRepository.deleteAllInternalValidationById_MetadataId(id);
            validationRepository.saveAll(validations);
        } catch (Exception e) {
            LOGGER.error("Could not save validation status on metadata {}.", id);
            LOGGER.error("Could not save validation status on metadata, exception: ", e);
        }

    }

    /**
     * Validates an xml document, using autodetectschema to determine how.
     *
     * @return true if metadata is valid
     */
    @Override
    public boolean validate(Element xml) {
        try {
            String schema = metadataSchemaUtils.autodetectSchema(xml);
            validate(schema, xml);
            return true;
        }
        // XSD validation error(s)
        catch (Exception x) {
            // do not print stacktrace as this is 'normal' program flow
            LOGGER.debug("invalid metadata", x);
            return false;
        }
    }

    /**
     * Filter to find errors in schematron response (error on REQUIRED elements)
     */
    public static final Filter ErrorFinder = new Filter() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean matches(Object obj) {
            if (obj instanceof Element) {
                Element element = (Element) obj;
                String name = element.getName();
                if (name.equals("error")) {
                    return true;
                } else if (name.equals("failed-assert")) {
                    return true;
                }
            }
            return false;
        }
    };

    public static final Filter ReportFinder = new Filter() {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean matches(Object obj) {
            if (obj instanceof Element) {
                Element element = (Element) obj;
                String name = element.getName();
                if (name.equals("report") || name.equals("xsderrors")) {
                    return true;
                }
            }
            return false;
        }
    };
}

package org.fao.geonet.kernel.datamanager.base;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.SchematronRequirement;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.SchematronValidator;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.Xml.ErrorHandler;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.jdom.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

public class BaseMetadataValidator implements org.fao.geonet.kernel.datamanager.IMetadataValidator {

    @Autowired
    private IMetadataSchemaUtils metadataSchemaUtils;

    private Path thesaurusDir;
    @Autowired
    private SchematronValidator schematronValidator;
    @Autowired
    private MetadataValidationRepository validationRepository;

    private IMetadataManager metadataManager;
    @Autowired
    @Lazy
    private SettingManager settingManager;

    @Override
    public void setMetadataManager(IMetadataManager metadataManager) {
        this.metadataManager = metadataManager;
    }

    public void init(ServiceContext context, Boolean force) throws Exception {
        metadataSchemaUtils = context.getBean(IMetadataSchemaUtils.class);
        validationRepository = context.getBean(MetadataValidationRepository.class);
        schematronValidator = context.getBean(SchematronValidator.class);
        thesaurusDir = context.getBean(ThesaurusManager.class).getThesauriDirectory();
        settingManager = context.getBean(SettingManager.class);
    }

    /**
     * Validates metadata against XSD and schematron files related to metadata schema throwing XSDValidationErrorEx if xsd errors or
     * SchematronValidationErrorEx if schematron rules fails.
     */
    @Override
    public void validateMetadata(String schema, Element xml, ServiceContext context, String fileName) throws Exception {
        setNamespacePrefix(xml);
        try {
            validate(schema, xml);
        } catch (XSDValidationErrorEx e) {
            if (!fileName.equals(" ")) {
                throw new XSDValidationErrorEx(e.getMessage() + "(in " + fileName + "): ", e.getObject());
            } else {
                throw new XSDValidationErrorEx(e.getMessage(), e.getObject());
            }
        }

        // --- Now do the schematron validation on this file - if there are errors
        // --- then we say what they are!
        // --- Note we have to use uuid here instead of id because we don't have
        // --- an id...

        Element schemaTronReport = doSchemaTronForEditor(schema, xml, context.getLanguage());
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

                throw new SchematronValidationErrorEx(
                        "Schematron errors detected for file " + fileName + " - " + errorReport + " for more details", schemaTronReport);
            }
        }

    }

    /**
     *
     * @param md
     */
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

    /**
     *
     * @param md
     * @param ns
     */
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
     * Use this validate method for XML documents with dtd.
     */
    @Override
    public void validate(String schema, Document doc) throws Exception {
        Xml.validate(doc);
    }

    /**
     * Use this validate method for XML documents with xsd validation.
     */
    @Override
    public void validate(String schema, Element md) throws Exception {
        if (getSettingManager().getValueAsBool(Settings.SYSTEM_METADATA_VALIDATION_REMOVESCHEMALOCATION, false)) {
            md.removeAttribute("schemaLocation", Namespaces.XSI);
        }
        String schemaLoc = md.getAttributeValue("schemaLocation", Namespaces.XSI);
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted schemaLocation of " + schemaLoc);
        if (schemaLoc == null)
            schemaLoc = "";

        if (schema == null) {
            // must use schemaLocation
            Xml.validate(md);
        } else {
            // if schemaLocation use that
            if (!schemaLoc.equals("")) {
                Xml.validate(md);
                // otherwise use supplied schema name
            } else {
                Xml.validate(metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.SCHEMA), md);
            }
        }
    }

    @Override
    public Element validateInfo(String schema, Element md, ErrorHandler eh) throws Exception {
        if (getSettingManager().getValueAsBool(Settings.SYSTEM_METADATA_VALIDATION_REMOVESCHEMALOCATION, false)) {
            md.removeAttribute("schemaLocation", Namespaces.XSI);
        }
        String schemaLoc = md.getAttributeValue("schemaLocation", Namespaces.XSI);
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Extracted schemaLocation of " + schemaLoc);
        if (schemaLoc == null)
            schemaLoc = "";

        if (schema == null) {
            // must use schemaLocation
            return Xml.validateInfo(md, eh);
        } else {
            // if schemaLocation use that
            if (!schemaLoc.equals("")) {
                return Xml.validateInfo(md, eh);
                // otherwise use supplied schema name
            } else {
                return Xml.validateInfo(metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.SCHEMA), md, eh);
            }
        }
    }

    /**
     * Creates XML schematron report.
     */
    @Override
    public Element doSchemaTronForEditor(String schema, Element md, String lang) throws Exception {
        // enumerate the metadata xml so that we can report any problems found
        // by the schematron_xml script to the geonetwork editor
        metadataManager.getEditLib().enumerateTree(md);

        // get an xml version of the schematron errors and return for error display
        Element schemaTronXmlReport = getSchemaTronXmlReport(schema, md, lang, null);

        // remove editing info added by enumerateTree
        metadataManager.getEditLib().removeEditingInfo(md);

        return schemaTronXmlReport;
    }

    /**
     * Valid the metadata record against its schema. For each error found, an xsderror attribute is added to the corresponding element
     * trying to find the element based on the xpath return by the ErrorHandler.
     */
    private synchronized Element getXSDXmlReport(String schema, Element md) {
        // NOTE: this method assumes that enumerateTree has NOT been run on the metadata
        ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setNs(Edit.NAMESPACE);
        Element xsdErrors;

        try {
            xsdErrors = validateInfo(schema, md, errorHandler);
        } catch (Exception e) {
            xsdErrors = JeevesException.toElement(e);
            return xsdErrors;
        }

        if (xsdErrors != null) {
            MetadataSchema mds = metadataSchemaUtils.getSchema(schema);
            List<Namespace> schemaNamespaces = mds.getSchemaNS();

            // -- now get each xpath and evaluate it
            // -- xsderrors/xsderror/{message,xpath}
            @SuppressWarnings("unchecked")
            List<Element> list = xsdErrors.getChildren();
            for (Element elError : list) {
                String xpath = elError.getChildText("xpath", Edit.NAMESPACE);
                String message = elError.getChildText("message", Edit.NAMESPACE);
                message = "\\n" + message;

                // -- get the element from the xpath and add the error message to it
                Element elem = null;
                try {
                    elem = Xml.selectElement(md, xpath, schemaNamespaces);
                } catch (JDOMException je) {
                    je.printStackTrace();
                    Log.error(Geonet.DATA_MANAGER, "Attach xsderror message to xpath " + xpath + " failed: " + je.getMessage());
                }
                if (elem != null) {
                    String existing = elem.getAttributeValue("xsderror", Edit.NAMESPACE);
                    if (existing != null)
                        message = existing + message;
                    elem.setAttribute("xsderror", message, Edit.NAMESPACE);
                } else {
                    Log.warning(Geonet.DATA_MANAGER, "WARNING: evaluating XPath " + xpath
                            + " against metadata failed - XSD validation message: " + message + " will NOT be shown by the editor");
                }
            }
        }
        return xsdErrors;
    }

    /**
     * Creates XML schematron report for each set of rules defined in schema directory.
     */
    private Element getSchemaTronXmlReport(String schema, Element md, String lang, Map<String, Integer[]> valTypeAndStatus)
            throws Exception {
        // NOTE: this method assumes that you've run enumerateTree on the
        // metadata

        MetadataSchema metadataSchema = metadataSchemaUtils.getSchema(schema);
        String[] rules = metadataSchema.getSchematronRules();

        // Schematron report is composed of one or more report(s)
        // for each set of rules.
        Element schemaTronXmlOut = new Element("schematronerrors", Edit.NAMESPACE);
        if (rules != null) {
            for (String rule : rules) {
                // -- create a report for current rules.
                // Identified by a rule attribute set to shematron file name
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, " - rule:" + rule);
                String ruleId = rule.substring(0, rule.indexOf(".xsl"));
                Element report = new Element("report", Edit.NAMESPACE);
                report.setAttribute("rule", ruleId, Edit.NAMESPACE);

                java.nio.file.Path schemaTronXmlXslt = metadataSchema.getSchemaDir().resolve("schematron").resolve(rule);
                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("lang", lang);
                    params.put("rule", rule);
                    params.put("thesaurusDir", this.thesaurusDir);
                    Element xmlReport = Xml.transform(md, schemaTronXmlXslt, params);
                    if (xmlReport != null) {
                        report.addContent(xmlReport);
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
                } catch (Exception e) {
                    Log.error(Geonet.DATA_MANAGER, "WARNING: schematron xslt " + schemaTronXmlXslt + " failed");

                    // If an error occurs that prevents to verify schematron rules, add to show in report
                    Element errorReport = new Element("schematronVerificationError", Edit.NAMESPACE);
                    errorReport.addContent("Schematron error ocurred, rules could not be verified: " + e.getMessage());
                    report.addContent(errorReport);

                    e.printStackTrace();
                }

                // -- append report to main XML report.
                schemaTronXmlOut.addContent(report);
            }
        }
        return schemaTronXmlOut;
    }

    /**
     * Used by harvesters that need to validate metadata.
     *
     * @param schema name of the schema to validate against
     * @param metadataId metadata id - used to record validation status
     * @param doc metadata document as JDOM Document not JDOM Element
     * @param lang Language from context
     */
    @Override
    public boolean doValidate(String schema, String metadataId, Document doc, String lang) {
        Integer intMetadataId = Integer.valueOf(metadataId);
        List<MetadataValidation> validations = new ArrayList<>();
        boolean valid = true;

        if (doc.getDocType() != null) {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "Validating against dtd " + doc.getDocType());

            // if document has a doctype then validate using that (assuming that the
            // dtd is either mapped locally or will be cached after first validate)
            try {
                Xml.validate(doc);
                validations.add(new MetadataValidation().setId(new MetadataValidationId(intMetadataId, "dtd"))
                        .setStatus(MetadataValidationStatus.VALID).setRequired(true).setNumTests(1).setNumFailures(0));
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "Valid.");
                }
            } catch (Exception e) {
                validations.add(new MetadataValidation().setId(new MetadataValidationId(intMetadataId, "dtd"))
                        .setStatus(MetadataValidationStatus.INVALID).setRequired(true).setNumTests(1).setNumFailures(1));

                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "Invalid.", e);
                }
                valid = false;
            }
        } else {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "Validating against XSD " + schema);
            }
            // do XSD validation
            Element md = doc.getRootElement();
            Element xsdErrors = getXSDXmlReport(schema, md);

            int xsdErrorCount = 0;
            if (xsdErrors != null && xsdErrors.getContent().size() > 0) {
                xsdErrorCount = xsdErrors.getContent().size();
            }
            if (xsdErrorCount > 0) {
                validations.add(new MetadataValidation().setId(new MetadataValidationId(intMetadataId, "xsd"))
                        .setStatus(MetadataValidationStatus.INVALID).setRequired(true).setNumTests(xsdErrorCount)
                        .setNumFailures(xsdErrorCount));
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Invalid.");
                valid = false;
            } else {
                validations.add(new MetadataValidation().setId(new MetadataValidationId(intMetadataId, "xsd"))
                        .setStatus(MetadataValidationStatus.VALID).setRequired(true).setNumTests(1).setNumFailures(0));
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Valid.");
            }
            try {
                metadataManager.getEditLib().enumerateTree(md);
                // Apply custom schematron rules
                Element errors = applyCustomSchematronRules(schema, Integer.parseInt(metadataId), doc.getRootElement(), lang, validations);
                valid = valid && errors == null;
                metadataManager.getEditLib().removeEditingInfo(md);
            } catch (Exception e) {
                e.printStackTrace();
                Log.error(Geonet.DATA_MANAGER, "Could not run schematron validation on metadata " + metadataId + ": " + e.getMessage());
                valid = false;
            }
        }

        // now save the validation status
        try {
            saveValidationStatus(intMetadataId, validations);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(Geonet.DATA_MANAGER, "Could not save validation status on metadata " + metadataId + ": " + e.getMessage());
        }

        return valid;
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Metadata Delete API
    // ---
    // --------------------------------------------------------------------------

    /**
     * Used by the validate embedded service. The validation report is stored in the session.
     *
     */
    @Override
    public Pair<Element, String> doValidate(UserSession session, String schema, String metadataId, Element md, String lang,
            boolean forEditing) throws Exception {
        int intMetadataId = Integer.parseInt(metadataId);
        String version = null;
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER, "Creating validation report for record #" + metadataId + " [schema: " + schema + "].");

        Element sessionReport = (Element) session.getProperty(Geonet.Session.VALIDATION_REPORT + metadataId);
        if (sessionReport != null && !forEditing) {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "  Validation report available in session.");
            sessionReport.detach();
            return Pair.read(sessionReport, version);
        }

        List<MetadataValidation> validations = new ArrayList<>();
        Element errorReport = new Element("report", Edit.NAMESPACE);
        errorReport.setAttribute("id", metadataId, Edit.NAMESPACE);

        // -- get an XSD validation report and add results to the metadata
        // -- as geonet:xsderror attributes on the affected elements
        Element xsdErrors = getXSDXmlReport(schema, md);
        int xsdErrorCount = 0;
        if (xsdErrors != null) {
            xsdErrorCount = xsdErrors.getContent().size();
        }
        if (xsdErrorCount > 0) {
            errorReport.addContent(xsdErrors);
            validations.add(new MetadataValidation().setId(new MetadataValidationId(intMetadataId, "xsd"))
                    .setStatus(MetadataValidationStatus.INVALID).setRequired(true).setNumTests(xsdErrorCount)
                    .setNumFailures(xsdErrorCount));

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "  - XSD error: " + Xml.getString(xsdErrors));
            }
        } else {
            validations.add(new MetadataValidation().setId(new MetadataValidationId(intMetadataId, "xsd"))
                    .setStatus(MetadataValidationStatus.VALID).setRequired(true).setNumTests(1).setNumFailures(0));

            if (Log.isTraceEnabled(Geonet.DATA_MANAGER)) {
                Log.trace(Geonet.DATA_MANAGER, "Valid.");
            }
        }

        // ...then schematrons
        // edit mode
        Element error = null;
        if (forEditing) {
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "  - Schematron in editing mode.");
            // -- now expand the elements and add the geonet: elements
            metadataManager.getEditLib().expandElements(schema, md);
            version = metadataManager.getEditLib().getVersionForEditing(schema, metadataId, md);

            // Apply custom schematron rules
            error = applyCustomSchematronRules(schema, Integer.parseInt(metadataId), md, lang, validations);
        } else {
            try {
                // enumerate the metadata xml so that we can report any problems found
                // by the schematron_xml script to the geonetwork editor
                metadataManager.getEditLib().enumerateTree(md);

                // Apply custom schematron rules
                error = applyCustomSchematronRules(schema, Integer.parseInt(metadataId), md, lang, validations);

                // remove editing info added by enumerateTree
                metadataManager.getEditLib().removeEditingInfo(md);

            } catch (Exception e) {
                e.printStackTrace();
                Log.error(Geonet.DATA_MANAGER, "Could not run schematron validation on metadata " + metadataId + ": " + e.getMessage());
            }
        }

        if (error != null) {
            errorReport.addContent(error);
        }

        // Save report in session (invalidate by next update) and db
        try {
            saveValidationStatus(intMetadataId, validations);
        } catch (Exception e) {
            Log.error(Geonet.DATA_MANAGER, "Could not save validation status on metadata " + metadataId + ": " + e.getMessage(), e);
        }

        return Pair.read(errorReport, version);
    }

    /**
     * Creates XML schematron report for each set of rules defined in schema directory. This method assumes that you've run enumerateTree on
     * the metadata
     *
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
     * @param id the metadata record internal identifier
     * @param validations the validation reports for each type of validation and schematron validation
     */
    private void saveValidationStatus(int id, List<MetadataValidation> validations) throws Exception {
        validationRepository.deleteAllById_MetadataId(id);
        validationRepository.save(validations);
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
            if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                Log.debug(Geonet.DATA_MANAGER, "invalid metadata: " + x.getMessage(), x);
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

    public SettingManager getSettingManager() {
        return settingManager;
    }
}

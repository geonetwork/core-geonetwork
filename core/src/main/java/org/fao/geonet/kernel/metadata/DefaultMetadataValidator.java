/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.MetadataValidationId;
import org.fao.geonet.domain.MetadataValidationStatus;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.JeevesException;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.SchematronValidator;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.Xml.ErrorHandler;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public class DefaultMetadataValidator implements IMetadataValidator {

    @Autowired
    private SchematronValidator schematronValidator;

    @Autowired
    private IMetadataSchemaUtils metadataSchemaUtils;

    @Autowired
    private MetadataValidationRepository validationRepository;

    private SchemaManager schemaManager;

    private EditLib editLib;

    /**
     * @see org.fao.geonet.kernel.metadata.IMetadataValidator#init(jeeves.server.context.ServiceContext)
     * @param context
     */
    @Override
    public void init(ServiceContext context) {
        this.schematronValidator = context.getBean(SchematronValidator.class);
        this.metadataSchemaUtils = context.getBean(IMetadataSchemaUtils.class);
        this.validationRepository = context.getBean(MetadataValidationRepository.class);
        this.setSchemaManager(context.getBean(SchemaManager.class));
    }

    /**
     * @param schemaManager
     *            the schemaManager to set
     */
    @Autowired
    public void setSchemaManager(SchemaManager schemaManager) {
        this.schemaManager = schemaManager;
        this.editLib = new EditLib(this.schemaManager);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataValidator#validate(java.lang.String,
     *      org.jdom.Document)
     * @param schema
     * @param doc
     * @throws Exception
     */
    @Override
    public void validate(String schema, Document doc) throws Exception {
        Xml.validate(doc);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataValidator#validate(java.lang.String,
     *      org.jdom.Element)
     * @param schema
     * @param md
     * @throws Exception
     */
    @Override
    public void validate(String schema, Element md) throws Exception {
        String schemaLoc = md.getAttributeValue("schemaLocation",
                Namespaces.XSI);
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER,
                    "Extracted schemaLocation of " + schemaLoc);
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
                Xml.validate(metadataSchemaUtils.getSchemaDir(schema)
                        .resolve(Geonet.File.SCHEMA), md);
            }
        }
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataValidator#validateInfo(java.lang.String,
     *      org.jdom.Element, org.fao.geonet.utils.Xml.ErrorHandler)
     * @param schema
     * @param md
     * @param eh
     * @return
     * @throws Exception
     */
    @Override
    public Element validateInfo(String schema, Element md, ErrorHandler eh)
            throws Exception {
        String schemaLoc = md.getAttributeValue("schemaLocation",
                Namespaces.XSI);
        if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
            Log.debug(Geonet.DATA_MANAGER,
                    "Extracted schemaLocation of " + schemaLoc);
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
                return Xml.validateInfo(metadataSchemaUtils.getSchemaDir(schema)
                        .resolve(Geonet.File.SCHEMA), md, eh);
            }
        }
    }

    /**
     * 
     * @param schema
     * @param xml
     * @param context
     * @throws Exception
     */
    @Override
    public void validateMetadata(String schema, Element xml,
            ServiceContext context) throws Exception {
        validateMetadata(schema, xml, context, " ");
    }

    /**
     * 
     * @param schema
     * @param xml
     * @param context
     * @param fileName
     * @throws Exception
     */
    @Override
    public void validateMetadata(String schema, Element xml,
            ServiceContext context, String fileName) throws Exception {

        DataManager.setNamespacePrefix(xml);
        try {
            validate(schema, xml);
        } catch (XSDValidationErrorEx e) {
            if (!fileName.equals(" ")) {
                throw new XSDValidationErrorEx(
                        e.getMessage() + "(in " + fileName + "): ",
                        e.getObject());
            } else {
                throw new XSDValidationErrorEx(e.getMessage(), e.getObject());
            }
        }

        // --- Now do the schematron validation on this file - if there are
        // errors
        // --- then we say what they are!
        // --- Note we have to use uuid here instead of id because we don't have
        // --- an id...

        Element schemaTronXml = doSchemaTronForEditor(schema, xml,
                context.getLanguage());
        xml.detach();
        if (schemaTronXml != null && schemaTronXml.getContent().size() > 0) {
            Element schemaTronReport = doSchemaTronForEditor(schema, xml,
                    context.getLanguage());

            List<Namespace> theNSs = new ArrayList<Namespace>();
            theNSs.add(Namespace.getNamespace("geonet",
                    "http://www.fao.org/geonetwork"));
            theNSs.add(Namespace.getNamespace("svrl",
                    "http://purl.oclc.org/dsdl/svrl"));

            Element failedAssert = Xml.selectElement(schemaTronReport,
                    "geonet:report/svrl:schematron-output/svrl:failed-assert",
                    theNSs);

            Element failedSchematronVerification = Xml.selectElement(
                    schemaTronReport,
                    "geonet:report/geonet:schematronVerificationError", theNSs);

            if ((failedAssert != null)
                    || (failedSchematronVerification != null)) {
                throw new SchematronValidationErrorEx(
                        "Schematron errors detected for file " + fileName
                                + " - " + Xml.getString(schemaTronReport)
                                + " for more details",
                        schemaTronReport);
            }
        }

    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataValidator#doSchemaTronForEditor(java.lang.String,
     *      org.jdom.Element, java.lang.String)
     * @param schema
     * @param md
     * @param lang
     * @return
     * @throws Exception
     */
    @Override
    public Element doSchemaTronForEditor(String schema, Element md, String lang)
            throws Exception {
        // enumerate the metadata xml so that we can report any problems found
        // by the schematron_xml script to the geonetwork editor
        editLib.enumerateTree(md);

        // get an xml version of the schematron errors and return for error
        // display
        Element schemaTronXmlReport = getSchemaTronXmlReport(schema, md, lang,
                null);

        // remove editing info added by enumerateTree
        editLib.removeEditingInfo(md);

        return schemaTronXmlReport;
    }

    /**
     * Creates XML schematron report for each set of rules defined in schema
     * directory.
     * 
     * @param schema
     * @param md
     * @param lang
     * @param valTypeAndStatus
     * @return
     * @throws Exception
     */
    private Element getSchemaTronXmlReport(String schema, Element md,
            String lang, Map<String, Integer[]> valTypeAndStatus)
                    throws Exception {
        // NOTE: this method assumes that you've run enumerateTree on the
        // metadata

        MetadataSchema metadataSchema = schemaManager.getSchema(schema);
        String[] rules = metadataSchema.getSchematronRules();

        // Schematron report is composed of one or more report(s)
        // for each set of rules.
        Element schemaTronXmlOut = new Element("schematronerrors",
            Edit.NAMESPACE);
        if (rules != null) {
            for (String rule : rules) {
                // -- create a report for current rules.
                // Identified by a rule attribute set to shematron file name
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, " - rule:" + rule);
                String ruleId = rule.substring(0, rule.indexOf(".xsl"));
                Element report = new Element("report", Edit.NAMESPACE);
                report.setAttribute("rule", ruleId,
                    Edit.NAMESPACE);

                java.nio.file.Path schemaTronXmlXslt = metadataSchema.getSchemaDir().resolve("schematron").resolve(rule);
                try {
                    Map<String, Object> params = new HashMap<String, Object>();
                    params.put("lang", lang);
                    params.put("rule", rule);
                    params.put("thesaurusDir", ApplicationContextHolder.get()
                            .getBean(ThesaurusManager.class)
                            .getThesauriDirectory());
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
                        Iterator<?> faileAssertElements = xmlReport.getDescendants(new ElementFilter("failed-assert",
                            Namespaces.SVRL));
                        while (faileAssertElements.hasNext()) {
                            faileAssertElements.next();
                            invalidRules++;
                        }
                        Integer[] results = {invalidRules != 0 ? 0 : 1, firedRules, invalidRules};
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
     * Valid the metadata record against its schema. For each error found, an
     * xsderror attribute is added to the corresponding element trying to find
     * the element based on the xpath return by the ErrorHandler.
     *
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    private synchronized Element getXSDXmlReport(String schema, Element md) {
        // NOTE: this method assumes that enumerateTree has NOT been run on the metadata
        ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setNs(Edit.NAMESPACE);
        Element xsdErrors;

        try {
            xsdErrors = validateInfo(schema,
                md, errorHandler);
        } catch (Exception e) {
            xsdErrors = JeevesException.toElement(e);
            return xsdErrors;
        }

        if (xsdErrors != null) {
            MetadataSchema mds = schemaManager.getSchema(schema);
            List<Namespace> schemaNamespaces = mds.getSchemaNS();

            //-- now get each xpath and evaluate it
            //-- xsderrors/xsderror/{message,xpath}
            @SuppressWarnings("unchecked")
            List<Element> list = xsdErrors.getChildren();
            for (Element elError : list) {
                String xpath = elError.getChildText("xpath", Edit.NAMESPACE);
                String message = elError.getChildText("message", Edit.NAMESPACE);
                message = "\\n" + message;

                //-- get the element from the xpath and add the error message to it
                Element elem = null;
                try {
                    elem = Xml.selectElement(md, xpath, schemaNamespaces);
                } catch (JDOMException je) {
                    je.printStackTrace();
                    Log.error(Geonet.DATA_MANAGER, "Attach xsderror message to xpath " + xpath + " failed: " + je.getMessage());
                }
                if (elem != null) {
                    String existing = elem.getAttributeValue("xsderror", Edit.NAMESPACE);
                    if (existing != null) message = existing + message;
                    elem.setAttribute("xsderror", message, Edit.NAMESPACE);
                } else {
                    Log.warning(Geonet.DATA_MANAGER, "WARNING: evaluating XPath " + xpath + " against metadata failed - XSD validation message: " + message + " will NOT be shown by the editor");
                }
            }
        }
        return xsdErrors;
    }

    /**
     *
     * @see org.fao.geonet.kernel.metadata.IMetadataValidator#doValidate(java.lang.String,
     *      java.lang.String, org.jdom.Document, java.lang.String)
     * @param schema
     * @param metadataId
     * @param doc
     * @param lang
     * @return
     */
    @Override
    public boolean doValidate(String schema, String metadataId, Document doc,
            String lang) {
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
                validations.add(new MetadataValidation().
                    setId(new MetadataValidationId(intMetadataId, "dtd")).
                    setStatus(MetadataValidationStatus.VALID).
                    setRequired(true).
                    setNumTests(1).
                    setNumFailures(0));
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                    Log.debug(Geonet.DATA_MANAGER, "Valid.");
                }
            } catch (Exception e) {
                validations.add(new MetadataValidation().
                    setId(new MetadataValidationId(intMetadataId, "dtd")).
                    setStatus(MetadataValidationStatus.INVALID).
                    setRequired(true).
                    setNumTests(1).
                    setNumFailures(1));

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
                validations.add(new MetadataValidation().
                    setId(new MetadataValidationId(intMetadataId, "xsd")).
                    setStatus(MetadataValidationStatus.INVALID).
                    setRequired(true).
                    setNumTests(xsdErrorCount).
                    setNumFailures(xsdErrorCount));
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Invalid.");
                valid = false;
            } else {
                validations.add(new MetadataValidation().
                    setId(new MetadataValidationId(intMetadataId, "xsd")).
                    setStatus(MetadataValidationStatus.VALID).
                    setRequired(true).
                    setNumTests(1).
                    setNumFailures(0));
                if (Log.isDebugEnabled(Geonet.DATA_MANAGER))
                    Log.debug(Geonet.DATA_MANAGER, "Valid.");
            }
            try {
                editLib.enumerateTree(md);
                //Apply custom schematron rules
                Element errors = applyCustomSchematronRules(schema, Integer.parseInt(metadataId), doc.getRootElement(), lang, validations);
                valid = valid && errors == null;
                editLib.removeEditingInfo(md);
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

    public Pair<Element, String> doValidate(UserSession session, String schema,
            String metadataId, Element md, String lang, boolean forEditing)
                    throws Exception {
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

        //-- get an XSD validation report and add results to the metadata
        //-- as geonet:xsderror attributes on the affected elements
        Element xsdErrors = getXSDXmlReport(schema, md);
        int xsdErrorCount = 0;
        if (xsdErrors != null) {
            xsdErrorCount = xsdErrors.getContent().size();
        }
        if (xsdErrorCount > 0) {
            errorReport.addContent(xsdErrors);
            validations.add(new MetadataValidation().
                setId(new MetadataValidationId(intMetadataId, "xsd")).
                setStatus(MetadataValidationStatus.INVALID).
                setRequired(true).
                setNumTests(xsdErrorCount).
                setNumFailures(xsdErrorCount));

            if (Log.isDebugEnabled(Geonet.DATA_MANAGER)) {
                Log.debug(Geonet.DATA_MANAGER, "  - XSD error: " + Xml.getString(xsdErrors));
            }
        } else {
            validations.add(new MetadataValidation().
                setId(new MetadataValidationId(intMetadataId, "xsd")).
                setStatus(MetadataValidationStatus.VALID).
                setRequired(true).
                setNumTests(1).
                setNumFailures(0));

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
            //-- now expand the elements and add the geonet: elements
            editLib.expandElements(schema, md);
            version = editLib.getVersionForEditing(schema, metadataId, md);

            //Apply custom schematron rules
            error = applyCustomSchematronRules(schema, Integer.parseInt(metadataId), md, lang, validations);
        } else {
            try {
                // enumerate the metadata xml so that we can report any problems found
                // by the schematron_xml script to the geonetwork editor
                editLib.enumerateTree(md);

                //Apply custom schematron rules
                error = applyCustomSchematronRules(schema, Integer.parseInt(metadataId), md, lang, validations);

                // remove editing info added by enumerateTree
                editLib.removeEditingInfo(md);

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

    public Element applyCustomSchematronRules(String schema, int metadataId,
            Element md, String lang, List<MetadataValidation> validations) {

        return schematronValidator.applyCustomSchematronRules(schema,
                metadataId, md, lang, validations);
    }

    /**
     * Saves validation status information into the database for the current
     * record.
     *
     * @param id
     *            the metadata record internal identifier
     * @param validations
     *            the validation reports for each type of validation and
     *            schematron validation
     */
    private void saveValidationStatus(int id,
            List<MetadataValidation> validations) throws Exception {
        validationRepository.deleteAllById_MetadataId(id);
        validationRepository.save(validations);
    }

    /**
     * 
     * @see org.fao.geonet.kernel.metadata.IMetadataValidator#validate(org.jdom.Element)
     * @param xml
     * @return
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
                Log.debug(Geonet.DATA_MANAGER,
                        "invalid metadata: " + x.getMessage(), x);
            return false;
        }
    }
}

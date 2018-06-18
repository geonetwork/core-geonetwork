package org.fao.geonet.kernel.datamanager;

import java.util.List;

import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.Xml.ErrorHandler;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Interface to handle all operations related to validations of records
 * 
 * @author delawen
 *
 */
public interface IMetadataValidator {

    /**
     * This is a hopefully soon to be deprecated initialization function to replace the @Autowired annotation
     * 
     * @param context
     * @param force
     * @throws Exception
     */
    public void init(ServiceContext context, Boolean force) throws Exception;

    /**
     * Validates metadata against XSD and schematron files related to metadata schema throwing XSDValidationErrorEx if xsd errors or
     * SchematronValidationErrorEx if schematron rules fails.
     */
    void validateMetadata(String schema, Element xml, ServiceContext context, String fileName) throws Exception;

    /**
     * 
     * if the metadata has no namespace or already has a namespace then we must skip this phase
     * 
     * @param md
     */
    void setNamespacePrefix(Element md);

    /**
     * Use this validate method for XML documents with dtd.
     */
    void validate(String schema, Document doc) throws Exception;

    /**
     * Use this validate method for XML documents with xsd validation.
     */
    void validate(String schema, Element md) throws Exception;

    /**
     * Validates an xml document with respect to an xml schema described by .xsd file path using supplied error handler.
     *
     * @param schema
     * @param md
     * @param eh
     * @return
     * @throws Exception
     */
    Element validateInfo(String schema, Element md, ErrorHandler eh) throws Exception;

    /**
     * Creates XML schematron report.
     */
    Element doSchemaTronForEditor(String schema, Element md, String lang) throws Exception;

    /**
     * Used by harvesters that need to validate metadata.
     *
     * @param schema name of the schema to validate against
     * @param metadataId metadata id - used to record validation status
     * @param doc metadata document as JDOM Document not JDOM Element
     * @param lang Language from context
     */
    boolean doValidate(String schema, String metadataId, Document doc, String lang);

    /**
     * Used by the validate embedded service. The validation report is stored in the session.
     *
     */
    Pair<Element, String> doValidate(UserSession session, String schema, String metadataId, Element md, String lang, boolean forEditing)
            throws Exception;

    /**
     * Creates XML schematron report for each set of rules defined in schema directory. This method assumes that you've run enumerateTree on
     * the metadata
     *
     * Returns null if no error on validation.
     */
    Element applyCustomSchematronRules(String schema, int metadataId, Element md, String lang, List<MetadataValidation> validations);

    /**
     * Validates an xml document, using autodetectschema to determine how.
     *
     * @return true if metadata is valid
     */
    boolean validate(Element xml);

    /**
     * Adds the namespace to the element
     * 
     * @param md
     * @param ns
     */
    void setNamespacePrefix(Element md, Namespace ns);

    /**
     * Helper function to prevent loop on dependencies
     * 
     * @param metadataManager
     */
    void setMetadataManager(IMetadataManager metadataManager);
}

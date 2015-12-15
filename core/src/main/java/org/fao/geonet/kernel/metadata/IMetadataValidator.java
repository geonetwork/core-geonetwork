/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.util.List;

import org.fao.geonet.domain.MetadataValidation;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.utils.Xml.ErrorHandler;
import org.jdom.Document;
import org.jdom.Element;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Addon to {@link DataManager} to handle metadata actions related to validation
 * 
 * @author delawen
 * 
 * 
 */
public interface IMetadataValidator {
    
    /**
     *FIXME
     * To remove when Spring autowiring works right
     * @param context
     */
    public void init(ServiceContext context);
    
    
    /**
     * Use this validate method for XML documents with dtd.
     *
     * @param schema
     * @param doc
     * @throws Exception
     */
    public void validate(String schema, Document doc) throws Exception;

    /**
     * Use this validate method for XML documents with xsd validation.
     *
     * @param schema
     * @param md
     * @throws Exception
     */
    public void validate(String schema, Element md) throws Exception;

    /**
     * TODO javadoc.
     *
     * @param schema
     * @param md
     * @param eh
     * @return
     * @throws Exception
     */
    public Element validateInfo(String schema, Element md, ErrorHandler eh)
            throws Exception;

    /**
     * Validates metadata against XSD and schematron files related to metadata
     * schema throwing XSDValidationErrorEx if xsd errors or
     * SchematronValidationErrorEx if schematron rules fails.
     *
     * @param schema
     * @param xml
     * @param context
     * @throws Exception
     */
    public void validateMetadata(String schema, Element xml,
            ServiceContext context) throws Exception;

    /**
     * Validates metadata against XSD and schematron files related to metadata
     * schema throwing XSDValidationErrorEx if xsd errors or
     * SchematronValidationErrorEx if schematron rules fails.
     *
     * @param schema
     * @param xml
     * @param context
     * @param fileName
     * @throws Exception
     */
    public void validateMetadata(String schema, Element xml,
            ServiceContext context, String fileName) throws Exception;

    /**
     * Creates XML schematron report.
     * 
     * @param schema
     * @param md
     * @param lang
     * @return
     * @throws Exception
     */
    public Element doSchemaTronForEditor(String schema, Element md, String lang)
            throws Exception;

    /**
     * Used by harvesters that need to validate metadata.
     *
     * @param schema
     *            name of the schema to validate against
     * @param metadataId
     *            metadata id - used to record validation status
     * @param doc
     *            metadata document as JDOM Document not JDOM Element
     * @param lang
     *            Language from context
     * @return
     */
    public boolean doValidate(String schema, String metadataId, Document doc,
            String lang);

    /**
     * Used by the validate embedded service. The validation report is stored in
     * the session.
     *
     * @param session
     * @param schema
     * @param metadataId
     * @param md
     * @param lang
     * @param forEditing
     *            TODO
     * @return
     * @throws Exception
     */
    public Pair<Element, String> doValidate(UserSession session, String schema,
            String metadataId, Element md, String lang, boolean forEditing)
                    throws Exception;

    /**
     *
     * Creates XML schematron report for each set of rules defined in schema
     * directory. This method assumes that you've run enumerateTree on the
     * metadata
     *
     * Returns null if no error on validation.
     */
    public Element applyCustomSchematronRules(String schema, int metadataId,
            Element md, String lang, List<MetadataValidation> validations);

    /**
     * Validates an xml document, using autodetectschema to determine how.
     *
     * @param xml
     * @return true if metadata is valid
     */
    public boolean validate(Element xml);
}

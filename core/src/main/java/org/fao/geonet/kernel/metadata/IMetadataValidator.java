/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.Xml.ErrorHandler;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

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
     *
     * @param name
     * @return
     */
    public Path getSchemaDir(String name);

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
}

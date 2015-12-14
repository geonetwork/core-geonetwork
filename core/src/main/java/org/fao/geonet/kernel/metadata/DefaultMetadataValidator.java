/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Geonet.Namespaces;
import org.fao.geonet.exceptions.SchematronValidationErrorEx;
import org.fao.geonet.exceptions.XSDValidationErrorEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.Xml.ErrorHandler;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.beans.factory.annotation.Autowired;

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
    private SchemaManager schemaManager;

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
                Xml.validate(getSchemaDir(schema).resolve(Geonet.File.SCHEMA),
                        md);
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
                return Xml.validateInfo(
                        getSchemaDir(schema).resolve(Geonet.File.SCHEMA), md,
                        eh);
            }
        }
    }

    /**
     * 
     * @param name
     * @return
     */
    @Override
    public Path getSchemaDir(String name) {
        return schemaManager.getSchemaDir(name);
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
        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);

        DataManager dataMan = gc.getBean(DataManager.class);

        DataManager.setNamespacePrefix(xml);
        try {
            dataMan.validate(schema, xml);
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

        Element schemaTronXml = dataMan.doSchemaTronForEditor(schema, xml,
                context.getLanguage());
        xml.detach();
        if (schemaTronXml != null && schemaTronXml.getContent().size() > 0) {
            Element schemaTronReport = dataMan.doSchemaTronForEditor(schema,
                    xml, context.getLanguage());

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
}

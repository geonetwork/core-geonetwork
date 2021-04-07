package org.fao.geonet.schemaPlugins;

import junit.framework.TestCase;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;


/**
 * Created by francois on 3/24/14.
 */
public abstract class AbstractSchemaTest extends TestCase {
    private final String schemaPath =
            "web/src/main/webapp/WEB-INF/data/config/schema_plugins/";

    private String schemaIdentifier;

    public void setSchemaIdentifier(String schemaIdentifier) {
        this.schemaIdentifier = schemaIdentifier;
    }
    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public Element transform(String xmlFilePath, String xslFilePath) throws Exception {
        Element metadata = Xml.loadFile(
                getSchemaPath() + getSchemaIdentifier() + xmlFilePath);
        return Xml.transform(metadata,
                getSchemaPath() + getSchemaIdentifier() + xslFilePath);
    }
    public Element transform(Element xml, String xskFilePath) throws Exception {
        return Xml.transform(xml,
                getSchemaPath() + getSchemaIdentifier() + xskFilePath);
    }
    public Element transform(String xmlFilePath, String xslFilePath,
                             Map<String, Object> params) throws Exception {
        Element metadata = Xml.loadFile(
                getSchemaPath() + getSchemaIdentifier() + xmlFilePath);
        return null;
//        Xml.transform(metadata,
//                getSchemaPath() + getSchemaIdentifier() + xslFilePath, params);
    }
    public Element transform(Element xml, String xslFilePath,
                             Map<String, Object> params) throws Exception {
        return null;
//        Xml.transform(xml,
//                getSchemaPath() + getSchemaIdentifier() + xslFilePath,
//                params);
    }

    /**
     * Test set and extract UUID from a metadata record
     */
    @Test
    public abstract void testSetAndExtractUUID() throws Exception;

    @Before
    public void setUp () {

    }
}

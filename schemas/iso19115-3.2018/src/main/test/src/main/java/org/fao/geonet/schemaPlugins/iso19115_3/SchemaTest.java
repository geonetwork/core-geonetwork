package org.fao.geonet.schemaPlugins.iso19115_3;

import org.fao.geonet.schemaPlugins.AbstractSchemaTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by francois on 3/24/14.
 */
public class SchemaTest extends AbstractSchemaTest {
    public SchemaTest() {
        setSchemaIdentifier("iso19115-3");
    }

    @Test
    public void testSetAndExtractUUID() throws Exception {
        String metadatauuid = "123456";
        Element metadata = Xml.loadFile(getSchemaPath() + getSchemaIdentifier() +
                "/test/resources/metadata.xml");
        Element root = new Element("root");
        Element env = new Element("env");
        Element uuid = new Element("uuid").setText(metadatauuid);
        env.addContent(uuid);
        root.addContent(env);
        root.addContent(metadata);

        Element results = transform(root, "/set-uuid.xsl");

        assertEquals("Root node is the metadata record root MD_Metadata.",
                results.getName(), "MD_Metadata");
        assertNotSame("Root node is not the root node from the input document.",
                results.getName(), "root");
        assertNull("Env node is removed from metadata record.",
                results.getChild("env"));
        assertEquals("Metadata UUID is updated.",
                results.getChild("metadataIdentifier", Namespaces.MDB)
                        .getChild("MD_Identifier", Namespaces.MCC)
                        .getChild("code", Namespaces.MCC)
                        .getChildText("CharacterString", Namespaces.GCO)
                , metadatauuid);

        Element results2 = transform(results, "/extract-uuid.xsl");
        assertEquals("Root is uuid..", results2.getName(), "uuid");
        assertEquals("Metadata UUID is extracted properly..",
                results2.getText(), metadatauuid);
    }

    @Test
    public void testExtractDateModified() throws Exception {
        Element results = transform(
                "/test/resources/metadata.xml",
                "/extract-date-modified.xsl");
        String date = "2005-03-31T19:17:30";

        assertNotNull ("Extracted date is not null.", results);
        assertEquals("Extracted date is in dateStamp element.",
                results.getName(), "dateStamp");
        assertEquals("Extracted date is " + date + ".",
                results.getValue(), date);
    }

    @Test
    public void testExtractGML() throws Exception {
        Element results = transform(
                "/test/resources/metadata.xml",
                "/extract-gml.xsl");

        assertNotNull ("Extracted geometry is not null.", results);
        assertEquals("Extracted geometry is a gml:GeometryCollection.",
                results.getName(), "GeometryCollection");
    }

    @Test
    public void testSetAndRemoveParentMetadata() throws Exception {
        Element original = Xml.loadFile(getSchemaPath() + getSchemaIdentifier() +
                "/test/resources/metadata.xml");

        assertNull ("Original does not contains parent.",
                original.getChild("parentMetadata", Namespaces.MDB));


        String parentMetadataUuid = "ParentMetadataUuid";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("parentUuid", parentMetadataUuid);
        Element results = transform(
                original,
                "/process/parent-add.xsl",
                params);

        assertEquals("After adding parent, records contains parent UUID.",
                results.getChild("parentMetadata", Namespaces.MDB)
                        .getAttributeValue("uuidref"),
                parentMetadataUuid);


        Element resultsAfterRemove = transform(
                results,
                "/process/parent-remove.xsl",
                params);

        assertNull("After removing parent, records does not contains parent UUID.",
                resultsAfterRemove.getChild("parentMetadata", Namespaces.MDB));

    }
    @Test
    public void testXslConversionISO19139toISO19115_3() throws Exception {
        Element results = transform("/test/resources/metadata-iso19139.xml",
                "/convert/19115to19115-1.xsl");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("setParentIdentifierWithUUIDAttribute", false);
        Element resultsWithOptions = transform("/test/resources/metadata-iso19139.xml",
                "/convert/19115to19115-1.xsl", params);

        assertNotNull ("Output document is not null.", results);
        // TODO: Could be nice to use XMLUnit to compare 2 documents


        // Uuid
        assertEquals("Metadata UUID is preserved.",
                results.getChild("metadataIdentifier", Namespaces.MDB)
                        .getChild("MD_Identifier", Namespaces.MCC)
                        .getChild("code", Namespaces.MCC)
                        .getChildText("CharacterString", Namespaces.GCO)
                , "da165110-88fd-11da-a88f-000d939bc5d8");


        // Parent identifier
        assertEquals("Metadata parent identifier is migrated to attribute uuidref.",
                results.getChild("parentMetadata", Namespaces.MDB)
                        .getAttributeValue("uuidref")
                , "theParentIdentifier");
        assertEquals("Metadata parent identifier is migrated to the citation title " +
                "if setParentIdentifierWithUUIDAttribute is false.",
                resultsWithOptions.getChild("parentMetadata", Namespaces.MDB)
                        .getChild("CI_Citation", Namespaces.CIT)
                        .getChild("title", Namespaces.CIT)
                        .getChildText("CharacterString", Namespaces.GCO)
                , "theParentIdentifier");
    }
}

package org.fao.geonet.kernel;

import static org.fao.geonet.Assert.*;
import static org.fao.geonet.constants.Geonet.Namespaces.*;

import com.google.common.collect.Lists;
import jeeves.server.ServiceConfig;
import junit.framework.TestCase;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SchemaLoader;
import org.jdom.Element;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Created by Jesse on 12/6/13.
 */
public class EditLibTest {

    @Rule
    public TemporaryFolder  _schemaCatalogContainer = new TemporaryFolder();
    @Test
    public void testAddElementOrFragmentFromXpaths() throws Exception {
        final GeonetworkDataDirectory gdd = new GeonetworkDataDirectory();

        final ServiceConfig serviceConfig = new ServiceConfig(Lists.<Element>newArrayList());
        gdd.init("geonetwork", AbstractCoreIntegrationTest.getWebappDir(), serviceConfig, null);

        String iso19139SchemaDir = gdd.getSchemaPluginsDir() + "/iso19139";
        String xmlSchemaFile = iso19139SchemaDir+"/schema.xsd";
        String xmlSubstitutionsFile = iso19139SchemaDir+"/schema-substitutes.xml";
        MetadataSchema schema = new SchemaLoader().load(xmlSchemaFile, xmlSubstitutionsFile);

        SchemaManager manager = new SchemaManager();
        final String resourcePath = gdd.getResourcesDir().getAbsolutePath();
        final String basePath = gdd.getWebappDir();
        final String schemaPluginsCat = _schemaCatalogContainer.getRoot() + Geonet.File.SCHEMA_PLUGINS_CATALOG;
        final String schemaPluginsDir = gdd.getSchemaPluginsDir().getAbsolutePath();
        manager.configure(basePath, resourcePath, schemaPluginsCat, schemaPluginsDir, "eng","iso19139", false);


        final Element metadataElement = new Element("MD_Metadata");
        String newValue = "newValue";
        final String xpathProperty = "gmd:fileIdentifier/gco:CharacterString";
        new EditLib(manager).addElementOrFragmentFromXpath(metadataElement, schema, xpathProperty, newValue, true);

        assertEqualsText(newValue, metadataElement, xpathProperty, GMD, GCO);
    }

    @Test
    public void testAddElementOrFragmentFromXpath() throws Exception {

    }
}

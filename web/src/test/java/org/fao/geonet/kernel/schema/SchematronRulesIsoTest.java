package org.fao.geonet.kernel.schema;

import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.fao.geonet.kernel.schema.AbstractInspireTest.INSPIRE_VALID_ISO19139_XML;
import static org.junit.Assert.assertEquals;

/**
 * Test some of the rules in the schematron-rules-iso.sch files in the iso19139 schema plugin.
 *
 * Created by Jesse on 3/25/14.
 */
public class SchematronRulesIsoTest extends AbstractSchematronTest {
    @Autowired
    private SchemaManager schemaManager;

    protected File schematronXsl;
    protected Element schematron;
    private Map<String, Object> params;
    @Before
    public void before() throws IOException, JDOMException {
        String schematronFile = "schematron/schematron-rules-iso";
        schematron = Xml.loadFile(new File(schemaManager.getSchemaDir("iso19139"), schematronFile+".sch"));
        schematronXsl = new File(schemaManager.getSchemaDir("iso19139"), schematronFile+".xsl");
        this.params = getParams("schematron-rules-inspire.disabled");
    }

    protected File getSchematronXsl() {
        return schematronXsl;
    }

    @Test
    public void testPasses() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        Element results = Xml.transform(testMetadata, getSchematronXsl().getPath(), params);
        assertEquals(0, countFailures(results));
    }

    @Test
    public void testMissingAbstract() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element charStringEl = testMetadata.getChild("identificationInfo", GMD)
                .getChild("MD_DataIdentification", GMD)
                .getChild("abstract", GMD);
        testEmptyStringErrors(testMetadata, charStringEl);
    }

    @Test
    public void testMissingTitle() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element charStringEl = testMetadata.getChild("identificationInfo", GMD)
                .getChild("MD_DataIdentification", GMD)
                .getChild("citation", GMD)
                .getChild("CI_Citation", GMD)
                .getChild("title", GMD);
        testEmptyStringErrors(testMetadata, charStringEl);
    }

    @Test
    public void testMissingCitation() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element citationEl = testMetadata.getChild("identificationInfo", GMD)
                .getChild("MD_DataIdentification", GMD)
                .getChild("citation", GMD)
                .getChild("CI_Citation", GMD);

        citationEl.detach();

        Element results = Xml.transform(testMetadata, getSchematronXsl().getPath(), params);
        assertEquals(1, countFailures(results));

        testMetadata.getChild("identificationInfo", GMD)
                .getChild("MD_DataIdentification", GMD)
                .getChild("citation", GMD).setAttribute("nilReason","missing", GCO);

        results = Xml.transform(testMetadata, getSchematronXsl().getPath(), params);
        assertEquals(0, countFailures(results));

    }

    @Test
    public void testMissingContact() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element contact = testMetadata.getChild("contact", GMD);
        testNoStringErrors(testMetadata, contact);
    }

    @Test
    public void testMissingLanguage() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element contact = testMetadata.getChild("language", GMD);
        testNoStringErrors(testMetadata, contact);
    }

    @Test
    public void testMissingDataIdentificationLanguage() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element element = testMetadata.getChild("identificationInfo", GMD)
                .getChild("MD_DataIdentification", GMD)
                .getChild("language", GMD);
        testNoStringErrors(testMetadata, element);
    }

    @Test
    public void testMissingDataIdentificationCitationDate() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element dateEl = Xml.selectElement(testMetadata, "gmd:identificationInfo/*/gmd:citation/*/gmd:date/*/gmd:date",
                Arrays.asList(GMD, GCO));
        testNoStringErrors(testMetadata, dateEl);
    }

    @Test
    public void testMissingDataIdentificationCitationDateType() throws Exception {
        final Element testMetadata = Xml.loadStream(SchematronRulesIsoTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        final Element dateTypeEl = Xml.selectElement(testMetadata, "gmd:identificationInfo/*/gmd:citation/*/gmd:date/*/gmd:dateType",
                Arrays.asList(GMD, GCO));
        testNoStringErrors(testMetadata, dateTypeEl);
    }

    private void testNoStringErrors(Element testMetadata, Element contact) throws Exception {
        contact.setContent(Collections.emptyList());

        Element results = Xml.transform(testMetadata, getSchematronXsl().getPath(), params);
        assertEquals(1, countFailures(results));

        contact.setAttribute("nilReason", "missing", GCO);
        results = Xml.transform(testMetadata, getSchematronXsl().getPath(), params);
        assertEquals(0, countFailures(results));
    }

    private void testEmptyStringErrors(Element testMetadata, Element charStringEl) throws Exception {
        charStringEl.setContent(Arrays.asList());

        Element results = Xml.transform(testMetadata, getSchematronXsl().getPath(), params);
        assertEquals(1, countFailures(results));

        charStringEl.addContent(new Element("CharacterString", GCO).setText(""));

        results = Xml.transform(testMetadata, getSchematronXsl().getPath(), params);
        assertEquals(1, countFailures(results));

        charStringEl.addContent(
                new Element("PT_FreeText", GMD).addContent(
                        new Element("textGroup", GMD).addContent(
                                new Element("LocalisedCharacterString", GMD).setAttribute("locale", "#DE").setText(" ")
                        )));


        results = Xml.transform(testMetadata, getSchematronXsl().getPath(), params);
        assertEquals(1, countFailures(results));

        charStringEl.setAttribute("nilReason", "missing", GCO);
        results = Xml.transform(testMetadata, getSchematronXsl().getPath(), params);
        assertEquals(1, countFailures(results));

        charStringEl.setContent(Arrays.asList());
        results = Xml.transform(testMetadata, getSchematronXsl().getPath(), params);
        assertEquals(0, countFailures(results));

    }
}

package org.fao.geonet.kernel.schema;

import com.google.common.collect.Lists;
import jeeves.utils.Xml;
import org.apache.commons.io.FileUtils;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.*;

/**
 * Test the inspire schematron.
 *
 * Created by Jesse on 1/31/14.
 */
public class TestInspireCompleteness extends AbstractSchematronTest {
    protected final static File SCHEMATRON_XSL;
    protected final static Element ISO_19139_INSPIRE_SCHEMATRON;
    static {
        String schematronFile = "iso19139/schematron/schematron-rules-inspire.sch.disabled";
        Pair<Element,File> compiledResult = compileSchematron(new File(SCHEMA_PLUGINS, schematronFile));
        ISO_19139_INSPIRE_SCHEMATRON = compiledResult.one();
        SCHEMATRON_XSL = compiledResult.two();
    }

    @Test
    public void testValid() throws Exception {
        final Element validMetadata = Xml.loadStream(TestInspireCompleteness.class.getResourceAsStream("inspire-valid-iso19139.xml"));

        Element results = Xml.transform(validMetadata, SCHEMATRON_XSL.getPath(), PARAMS);
        assertEquals(0, countFailures(results));
    }

    @Test
    public void testMissingUseConstraint() throws Exception {
        final Element testMetadata = Xml.loadStream(TestInspireCompleteness.class.getResourceAsStream("inspire-valid-iso19139.xml"));

        final String xpath = "*//gmd:resourceConstraints//gmd:useLimitation";
        List<Content> useLimitation = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : useLimitation) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, SCHEMATRON_XSL.getPath(), PARAMS);
        assertEquals(1, countFailures(results));

        Element failure = (Element) results.getDescendants(FAILURE_FILTER).next();

        assertTrue(failure.getAttributeValue("test").contains("useLimitation"));
    }

    @Test
    public void testMissingAccessConstraint() throws Exception {
        final Element testMetadata = Xml.loadStream(TestInspireCompleteness.class.getResourceAsStream("inspire-valid-iso19139.xml"));

        final String xpath = "*//gmd:resourceConstraints//gmd:accessConstraints";
        List<Content> useLimitation = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : useLimitation) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, SCHEMATRON_XSL.getPath(), PARAMS);
        assertEquals(1, countFailures(results));

        Element failure = (Element) results.getDescendants(FAILURE_FILTER).next();

        assertTrue(failure.getAttributeValue("test"), failure.getAttributeValue("test").contains("accessConstraints"));
    }
    @Test
    public void testMissingDegreeOfConformity() throws Exception {
        final Element testMetadata = Xml.loadStream(TestInspireCompleteness.class.getResourceAsStream("inspire-valid-iso19139.xml"));

        final String xpath = "*//gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:pass";
        List<Content> pass = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : pass) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, SCHEMATRON_XSL.getPath(), PARAMS);
        assertEquals(1, countFailures(results));

        Element failure = (Element) results.getDescendants(FAILURE_FILTER).next();

        assertTrue(failure.getAttributeValue("test"), failure.getAttributeValue("test").contains("degree"));
    }
    @Test
    public void testMissingConformityTitle() throws Exception {
        final Element testMetadata = Xml.loadStream(TestInspireCompleteness.class.getResourceAsStream("inspire-valid-iso19139.xml"));

        final String xpath = "*//gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:title";
        List<Content> pass = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : pass) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, SCHEMATRON_XSL.getPath(), PARAMS);
        assertEquals(1, countFailures(results));
    }

    @Test
    public void testMissingConformityDate() throws Exception {
        final Element testMetadata = Xml.loadStream(TestInspireCompleteness.class.getResourceAsStream("inspire-valid-iso19139.xml"));

        final String xpath = "*//gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:date";
        List<Content> pass = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : pass) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, SCHEMATRON_XSL.getPath(), PARAMS);
        assertEquals(1, countFailures(results));
    }

    @Test
    public void testConformityEngLanguage_GerConformityString() throws Exception {
        final Element testMetadata = Xml.loadStream(TestInspireCompleteness.class.getResourceAsStream("inspire-valid-iso19139.xml"));

        testMetadata.getChild("language", GMD).getChild("CharacterString", GCO).setText("eng");

        Element results = Xml.transform(testMetadata, SCHEMATRON_XSL.getPath(), PARAMS);
        assertEquals(1, countFailures(results));

        Element failure = (Element) results.getDescendants(FAILURE_FILTER).next();

        assertTrue(failure.getAttributeValue("test"), failure.getAttributeValue("test").contains("conformity"));
    }

}

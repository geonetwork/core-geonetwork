package org.fao.geonet.kernel.schema;

import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the inspire schematron.
 *
 * Created by Jesse on 1/31/14.
 */
public class BasicInspireTest extends AbstractInspireTest {
    protected Path schematronXsl;
    protected Element inspire_schematron;

    @Before
    public void before() {
        super.before();
        Pair<Element,Path> compiledResult = compileSchematron(getSchematronFile("iso19139", "schematron-rules-inspire.disabled.sch"));
        inspire_schematron = compiledResult.one();
        schematronXsl = compiledResult.two();
    }

    protected Path getSchematronXsl() {
        return schematronXsl;
    }

    @Test
    public void testMissingUseConstraint() throws Exception {
        final Element testMetadata = Xml.loadStream(AbstractInspireTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));

        final String xpath = "*//gmd:resourceConstraints//gmd:useLimitation";
        List<Content> useLimitation = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : useLimitation) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(1, countFailures(results));

        Element failure = (Element) results.getDescendants(FAILURE_FILTER).next();

        assertTrue(failure.getAttributeValue("test").contains("useLimitation"));
    }

    @Test
    public void testMissingAccessConstraint() throws Exception {
        final Element testMetadata = Xml.loadStream(AbstractInspireTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));

        final String xpath = "*//gmd:resourceConstraints//gmd:accessConstraints";
        List<Content> useLimitation = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : useLimitation) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(1, countFailures(results));

        Element failure = (Element) results.getDescendants(FAILURE_FILTER).next();

        assertTrue(failure.getAttributeValue("test"), failure.getAttributeValue("test").contains("accessConstraints"));
    }

    @Test
    public void testMissingDegreeOfConformity() throws Exception {
        final Element testMetadata = Xml.loadStream(AbstractInspireTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));

        final String xpath = "gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:pass";

        @SuppressWarnings("unchecked")
        List<Content> pass = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : pass) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(1, countFailures(results));

        Element failure = (Element) results.getDescendants(FAILURE_FILTER).next();

        assertTrue(failure.getAttributeValue("test"), failure.getAttributeValue("test").contains("degree"));
    }

}

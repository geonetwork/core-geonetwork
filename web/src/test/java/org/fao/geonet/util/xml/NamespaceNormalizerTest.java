package org.fao.geonet.util.xml;

import jeeves.utils.Xml;
import org.fao.geonet.test.TestCase;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.util.Map;

/**
 *
 * Unit test for normalizing namespaces across XML documents.
 *
 * @author heikki doeleman
 *
 */
public class NamespaceNormalizerTest extends TestCase {

    private XMLOutputter outputter = new XMLOutputter(Format.getRawFormat());

    public NamespaceNormalizerTest(String name) throws Exception {
		super(name);
	}

    public void testCreateNormalizedNamespaceMap() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root xmlns:a=\"http://aaa\"><a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        String xml2 = "<?xml version=\"1.0\"?><root xmlns:b=\"http://aaa\"><b:a/></root>";
        Element xmle2 = Xml.loadString(xml2, false);
        NormalizedNamespaceMap normalizedNamespaceMap = NamespaceNormalizer.createNormalizedNamespaceMapOverMultipleDocuments(xmle, xmle2);

        assertEquals("Unexpected number of mappings", 2 , normalizedNamespaceMap.size());
        assertEquals("Unexpected uri", "http://aaa", normalizedNamespaceMap.values().iterator().next().getURI());
        assertContains("Unexpected prefix", "xxx1", normalizedNamespaceMap.normalizedPrefixes());
        assertContains("Unexpected prefix", "xxx2", normalizedNamespaceMap.normalizedPrefixes());
    }


    public void testCreateNormalizedNamespaceMap2() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root xmlns:a=\"http://aaa\" xmlns:b=\"http://bbb\"><a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        String xml2 = "<?xml version=\"1.0\"?><root xmlns:b=\"http://aaa\" xmlns:c=\"http://ccc\"><b:a/></root>";
        Element xmle2 = Xml.loadString(xml2, false);
        NormalizedNamespaceMap normalizedNamespaceMap = NamespaceNormalizer.createNormalizedNamespaceMapOverMultipleDocuments(xmle, xmle2);

        assertEquals("Unexpected number of mappings", 4, normalizedNamespaceMap.size());

    }

    public void testCreateNormalizedNamespaceMap3() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root xmlns=\"http://default\" xmlns:a=\"http://aaa\" xmlns:b=\"http://bbb\"><a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        String xml2 = "<?xml version=\"1.0\"?><root xmlns:b=\"http://aaa\" xmlns:c=\"http://ccc\"><b:a/></root>";
        Element xmle2 = Xml.loadString(xml2, false);
        NormalizedNamespaceMap normalizedNamespaceMap = NamespaceNormalizer.createNormalizedNamespaceMapOverMultipleDocuments(xmle, xmle2);

        assertEquals("Unexpected number of prefix uri mappings", 5, normalizedNamespaceMap.size());

    }

    public void testCreateURI2PrefixMap() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root xmlns=\"http://default\" xmlns:a=\"http://aaa\" xmlns:b=\"http://bbb\"><a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        Map<String, String> prefixMap = NamespaceNormalizer.createURI2PrefixMap(xmle);

        assertEquals("Unexpected number of prefix uri mappings", 3, prefixMap.size());

    }

    public void testNormalizeNoNamespace() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root/>";
        Element xmle = Xml.loadString(xml, false);
        NamespaceNormalizer.normalize(xmle);

        assertEquals("Unexpected result", "<root />", outputter.outputString(xmle));
    }

    public void testNormalizeNSPrefixes() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root xmlns:a=\"http://aaa\" xmlns:b=\"http://bbb\"><a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        NamespaceNormalizer.normalize(xmle);

        assertEquals("Unexpected result", "<root xmlns:a=\"http://aaa\" xmlns:b=\"http://bbb\" xmlns:xxx1=\"http://aaa\" xmlns:xxx2=\"http://bbb\"><xxx1:a /></root>",
                outputter.outputString(xmle));
    }

    public void testNormalizeNSPrefixes2() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root xmlns:a=\"http://aaa\"><a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        String xml2 = "<?xml version=\"1.0\"?><root xmlns:b=\"http://aaa\"><b:a/></root>";
        Element xmle2 = Xml.loadString(xml2, false);
        NamespaceNormalizer.normalize(xmle, xmle2);

        assertEquals("Unexpected difference in normalized docs", "<root xmlns:a=\"http://aaa\" xmlns:xxx1=\"http://aaa\"><xxx1:a /></root>",
                outputter.outputString(xmle));
        assertEquals("Unexpected difference in normalized docs", "<root xmlns:b=\"http://aaa\" xmlns:xxx1=\"http://aaa\"><xxx1:a /></root>",
                outputter.outputString(xmle2));
    }

    public void testNormalizeNSPrefixes3() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root xmlns=\"http://default\" xmlns:a=\"http://aaa\"><a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        NamespaceNormalizer.normalize(xmle);

        assertEquals("Unexpected result", "<xxx1:root xmlns:xxx1=\"http://default\" xmlns:a=\"http://aaa\" xmlns=\"http://default\" xmlns:xxx2=\"http://aaa\"><xxx2:a /></xxx1:root>",
                outputter.outputString(xmle));
    }

    public void testNormalizeNSPrefixes4() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root xmlns=\"http://default1\" xmlns:a=\"http://aaa\"><a:a/><c/></root>";
        Element xmle = Xml.loadString(xml, false);
        String xml2 = "<?xml version=\"1.0\"?><root xmlns=\"http://default2\" xmlns:b=\"http://aaa\"><b:a/><c/></root>";
        Element xmle2 = Xml.loadString(xml2, false);
        NamespaceNormalizer.normalize(xmle, xmle2);

        assertEquals("Unexpected result", "<xxx1:root xmlns:xxx1=\"http://default1\" xmlns:a=\"http://aaa\" xmlns=\"http://default1\" xmlns:xxx2=\"http://aaa\"><xxx2:a /><xxx1:c /></xxx1:root>",
                outputter.outputString(xmle));
        assertEquals("Unexpected result", "<xxx3:root xmlns:xxx3=\"http://default2\" xmlns:b=\"http://aaa\" xmlns=\"http://default2\" xmlns:xxx2=\"http://aaa\"><xxx2:a /><xxx3:c /></xxx3:root>",
                outputter.outputString(xmle2));
    }

    public void testNormalizeNSPrefixes5() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root xmlns=\"http://default\" xmlns:a=\"http://aaa\"><a:a/><b:b xmlns:b=\"http://bbb\"/></root>";
        Element xmle = Xml.loadString(xml, false);
        NamespaceNormalizer.normalize(xmle);


        assertEquals("Unexpected result", "<xxx1:root xmlns:xxx1=\"http://default\" xmlns:a=\"http://aaa\" xmlns=\"http://default\" xmlns:xxx2=\"http://aaa\"><xxx2:a /><xxx3:b xmlns:xxx3=\"http://bbb\" xmlns:b=\"http://bbb\" /></xxx1:root>",
                outputter.outputString(xmle));
    }

    public void testNormalizeNSPrefixes6() throws Exception {
        String xml = "<aaa:root xmlns:aaa=\"http://zzzzzzz\"><aaa:a/></aaa:root>";
        Element xmle = Xml.loadString(xml, false);
        NamespaceNormalizer.normalize(xmle);

        assertEquals("Unexpected result", "<xxx1:root xmlns:xxx1=\"http://zzzzzzz\" xmlns:aaa=\"http://zzzzzzz\"><xxx1:a /></xxx1:root>",
                outputter.outputString(xmle));
    }

    public void testNormalizeNSPrefixes7() throws Exception {
        String xml = "<aaa:root xmlns:aaa=\"http://zzzzzzz\" xmlns:bbb=\"http://yyy\"><aaa:a bbb:nilReason=\"missing\"/></aaa:root>";
        Element xmle = Xml.loadString(xml, false);
        NamespaceNormalizer.normalize(xmle);

        assertEquals("Unexpected result", "<xxx1:root xmlns:xxx1=\"http://zzzzzzz\" xmlns:bbb=\"http://yyy\" xmlns:aaa=\"http://zzzzzzz\" xmlns:xxx2=\"http://yyy\"><xxx1:a xxx2:nilReason=\"missing\" /></xxx1:root>",
                outputter.outputString(xmle));
    }

    public void testNormalizeNoInput() throws Exception {
        NamespaceNormalizer.normalize();
        // no exception, so it's OK
    }

}
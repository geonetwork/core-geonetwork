package org.fao.geonet.util.xml;

import jeeves.utils.Xml;
import org.fao.geonet.test.TestCase;
import org.jdom.Element;
import org.jdom.Namespace;

import java.util.List;

/**
 *
 * Unit test for NamespaceUtils.
 *
 * @author heikki doeleman
 *
 */
public class NamespaceUtilsTest extends TestCase {

    public NamespaceUtilsTest(String name) throws Exception {
		super(name);
	}

    public void testNamespaceInScope() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root " +
                "xmlns:a=\"http://aaa\" " +
                "xmlns:b=\"http://bbb\" " +
                "xmlns:c=\"http://ccc\" " +
                "xmlns:d=\"http://ddd\">" +
                "<a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        List<Namespace> inScope = NamespaceUtils.getNamespacesInScope(xmle);

        assertContains("Expected inscope namespace " + Namespace.NO_NAMESPACE , Namespace.NO_NAMESPACE, inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("a", "http://aaa") , Namespace.getNamespace("a", "http://aaa"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("b", "http://bbb"), Namespace.getNamespace("b", "http://bbb"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("c", "http://ccc"), Namespace.getNamespace("c", "http://ccc"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("d", "http://ddd"), Namespace.getNamespace("d", "http://ddd"), inScope);

        Element a = xmle.getChild("a", Namespace.getNamespace("a", "http://aaa"));
        inScope = NamespaceUtils.getNamespacesInScope(a);

        assertContains("Expected inscope namespace " + Namespace.NO_NAMESPACE , Namespace.NO_NAMESPACE, inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("a", "http://aaa") , Namespace.getNamespace("a", "http://aaa"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("b", "http://bbb"), Namespace.getNamespace("b", "http://bbb"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("c", "http://ccc"), Namespace.getNamespace("c", "http://ccc"), inScope);
        assertContains("Expected inscope namespace " + Namespace.getNamespace("d", "http://ddd"), Namespace.getNamespace("d", "http://ddd"), inScope);
    }

    public void testNamespaceInherited() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root " +
                "xmlns:a=\"http://aaa\" " +
                "xmlns:b=\"http://bbb\" " +
                "xmlns:c=\"http://ccc\" " +
                "xmlns:d=\"http://ddd\">" +
                "<a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        List<Namespace> inHerited = NamespaceUtils.getNamespacesInherited(xmle);

        assertContains("Expected inherited namespace " + Namespace.NO_NAMESPACE , Namespace.NO_NAMESPACE, inHerited);
        assertContains("Expected inherited namespace " + Namespace.XML_NAMESPACE , Namespace.XML_NAMESPACE, inHerited);

        Element a = xmle.getChild("a", Namespace.getNamespace("a", "http://aaa"));
        inHerited = NamespaceUtils.getNamespacesInherited(a);

        assertContains("Expected inherited namespace " + Namespace.NO_NAMESPACE , Namespace.NO_NAMESPACE, inHerited);
        assertContains("Expected inherited namespace " + Namespace.XML_NAMESPACE , Namespace.XML_NAMESPACE, inHerited);
    }

    public void testNamespaceIntroduced() throws Exception {
        String xml = "<?xml version=\"1.0\"?><root " +
                "xmlns:a=\"http://aaa\" " +
                "xmlns:b=\"http://bbb\" " +
                "xmlns:c=\"http://ccc\" " +
                "xmlns:d=\"http://ddd\">" +
                "<a:a/></root>";
        Element xmle = Xml.loadString(xml, false);
        List<Namespace> introduced = NamespaceUtils.getNamespacesIntroduced(xmle);

        assertContains("Expected introduced namespace " + Namespace.getNamespace("a", "http://aaa") , Namespace.getNamespace("a", "http://aaa"), introduced);
        assertContains("Expected introduced namespace " + Namespace.getNamespace("b", "http://bbb"), Namespace.getNamespace("b", "http://bbb"), introduced);
        assertContains("Expected introduced namespace " + Namespace.getNamespace("c", "http://ccc"), Namespace.getNamespace("c", "http://ccc"), introduced);
        assertContains("Expected introduced namespace " + Namespace.getNamespace("d", "http://ddd"), Namespace.getNamespace("d", "http://ddd"), introduced);

        Element a = xmle.getChild("a", Namespace.getNamespace("a", "http://aaa"));
        introduced = NamespaceUtils.getNamespacesIntroduced(a);

        assertEquals("Expected 0 introduced namespaces", 0, introduced.size());

    }
}
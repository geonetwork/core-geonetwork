package org.fao.geonet.kernel.schema;

import com.google.common.io.Files;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test the inspire schematron.
 * <p/>
 * Created by Jesse on 1/31/14.
 */
public class TestIso19139SchematronDefinition extends AbstractSchematronTest {
    @Autowired
    private SchemaManager schemaManager;


    @SuppressWarnings("unchecked")
    @Test
    public void testAllSchematrons() throws Exception {
        final String schemaDir = schemaManager.getSchemaDir("iso19139");
        for (File schematronXsl : new File(schemaDir, "schematron").listFiles()) {
            if (!schematronXsl.getName().endsWith(".xsl")) {
                continue;
            }

            final Element validMetadata = loadValidMetadata();

            String schematronName = Files.getNameWithoutExtension(schematronXsl.getName());
            Element results = Xml.transform(validMetadata, schematronXsl.getPath(), getParams(schematronName));
            assertEquals(schematronName, 0, countFailures(results));

            Element schematronDefinition = Xml.loadFile(new File(schematronXsl.getParentFile(), schematronName + ".sch"));
            final List<Element> declaredPattern = schematronDefinition.getChildren("pattern", SCH_NAMESPACE);
            final List<Element> xsltPattern = (List<Element>) Xml.selectNodes(results, "svrl:active-pattern", NAMESPACES);
            assertEquals(declaredPattern.size(), xsltPattern.size());

            for (Element pattern : declaredPattern) {
                checkPattern(schematronXsl, validMetadata, pattern);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkPattern(File file, Element validMetadata, Element pattern) throws JDOMException {
        final List<Element> declaredRules = pattern.getChildren("rule", SCH_NAMESPACE);

        Map<Element, List<Element>> xml = new IdentityHashMap<Element, List<Element>>();

        for (Element declaredRule : declaredRules) {
            String context = declaredRule.getAttributeValue("context");

            final XPath xPath = XPath.newInstance(context);
            for (Namespace namespace : NAMESPACES) {
                xPath.addNamespace(namespace);
            }


            List<Element> nodes = (List<Element>) xPath.selectNodes(validMetadata);
            String titleOfRule = createRuleTitle(declaredRule);

            for (Map.Entry<Element, List<Element>> previouslyLoadedNodes : xml.entrySet()) {
                for (Element currentlyLoadedNode : nodes) {
                    if (previouslyLoadedNodes.getValue().contains(currentlyLoadedNode)) {
                        throwDuplicateContextRule(file, titleOfRule, previouslyLoadedNodes);
                    }
                }
            }

            xml.put(declaredRule, nodes);
        }
    }

    private void throwDuplicateContextRule(File file, String titleOfRule, Map.Entry<Element, List<Element>> previouslyLoadedNodes) {
        final String ruleTitle = createRuleTitle(previouslyLoadedNodes.getKey());

        String errorDescription = "A problem was found with the rules in the schematron :'" +
                                  file.getPath().substring(file.getParent().length() + 1) + "' \n\n";

        String fixExplanation =
                "Each rule in a pattern must select different nodes in the metadata because,\n" +
                "due to the nature of the generated XSLT, only one of the rules will be \n" +
                "executed.  Therefore the context attribute of each rule must be unique.  \n" +
                "There are two ways to  fix this problem.  \n" +
                "        1. Put the assertions and reports of the two rules into the same rule\n" +
                "        2. Put the rules in separate patterns\n\n";

        throw new AssertionError(errorDescription + titleOfRule + "\n\n\t selects one of the same nodes as" +
                                 " another or the rules in the same " +
                                 "pattern: \n\n\t" + ruleTitle + "\n\n" + fixExplanation);
    }

    private String createRuleTitle(Element declaredRule) {
        final Element parentElement = declaredRule.getParentElement();
        String patternTitle = parentElement.getChildText("title", SCH_NAMESPACE);

        if (patternTitle == null || patternTitle.isEmpty()) {
            patternTitle = "<Pattern Missing Title>";
        }
        int index = parentElement.getChildren().indexOf(declaredRule);

        return patternTitle + ":[" + index + "]" + declaredRule.getAttributeValue("context");

    }

    private Element loadValidMetadata() throws IOException, JDOMException {
        return Xml.loadStream(TestIso19139SchematronDefinition.class.getResourceAsStream("inspire-valid-iso19139.xml"));
    }

}

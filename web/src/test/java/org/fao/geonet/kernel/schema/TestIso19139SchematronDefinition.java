/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.kernel.schema;

import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Test the inspire schematron.
 * <p/>
 * Created by Jesse on 1/31/14.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(inheritLocations = true, locations = {"classpath*:http-request-factory-context.xml"})
public class TestIso19139SchematronDefinition extends AbstractSchematronTest {
    @Autowired
    protected ConfigurableApplicationContext _applicationContext;

    @Before
    public void setApplicationContextInApplicationHolder() {
        ApplicationContextHolder.set(_applicationContext);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAllSchematrons() throws Exception {
        try (DirectoryStream<Path> schematronFiles = Files.newDirectoryStream(getSchematronDir("iso19139"), "*.sch")) {
            for (Path schematronFile : schematronFiles) {
                final Pair<Element, Path> compiledSchematron = compileSchematron(schematronFile);
                Element schematronDefinition = compiledSchematron.one();
                Path schematronXsl = compiledSchematron.two();

                final Element validMetadata = loadValidMetadata();

                Element results = Xml.transform(validMetadata, schematronXsl, params);
                assertEquals(schematronFile.getFileName().toString(), 0, countFailures(results));

                final List<Element> declaredPattern = schematronDefinition.getChildren("pattern", SCH_NAMESPACE);
                final List<Element> xsltPattern = (List<Element>) Xml.selectNodes(results, "svrl:active-pattern", NAMESPACES);
                assertEquals(declaredPattern.size(), xsltPattern.size());

                for (Element pattern : declaredPattern) {
                    checkPattern(schematronFile, validMetadata, pattern);
                }

            }
        }
    }

    @SuppressWarnings("unchecked")
    private void checkPattern(Path file, Element validMetadata, Element pattern) throws JDOMException {
        final List<Element> declaredRules = pattern.getChildren("rule", SCH_NAMESPACE);

        Map<Element, List<Element>> xml = new IdentityHashMap<>();

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

    private void throwDuplicateContextRule(Path file, String titleOfRule, Map.Entry<Element, List<Element>> previouslyLoadedNodes) {
        final String ruleTitle = createRuleTitle(previouslyLoadedNodes.getKey());

        String errorDescription = "A problem was found with the rules in the schematron :'" +
            file.getFileName() + "' \n\n";

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

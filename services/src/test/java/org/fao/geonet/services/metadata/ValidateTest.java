package org.fao.geonet.services.metadata;

import static org.junit.Assert.*;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.junit.Test;

import java.util.Iterator;
import java.util.List;

/**
 * Test methods of Validate.
 *
 * Created by Jesse on 3/14/14.
 */
@SuppressWarnings("unchecked")
public class ValidateTest {
    @Test
    public void testRestructureReport() throws Exception {
        final Element report = Xml.loadString(REPORT, false);
        Validate.restructureReportToHavePatternRuleHierarchy(report);

        final Iterator patternsIter = report.getDescendants(new ElementFilter(Validate.EL_ACTIVE_PATTERN, Geonet.Namespaces.SVRL));
        while (patternsIter.hasNext()) {
            Object next = patternsIter.next();

            assertPattern((Element) next);
        }
    }

    private void assertPattern(Element patternUnderTest) {
        assertTrue(patternUnderTest.getChildren().size() > 0);
        List<Element> children = patternUnderTest.getParentElement().getChildren();

        for (Element pattern : children) {
            assertFalse(pattern.getName().equals(Validate.EL_FIRED_RULE));
            assertFalse(pattern.getName().equals(Validate.EL_FAILED_ASSERT));
            assertFalse(pattern.getName().equals(Validate.EL_SUCCESS_REPORT));
        }

        final List<Element> rules = patternUnderTest.getChildren();

        for (Element rule : rules) {
            assertEquals(Validate.EL_FIRED_RULE, rule.getName());
            assertFalse(rule.getAttributeValue(Validate.ATT_CONTEXT).equals(Validate.DEFAULT_CONTEXT));
            List<Element> ruleChildren = rule.getChildren();
            for (Element ruleChild : ruleChildren) {
                final String name = ruleChild.getName();
                assertTrue(name,
                        name.equals(Validate.EL_FAILED_ASSERT) ||
                        name.equals(Validate.EL_SUCCESS_REPORT));
            }
        }
    }

    private static final String REPORT = "<geonet:report xmlns:geonet=\"http://www.fao.org/geonetwork\" geonet:id=\"210\">\n"
                                         + "  <geonet:xsderrors>\n"
                                         + "    <geonet:error>\n"
                                         + "      <geonet:typeOfError>ERROR</geonet:typeOfError>\n"
                                         + "      <geonet:errorNumber>1</geonet:errorNumber>\n"
                                         + "      <geonet:message>cvc-complex-type.2.4.b: The content of element 'gmd:MD_Metadata' is " +
                                         "not complete. One of '{\"http://www.isotc211.org/2005/gmd\":characterSet, " +
                                         "\"http://www.isotc211.org/2005/gmd\":parentIdentifier, " +
                                         "\"http://www.isotc211.org/2005/gmd\":hierarchyLevel, " +
                                         "\"http://www.isotc211.org/2005/gmd\":hierarchyLevelName, " +
                                         "\"http://www.isotc211.org/2005/gmd\":contact}' is expected. (Element: gmd:MD_Metadata with " +
                                         "parent element: Unknown)</geonet:message>\n"
                                         + "      <geonet:xpath>.</geonet:xpath>\n"
                                         + "    </geonet:error>\n"
                                         + "  </geonet:xsderrors>\n"
                                         + "  <geonet:schematronerrors>\n"
                                         + "    <geonet:report geonet:rule=\"schematron-rules-geonetwork\" geonet:displayPriority=\"1\"" +
                                         " geonet:dbident=\"100\" geonet:required=\"REQUIRED\">\n"
                                         + "      <svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\" " +
                                         "title=\"Schematron validation / GeoNetwork recommendations\" schemaVersion=\"\">\n"
                                         + "        <!--   \n"
                                         + "\t\t   \n"
                                         + "\t\t   \n"
                                         + "\t\t -->\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.opengis.net/gml\" " +
                                         "prefix=\"gml\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/gmd\" " +
                                         "prefix=\"gmd\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/srv\" " +
                                         "prefix=\"srv\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/gco\" " +
                                         "prefix=\"gco\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.fao.org/geonetwork\" " +
                                         "prefix=\"geonet\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.w3.org/1999/xlink\" " +
                                         "prefix=\"xlink\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:fired-rule context=\"//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']\" " +
                                         "/>\n"
                                         + "        <svrl:successful-report ref=\"#_1\" test=\"$localeAndNoLanguage\" " +
                                         "location=\"/*[local-name()='MD_Metadata']\">\n"
                                         + "          <svrl:text>\"\"</svrl:text>\n"
                                         + "        </svrl:successful-report>\n"
                                         + "        <svrl:successful-report ref=\"#_1\" test=\"$duplicateLanguage\" " +
                                         "location=\"/*[local-name()='MD_Metadata']\">\n"
                                         + "          <svrl:text />\n"
                                         + "        </svrl:successful-report>\n"
                                         + "      </svrl:schematron-output>\n"
                                         + "    </geonet:report>\n"
                                         + "    <geonet:report geonet:rule=\"schematron-rules-inspire-strict\" " +
                                         "geonet:displayPriority=\"2\" geonet:dbident=\"102\" geonet:required=\"REQUIRED\">\n"
                                         + "      <svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\" " +
                                         "xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gco=\"http://www.isotc211.org/2005/gco\"" +
                                         " xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                                         "xmlns:saxon=\"http://saxon.sf.net/\" xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" " +
                                         "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:skos=\"http://www.w3" +
                                         ".org/2004/02/skos/core#\" xmlns:schold=\"http://www.ascc.net/xml/schematron\" " +
                                         "xmlns:gml=\"http://www.opengis.net/gml\" xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\" " +
                                         "xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" +
                                         " title=\"INSPIRE Strict Validation rules\" schemaVersion=\"\">\n"
                                         + "        <!--   \n"
                                         + "\t\t   \n"
                                         + "\t\t   \n"
                                         + "\t\t -->\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.opengis.net/gml\" " +
                                         "prefix=\"gml\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/gmd\" " +
                                         "prefix=\"gmd\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/gmx\" " +
                                         "prefix=\"gmx\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/srv\" " +
                                         "prefix=\"srv\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/gco\" " +
                                         "prefix=\"gco\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.fao.org/geonetwork\" " +
                                         "prefix=\"geonet\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.w3.org/2004/02/skos/core#\" " +
                                         "prefix=\"skos\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.w3.org/1999/xlink\" " +
                                         "prefix=\"xlink\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "      </svrl:schematron-output>\n"
                                         + "    </geonet:report>\n"
                                         + "    <geonet:report geonet:rule=\"schematron-rules-inspire\" geonet:displayPriority=\"3\" " +
                                         "geonet:dbident=\"104\" geonet:required=\"REQUIRED\">\n"
                                         + "      <svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\" " +
                                         "xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gco=\"http://www.isotc211.org/2005/gco\"" +
                                         " xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                                         "xmlns:saxon=\"http://saxon.sf.net/\" xmlns:gmx=\"http://www.isotc211.org/2005/gmx\" " +
                                         "xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:skos=\"http://www.w3" +
                                         ".org/2004/02/skos/core#\" xmlns:schold=\"http://www.ascc.net/xml/schematron\" " +
                                         "xmlns:gml=\"http://www.opengis.net/gml\" xmlns:iso=\"http://purl.oclc.org/dsdl/schematron\" " +
                                         "xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" +
                                         " title=\"INSPIRE metadata implementing rule validation\" schemaVersion=\"\">\n"
                                         + "        <!--   \n"
                                         + "\t\t   \n"
                                         + "\t\t   \n"
                                         + "\t\t -->\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.opengis.net/gml\" " +
                                         "prefix=\"gml\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/gmd\" " +
                                         "prefix=\"gmd\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/gmx\" " +
                                         "prefix=\"gmx\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/srv\" " +
                                         "prefix=\"srv\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/gco\" " +
                                         "prefix=\"gco\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.fao.org/geonetwork\" " +
                                         "prefix=\"geonet\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.w3.org/2004/02/skos/core#\" " +
                                         "prefix=\"skos\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.w3.org/1999/xlink\" " +
                                         "prefix=\"xlink\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:fired-rule context=\"//gmd:MD_Metadata\" />\n"
                                         + "        <svrl:failed-assert ref=\"#_1\" test=\"$resourceType_present\" " +
                                         "location=\"/*:MD_Metadata[namespace-uri()='http://www.isotc211.org/2005/gmd'][1]\">\n"
                                         + "          <svrl:text />\n"
                                         + "        </svrl:failed-assert>\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:fired-rule context=\"/gmd:MD_Metadata\" />\n"
                                         + "        <svrl:failed-assert ref=\"#_1\" test=\"$degree\" " +
                                         "location=\"/*:MD_Metadata[namespace-uri()='http://www.isotc211.org/2005/gmd'][1]\">\n"
                                         + "          <svrl:text>false</svrl:text>\n"
                                         + "        </svrl:failed-assert>\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:fired-rule context=\"//gmd:MD_Metadata\" />\n"
                                         + "        <svrl:failed-assert ref=\"#_1\" test=\"$dateStamp\" " +
                                         "location=\"/*:MD_Metadata[namespace-uri()='http://www.isotc211.org/2005/gmd'][1]\">\n"
                                         + "          <svrl:text />\n"
                                         + "        </svrl:failed-assert>\n"
                                         + "        <svrl:failed-assert ref=\"#_1\" test=\"$language_present\" " +
                                         "location=\"/*:MD_Metadata[namespace-uri()='http://www.isotc211.org/2005/gmd'][1]\">\n"
                                         + "          <svrl:text />\n"
                                         + "        </svrl:failed-assert>\n"
                                         + "        <svrl:failed-assert ref=\"#_1\" test=\"not($missing)\" " +
                                         "location=\"/*:MD_Metadata[namespace-uri()='http://www.isotc211.org/2005/gmd'][1]\">\n"
                                         + "          <svrl:text />\n"
                                         + "        </svrl:failed-assert>\n"
                                         + "      </svrl:schematron-output>\n"
                                         + "    </geonet:report>\n"
                                         + "    <geonet:report geonet:rule=\"schematron-rules-iso\" geonet:displayPriority=\"4\" " +
                                         "geonet:dbident=\"106\" geonet:required=\"REQUIRED\">\n"
                                         + "      <svrl:schematron-output xmlns:svrl=\"http://purl.oclc.org/dsdl/svrl\" " +
                                         "title=\"Schematron validation for ISO&#xA;&#x9;&#x9;19115(19139)\" schemaVersion=\"\">\n"
                                         + "        <!--   \n"
                                         + "\t\t   \n"
                                         + "\t\t   \n"
                                         + "\t\t -->\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.opengis.net/gml\" " +
                                         "prefix=\"gml\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/gmd\" " +
                                         "prefix=\"gmd\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/srv\" " +
                                         "prefix=\"srv\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.isotc211.org/2005/gco\" " +
                                         "prefix=\"gco\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.fao.org/geonetwork\" " +
                                         "prefix=\"geonet\" />\n"
                                         + "        <svrl:ns-prefix-in-attribute-values uri=\"http://www.w3.org/1999/xlink\" " +
                                         "prefix=\"xlink\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:fired-rule context=\"*[gco:CharacterString]\" />\n"
                                         + "        <svrl:fired-rule context=\"*[gco:CharacterString]\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:fired-rule context=\"//gmd:MD_Metadata|//*[@gco:isoType='gmd:MD_Metadata']\" " +
                                         "/>\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "        <svrl:active-pattern document=\"\" name=\"\" />\n"
                                         + "      </svrl:schematron-output>\n"
                                         + "    </geonet:report>\n"
                                         + "  </geonet:schematronerrors>\n"
                                         + "</geonet:report>";
}

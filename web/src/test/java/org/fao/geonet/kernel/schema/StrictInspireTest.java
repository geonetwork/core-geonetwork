package org.fao.geonet.kernel.schema;

import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the inspire schematron.
 *
 * Created by Jesse on 1/31/14.
 */
public class StrictInspireTest extends AbstractInspireTest {
    protected Path schematronXsl;
    protected Element inspire_schematron;

    private static final Map<String, String> CONFORMITY_STRING = new HashMap<String, String>();
    static {
        CONFORMITY_STRING.put("ger", "verordnung (eg) nr. 1089/2010 der kommission vom 23. november 2010 zur durchführung der richtlinie 2007/2/eg des europäischen parlaments und des rates hinsichtlich der interoperabilität von geodatensätzen und -diensten");
        CONFORMITY_STRING.put("eng", "commission regulation (eu) no 1089/2010 of 23 november 2010 implementing directive 2007/2/ec of the european parliament and of the council as regards interoperability of spatial data sets and services");
        CONFORMITY_STRING.put("fre", "règlement (ue) n o 1089/2010 de la commission du 23 novembre 2010 portant modalités d'application de la directive 2007/2/ce du parlement européen et du conseil en ce qui concerne l'interopérabilité des séries et des services de données géographiques");
        CONFORMITY_STRING.put("ita", "regolamento (ue) n . 1089/2010 della commissione del 23 novembre 2010 recante attuazione della direttiva 2007/2/ce del parlamento europeo e del consiglio per quanto riguarda l'interoperabilità dei set di dati territoriali e dei servizi di dati territoriali");
        CONFORMITY_STRING.put("spa", "reglamento (ue) n o 1089/2010 de la comisión de 23 de noviembre de 2010 por el que se aplica la directiva 2007/2/ce del parlamento europeo y del consejo en lo que se refiere a la interoperabilidad de los conjuntos y los servicios de datos espaciales");
        CONFORMITY_STRING.put("fin", "komission asetus (eu) n:o 1089/2010, annettu 23 päivänä marraskuuta 2010 , euroopan parlamentin ja neuvoston direktiivin 2007/2/ey täytäntöönpanosta paikkatietoaineistojen ja -palvelujen yhteentoimivuuden osalta");
        CONFORMITY_STRING.put("dut", "verordening (eu) n r. 1089/2010 van de commissie van 23 november 2010 ter uitvoering van richtlijn 2007/2/eg van het europees parlement en de raad betreffende de interoperabiliteit van verzamelingen ruimtelijke gegevens en van diensten met betrekking tot ruimtelijke gegevens");
    }

    @Before
    public void before() {
        super.before();
        Pair<Element,Path> compiledResult = compileSchematron(getSchematronFile("iso19139", "schematron-rules-inspire-strict.disabled.sch"));
        inspire_schematron = compiledResult.one();
        schematronXsl = compiledResult.two();
    }

    protected Path getSchematronXsl() {
        return schematronXsl;
    }

    @Test
    public void testConformityString() throws Exception {
        final Element testMetadata = Xml.loadStream(AbstractInspireTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));
        new EditLib(null).enumerateTree(testMetadata);
        Map<String, String> languageMap = new HashMap<String, String>();
        languageMap.put("De", "ger");
        languageMap.put("En", "eng");
        languageMap.put("Fr", "fre");
        languageMap.put("It", "ita");
        languageMap.put("Es", "spa");
        languageMap.put("Fi", "fin");
        languageMap.put("Nl", "dut");


        final String invalidText = "invalid";
        for (Map.Entry<String, String> lang : languageMap.entrySet()) {
            final String expectedTitle = CONFORMITY_STRING.get(lang.getValue());

            final Element title = Xml.selectElement(testMetadata,
                    "gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/*/gmd:title", Arrays.asList(GMD, GCO));
            title.setContent(new Element("CharacterString", GCO).setText(invalidText));
            testMetadata.getChild("language", GMD).getChild("CharacterString", GCO).setText(lang.getValue());

            checkFailure(testMetadata, lang, invalidText, expectedTitle);

            title.setContent(new Element("PT_FreeText", GMD).addContent(
                    new Element("textGroup", GMD).addContent(
                            new Element("LocalisedCharacterString", GMD).
                                    setAttribute("locale", "#" + lang.getKey().toUpperCase()).
                                    setText(invalidText)
                    )
            ));

            checkFailure(testMetadata, lang, invalidText, expectedTitle);

            title.setContent(new Element("PT_FreeText", GMD).addContent(
                    new Element("textGroup", GMD).addContent(
                            new Element("LocalisedCharacterString", GMD).
                                    setAttribute("locale", "#XY").
                                    setText(expectedTitle)
                    )
            ));

            Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
            assertEquals(Xml.getString(results), 2, countFailures(results));

            title.setContent(new Element("CharacterString", GCO).setText(expectedTitle));
            results = Xml.transform(testMetadata, getSchematronXsl(), params);
            assertEquals(0, countFailures(results));

            title.setContent(new Element("PT_FreeText", GMD).addContent(
                    new Element("textGroup", GMD).addContent(
                            new Element("LocalisedCharacterString", GMD).
                                    setAttribute("locale", "#"+lang.getKey().toUpperCase()).
                                    setText(expectedTitle)
                    )
            ));
            results = Xml.transform(testMetadata, getSchematronXsl(), params);
            assertEquals(0, countFailures(results));
        }
    }

    private void checkFailure(Element testMetadata, Map.Entry<String, String> lang, String invalidText, String expectedTitle) throws
            Exception {
        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);

        assertEquals(Xml.getString(results), 1, countFailures(results));

        Element failure = (Element) results.getDescendants(FAILURE_FILTER).next();

        assertTrue(failure.getAttributeValue("test"), failure.getAttributeValue("test").contains("$has"+lang.getKey()+"Title"));

        final List<?> failureMessageTextElements = Xml.selectNodes(failure, "*//text()");
        final Text expectedTextFromFailure = (Text) failureMessageTextElements.get(0);

        assertTrue(expectedTextFromFailure.getText().contains("'"+expectedTitle+"'"));

        final Text actualTextFromFailure = (Text) failureMessageTextElements.get(0);
        assertTrue(actualTextFromFailure.getText().contains("'"+invalidText+"'"));
    }


    @Test
    public void testMissingConformityDate() throws Exception {
        final Element testMetadata = Xml.loadStream(AbstractInspireTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));

        final String xpath = "gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/*/gmd:date";
        List<Content> pass = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : pass) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(1, countFailures(results));
    }

    @Test
    public void testMissingConformityTitle() throws Exception {
        final Element testMetadata = Xml.loadStream(AbstractInspireTest.class.getResourceAsStream(INSPIRE_VALID_ISO19139_XML));

        final String xpath = "gmd:dataQualityInfo/*/gmd:report/*/gmd:result/*/gmd:specification/*/gmd:title";
        List<Content> pass = (List<Content>) Xml.selectNodes(testMetadata, xpath, NAMESPACES);

        for (Content content : pass) {
            content.detach();
        }

        Element results = Xml.transform(testMetadata, getSchematronXsl(), params);
        assertEquals(2, countFailures(results));
    }
}

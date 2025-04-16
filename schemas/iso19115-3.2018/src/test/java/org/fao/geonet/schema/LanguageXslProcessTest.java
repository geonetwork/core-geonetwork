package org.fao.geonet.schema;

import org.fao.geonet.schema.iso19115_3_2018.ISO19115_3_2018SchemaPlugin;
import org.fao.geonet.schemas.XslProcessTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.xmlunit.matchers.EvaluateXPathMatcher.hasXPath;

/**
 * Created by francois on 3/24/14.
 */
public class LanguageXslProcessTest extends XslProcessTest {

    public LanguageXslProcessTest() {
        super();
        this.setXslFilename("process/languages-refactor.xsl");
        this.setXmlFilename("metadata.xml");
        this.setNs(ISO19115_3_2018SchemaPlugin.allNamespaces);
    }

    private static final String XPATH_MAIN_LANGUAGE =
        "/mdb:MD_Metadata/mdb:defaultLocale/*/lan:language/*/@codeListValue";
    private static final String XPATH_OTHER_LANGUAGE =
        "/mdb:MD_Metadata/mdb:otherLocale[%d]/*/lan:language/*/@codeListValue";
    private static final String XPATH_COUNT_ELEMENTS =
        "count(//*)";
    private static final String XPATH_COUNT_LOCALISED =
        "count(//lan:LocalisedCharacterString)";
    private static final String XPATH_MAIN_TITLE =
        "/mdb:MD_Metadata/mdb:identificationInfo/*/mri:citation/*/cit:title/gco:CharacterString";
    private static final String XPATH_TITLE_TRANSLATION =
        "/mdb:MD_Metadata/mdb:identificationInfo/*/mri:citation/*/cit:title/*/lan:textGroup[%d]/lan:LocalisedCharacterString";

    @Test
    public void testSetDefaultLanguage() throws Exception {
        Element inputElement = Xml.loadFile(xmlFile);
        String resultString = Xml.getString(inputElement);
        check(resultString, "eng", new String[]{}, 193, 0);
//        TODO: We need to update Mockito for static method probably
//        XslUtil xslUtil = Mockito.mock(XslUtil.class);
//        Mockito.when(xslUtil.twoCharLangCode("eng")).thenReturn("en");
//        Mockito.when(xslUtil.twoCharLangCode("fre")).thenReturn("fr");

        Map<String, Object> params = new HashMap<>(1);
        params.put("defaultLanguage", "fre");
        Element resultElement = Xml.transform(inputElement, xslFile, params);
        resultString = Xml.getString(resultElement);
        check(resultString, "fre", new String[]{}, 193, 0);
    }

    @Test
    public void testLanguageOnMultilingualRecord() throws Exception {
        Element inputElement = Xml.loadFile(testClass.getClassLoader().getResource("metadata-multilingual.xml"));
        String resultString = Xml.getString(inputElement);
        check(resultString, "eng", new String[]{"fre", "ger", "chi", "spa", "ara", "rus"}, 224, 9);

        Map<String, Object> params = new HashMap<>(1);
        params.put("defaultLanguage", "fre");
        Element resultElement = Xml.transform(inputElement, xslFile, params);
        resultString = Xml.getString(resultElement);
        check(resultString, "fre", new String[]{}, 224, 9);
        assertThat(
            resultString,
            hasXPath(XPATH_MAIN_TITLE, equalTo("Modèle pour les données vecteur (multilingue)"))
                .withNamespaceContext(ns)
        );
        assertThat(
            resultString,
            hasXPath(String.format(XPATH_TITLE_TRANSLATION, 2), equalTo("Template for Vector data (multilingual)"))
                .withNamespaceContext(ns)
        );
        assertThat(
            resultString,
            hasXPath(String.format(XPATH_TITLE_TRANSLATION, 1), equalTo("Modèle pour les données vecteur (multilingue)"))
                .withNamespaceContext(ns)
        );
    }

    @Test
    public void testRemovingOtherLanguages() throws Exception {
        Element inputElement = Xml.loadFile(testClass.getClassLoader().getResource("metadata-multilingual.xml"));
        String resultString = Xml.getString(inputElement);

        Map<String, Object> params = new HashMap<>(1);
        params.put("others", "none");
        Element resultElement = Xml.transform(inputElement, xslFile, params);
        resultString = Xml.getString(resultElement);

        check(resultString, "eng", new String[]{}, 167, 0);
    }

    @Test
    public void testChangeDefaultAndRemovingOtherLanguages() throws Exception {
        Element inputElement = Xml.loadFile(testClass.getClassLoader().getResource("metadata-multilingual.xml"));

        Map<String, Object> params = new HashMap<>(1);
        params.put("defaultLanguage", "fre");
        params.put("others", "none");
        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        check(resultString, "fre", new String[]{}, 167, 0);
        assertThat(
            resultString,
            hasXPath(XPATH_MAIN_TITLE, equalTo("Modèle pour les données vecteur (multilingue)"))
                .withNamespaceContext(ns)
        );
    }

    @Test
    public void testChangeDefaultAndRemovingOtherLanguagesMovingPreviousDefaultTranslations() throws Exception {
        Element inputElement = Xml.loadFile(testClass.getClassLoader().getResource("metadata-multilingual.xml"));

        Map<String, Object> params = new HashMap<>(1);
        params.put("defaultLanguage", "ara");
        params.put("others", "none");
        params.put("copyPreviousDefaultIfEmpty", "true");
        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        check(resultString, "ara", new String[]{}, 167, 0);
        assertThat(
            resultString,
            hasXPath(XPATH_MAIN_TITLE, equalTo("Template for Vector data (multilingual)"))
                .withNamespaceContext(ns)
        );
    }

    @Test
    public void testChangeDefaultAndRemovingSomeOtherLanguages() throws Exception {
        Element inputElement = Xml.loadFile(testClass.getClassLoader().getResource("metadata-multilingual.xml"));

        Map<String, Object> params = new HashMap<>(1);
        params.put("defaultLanguage", "fre");
        params.put("others", "rus");
        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        check(resultString, "fre", new String[]{}, 180, 2);
        assertThat(
            resultString,
            hasXPath(XPATH_MAIN_TITLE, equalTo("Modèle pour les données vecteur (multilingue)"))
                .withNamespaceContext(ns)
        );
        assertThat(
            resultString,
            hasXPath(String.format(XPATH_TITLE_TRANSLATION, 2), equalTo("Шаблон для векторных данных (многоязычное)"))
                .withNamespaceContext(ns)
        );
    }


    @Test
    public void testChangeOtherLanguagesOrder() throws Exception {
        Element inputElement = Xml.loadFile(testClass.getClassLoader().getResource("metadata-multilingual.xml"));

        Map<String, Object> params = new HashMap<>(1);
        params.put("others", "rus,fre,spa,ger,chi,ara");
        Element resultElement = Xml.transform(inputElement, xslFile, params);
        String resultString = Xml.getString(resultElement);

        check(resultString, "eng", new String[]{}, 224, 9);
        assertThat(
            resultString,
            hasXPath(XPATH_MAIN_TITLE, equalTo("Template for Vector data (multilingual)"))
                .withNamespaceContext(ns)
        );
        assertThat(
            resultString,
            hasXPath(String.format(XPATH_TITLE_TRANSLATION, 3), equalTo("Modèle pour les données vecteur (multilingue)"))
                .withNamespaceContext(ns)
        );
    }

    private void check(String resultString,
                       String mainLanguage, String[] otherLanguages,
                       int totalElements, int localisedElement) {
        assertThat(
            resultString,
            hasXPath(XPATH_MAIN_LANGUAGE, equalTo(mainLanguage))
                .withNamespaceContext(ns)
        );

        final int[] i = {1};
        Arrays.stream(otherLanguages).forEach(l -> {
            assertThat(
                resultString,
                hasXPath(String.format(XPATH_OTHER_LANGUAGE, i[0]++),
                    equalTo(l))
                    .withNamespaceContext(ns)
            );
        });

        assertThat(
            resultString,
            hasXPath(XPATH_COUNT_ELEMENTS, equalTo(totalElements + ""))
                .withNamespaceContext(ns)
        );

        assertThat(
            resultString,
            hasXPath(XPATH_COUNT_LOCALISED, equalTo(localisedElement + ""))
                .withNamespaceContext(ns)
        );
    }
}

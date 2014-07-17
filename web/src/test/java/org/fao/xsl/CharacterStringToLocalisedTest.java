package org.fao.xsl;

import com.google.common.collect.Lists;
import jeeves.utils.Xml;
import jeeves.xlink.XLink;
import org.fao.geonet.kernel.AbstractThesaurusBasedTest;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.util.XslUtil;
import org.fao.xsl.support.Attribute;
import org.fao.xsl.support.Count;
import org.fao.xsl.support.EqualAttribute;
import org.fao.xsl.support.EqualText;
import org.fao.xsl.support.Finder;
import org.fao.xsl.support.Requirement;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.actors.threadpool.Arrays;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.fao.geonet.constants.Geonet.Namespaces.GCO;
import static org.fao.geonet.constants.Geonet.Namespaces.GMD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CharacterStringToLocalisedTest {

    @BeforeClass
    public static void setup() throws Exception {
        Field field = IsoLanguagesMapper.class.getDeclaredField("instance");
        field.setAccessible(true);

        IsoLanguagesMapper instance = AbstractThesaurusBasedTest.isoLangMapper;
        field.set(null, instance);
    }
    
    @Test
    public void convertLinkageLocalisedURLWithoutURLGroup() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp
                + "/xsl/characterstring-to-localisedcharacterstring.xsl";

        Element testData = Xml
                .loadString(
                        "<che:CHE_MD_Metadata xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:geonet=\"http://www.fao.org/geonetwork\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gco:isoType=\"gmd:MD_Metadata\"> "
                                + "<gmd:language xmlns:xalan=\"http://xml.apache.org/xalan\"> <gco:CharacterString>fra</gco:CharacterString> </gmd:language> "
                                + "<gmd:linkage xsi:type=\"che:PT_FreeURL_PropertyType\"> "
                                + "<che:LocalisedURL>http://www.mapage.ch</che:LocalisedURL> "
                                + "<che:PT_FreeURL> <che:URLGroup> <che:LocalisedURL locale=\"#DE\">http://www.meineseite.ch</che:LocalisedURL> </che:URLGroup> </che:PT_FreeURL> "
                                + "</gmd:linkage> " + "</che:CHE_MD_Metadata>", false);

        Element transformed = Xml.transform(testData, pathToXsl);
        findAndAssert(transformed, new Count(1, new Finder("linkage/PT_FreeURL")));
        findAndAssert(transformed, new Count(0, new Finder("linkage/LocalisedURL")));
        findAndAssert(transformed, new Count(2, new Finder("LocalisedURL")));
        findAndAssert(transformed, new Count(1, new Attribute("LocalisedURL", "locale", "#DE")));
        findAndAssert(transformed, new Count(1, new Attribute("LocalisedURL", "locale", "#FR")));
    }

    @Test
    public void removePTFreeTextFromOnlineResource() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp + "/xsl/characterstring-to-localisedcharacterstring.xsl";

        final String xml = "<che:CHE_MD_Metadata xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:srv=\"http://www.isotc211" +
                            ".org/2005/srv\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211" +
                            ".org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:geonet=\"http://www.fao.org/geonetwork\" " +
                            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gco:isoType=\"gmd:MD_Metadata\"> "
                            + "<gmd:language xmlns:xalan=\"http://xml.apache.org/xalan\"> " +
                            "<gco:CharacterString>fra</gco:CharacterString> </gmd:language> "
                            + "<gmd:CI_OnlineResource>\n"
                            + "  <gmd:name xsi:type=\"gmd:PT_FreeText_PropertyType\" gco:nilReason=\"missing\">\n"
                            + "    <gco:CharacterString/>\n"
                            + "    <gmd:PT_FreeText>\n"
                            + "      <gmd:textGroup>\n"
                            + "        <gmd:LocalisedCharacterString locale=\"#%s\">SITN - Kartenserver des Kantons " +
                            "Neuenburg</gmd:LocalisedCharacterString>\n"
                            + "      </gmd:textGroup>\n"
                            + "    </gmd:PT_FreeText>\n"
                            + "  </gmd:name>\n"
                            + "</gmd:CI_OnlineResource>\n" + "</che:CHE_MD_Metadata>";
        Element testData = Xml.loadString(String.format(xml, "DE"), false);

        Element transformed = Xml.transform(testData, pathToXsl);
        findAndAssert(transformed, new Count(1, new Finder("CI_OnlineResource/name/CharacterString")));
        findAndAssert(transformed, new Count(0, new Finder("CI_OnlineResource/name/PT_FreeText/textGroup/LocalisedCharacterString")));

        testData = Xml.loadString(String.format(xml, "FR"), false);

        transformed = Xml.transform(testData, pathToXsl);
        findAndAssert(transformed, new Count(1, new Finder("CI_OnlineResource/name/CharacterString")));
        findAndAssert(transformed, new Count(0, new Finder("CI_OnlineResource/name/PT_FreeText/textGroup/LocalisedCharacterString")));

        testData = Xml.loadString("<che:CHE_MD_Metadata xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:srv=\"http://www.isotc211" +
                                  ".org/2005/srv\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211" +
                                  ".org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:geonet=\"http://www.fao.org/geonetwork\" " +
                                  "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gco:isoType=\"gmd:MD_Metadata\"> "
                                  + "<gmd:language xmlns:xalan=\"http://xml.apache.org/xalan\"> " +
                                  "<gco:CharacterString>fra</gco:CharacterString> </gmd:language> "
                                  + "<gmd:CI_OnlineResource>\n"
                                  + "  <gmd:name xsi:type=\"gmd:PT_FreeText_PropertyType\" gco:nilReason=\"missing\">\n"
                                  + "    <gco:CharacterString>SITN - Kartenserver des Kantons Neuenburg</gco:CharacterString>\n"
                                  + "  </gmd:name>\n"
                                  + "</gmd:CI_OnlineResource>\n" + "</che:CHE_MD_Metadata>", false);

        transformed = Xml.transform(testData, pathToXsl);
        findAndAssert(transformed, new Count(1, new Finder("CI_OnlineResource/name/CharacterString")));
        findAndAssert(transformed, new Count(0, new Finder("CI_OnlineResource/name/PT_FreeText/textGroup/LocalisedCharacterString")));

        testData = Xml.loadString("<che:CHE_MD_Metadata xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:srv=\"http://www.isotc211" +
                                  ".org/2005/srv\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211" +
                                  ".org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:geonet=\"http://www.fao.org/geonetwork\" " +
                                  "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gco:isoType=\"gmd:MD_Metadata\"> "
                                  + "<gmd:language xmlns:xalan=\"http://xml.apache.org/xalan\"> " +
                                  "<gco:CharacterString>fra</gco:CharacterString> </gmd:language> "
                                  + "<gmd:CI_OnlineResource>\n"
                                  + "  <gmd:name xsi:type=\"gmd:PT_FreeText_PropertyType\" gco:nilReason=\"missing\">\n"
                                  + "  </gmd:name>\n"
                                  + "</gmd:CI_OnlineResource>\n" + "</che:CHE_MD_Metadata>", false);

        transformed = Xml.transform(testData, pathToXsl);
        findAndAssert(transformed, new Count(0, new Finder("CI_OnlineResource/name")));
    }

    @Test
    public void fixBrokenKeywordXLinks() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp
                + "/xsl/characterstring-to-localisedcharacterstring.xsl";
        Element testData = Xml
                .loadString(
                        "<descriptiveKeywords xmlns:xlink=\"http://www.w3.org/1999/xlink\" xlink:href=\"local://che.keyword.get?thesaurus=local._none_.geocat.ch&amp;id=http%3A//geocat.ch/concept%23154&amp;amp;locales=DE,FR,IT,EN\"/>",
                        false);
        Element transformed = Xml.transform(testData, pathToXsl);
        String expected = "local://che.keyword.get?thesaurus=local._none_.geocat.ch&id=http%3A//geocat.ch/concept%23154&locales=DE,FR,IT,EN";
        assertEquals(expected, transformed.getAttributeValue("href", XLink.NAMESPACE_XLINK));
        assertFalse(Xml.getString(transformed).contains("&amp;amp;"));
    }

    @Test
    public void convertLocales() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp
                + "/xsl/characterstring-to-localisedcharacterstring.xsl";
        Element testData = Xml
                .loadString(
                        "<che:CHE_MD_Metadata  xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:geonet=\"http://www.fao.org/geonetwork\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gco:isoType=\"gmd:MD_Metadata\" xsi:schemaLocation=\"http://www.geocat.ch/2008/che http://www.isotc211.org/2005/gmd http://www.isotc211.org/2005/gmd/gmd.xsd http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd\">"
                                + "<gmd:language><gco:CharacterString>deu</gco:CharacterString></gmd:language>"
                                + "<gmd:locale><gmd:PT_Locale id=\"DE\"> <gmd:languageCode> <gmd:LanguageCode codeList=\"#LanguageCode\" codeListValue=\"deu\">German</gmd:LanguageCode> </gmd:languageCode> <gmd:characterEncoding> <gmd:MD_CharacterSetCode codeList=\"#MD_CharacterSetCode\" codeListValue=\"utf8\">UTF8</gmd:MD_CharacterSetCode> </gmd:characterEncoding> </gmd:PT_Locale> </gmd:locale>"
                                + "<gmd:locale> <gmd:PT_Locale id=\"FR\"> <gmd:languageCode> <gmd:LanguageCode codeList=\"#LanguageCode\" codeListValue=\"fra\">French</gmd:LanguageCode> </gmd:languageCode> <gmd:characterEncoding> <gmd:MD_CharacterSetCode codeList=\"#MD_CharacterSetCode\" codeListValue=\"utf8\">UTF8</gmd:MD_CharacterSetCode> </gmd:characterEncoding> </gmd:PT_Locale> </gmd:locale>"
                                + "<gmd:locale> <gmd:PT_Locale id=\"IT\"> <gmd:languageCode> <gmd:LanguageCode codeList=\"#LanguageCode\" codeListValue=\"ita\">Italian</gmd:LanguageCode> </gmd:languageCode> <gmd:characterEncoding> <gmd:MD_CharacterSetCode codeList=\"#MD_CharacterSetCode\" codeListValue=\"utf8\">UTF8</gmd:MD_CharacterSetCode> </gmd:characterEncoding> </gmd:PT_Locale> </gmd:locale>"
                                + "<gmd:locale> <gmd:PT_Locale id=\"EN\"> <gmd:languageCode> <gmd:LanguageCode codeList=\"#LanguageCode\" codeListValue=\"eng\">English</gmd:LanguageCode> </gmd:languageCode> <gmd:characterEncoding> <gmd:MD_CharacterSetCode codeList=\"#MD_CharacterSetCode\" codeListValue=\"utf8\">UTF8</gmd:MD_CharacterSetCode> </gmd:characterEncoding> </gmd:PT_Locale> </gmd:locale>"
                                + "<che:legislationInformation><che:CHE_MD_Legislation gco:isoType=\"gmd:MD_Legislation\">"
                                + "<che:language><gmd:LanguageCode codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#LanguageCode\" codeListValue=\"deu\" /></che:language>"
                                + "<che:language><gmd:LanguageCode codeList=\"http://www.isotc211.org/2005/resources/codeList.xml#LanguageCode\" codeListValue=\"fra\" /></che:language>"
                                + "</che:CHE_MD_Legislation></che:legislationInformation>" + "</che:CHE_MD_Metadata>",
                        false);
        Element transformed = Xml.transform(testData, pathToXsl);
        findAndAssert(transformed, new Count(1, new Finder("language/CharacterString", new EqualText("ger"))));
        findAndAssert(transformed, new Count(4, new Finder("locale/PT_Locale/languageCode")));
        findAndAssert(transformed, new Count(1, new Finder("locale/PT_Locale/languageCode/LanguageCode",
                new EqualAttribute("codeListValue", "ger"))));
        findAndAssert(transformed, new Count(1, new Finder("locale/PT_Locale/languageCode/LanguageCode",
                new EqualAttribute("codeListValue", "fre"))));
        findAndAssert(transformed, new Count(1, new Finder("locale/PT_Locale/languageCode/LanguageCode",
                new EqualAttribute("codeListValue", "ita"))));
        findAndAssert(transformed, new Count(1, new Finder("locale/PT_Locale/languageCode/LanguageCode",
                new EqualAttribute("codeListValue", "eng"))));

        findAndAssert(transformed, new Count(1, new Finder(
                "legislationInformation/CHE_MD_Legislation/language/LanguageCode", new EqualAttribute("codeListValue",
                        "ger"))));
        findAndAssert(transformed, new Count(1, new Finder(
                "legislationInformation/CHE_MD_Legislation/language/LanguageCode", new EqualAttribute("codeListValue",
                        "fre"))));

    }

    private void findAndAssert(Element transformed, Requirement finder) {
        assertTrue(finder + " did not find a match in: \n" + Xml.getString(transformed), finder.eval(transformed));
    }

    @Test
    public void convertCharacterstrings() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp
                + "/xsl/characterstring-to-localisedcharacterstring.xsl";
        String testData = "/data/iso19139/contact_with_linkage.xml";
        Element data = TransformationTestSupport.transform(getClass(), pathToXsl, testData);

        assertNoLocalisationString(data, "gmd:language");
        assertLocalisationString(data, ".//che:CHE_CI_ResponsibleParty/gmd:organisationName");
        assertLocalisationString(data, ".//che:CHE_CI_ResponsibleParty/che:organisationAcronym");
        assertLocalisationURL(data, ".//che:CHE_CI_ResponsibleParty//gmd:linkage");
        assertNoLocalisationString(data, ".//che:CHE_CI_ResponsibleParty//gmd:electronicMailAddress");
        assertNoLocalisationString(data, "./gmd:dataSetURI");

        assertNoLocalisationString(data, ".//gmd:distributionFormat//gmd:name");
        assertNoLocalisationString(data, ".//gmd:distributionFormat//gmd:version");
    }

    @Test
    public void removeCharacterstrings() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp
                + "/xsl/characterstring-to-localisedcharacterstring.xsl";
        String testData = "/data/iso19139/bothcharstringandlocalized.xml";
        Element data = TransformationTestSupport.transform(getClass(), pathToXsl, testData);

        assertNoLocalisationString(data, "gmd:language");
        assertLocalisationString(data, ".//che:CHE_CI_ResponsibleParty/gmd:organisationName");
    }

    @Test
    public void convertGmdLinkageIncorrectlyEmbeds() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp
                + "/xsl/characterstring-to-localisedcharacterstring.xsl";

        Element testData = Xml
                .loadString(
                        "<gmd:linkage xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xsi:type=\"che:PT_FreeURL_PropertyType\">"
                                + "<gmd:URL>http://wms.geo.admin.ch/?lang=de&amp;</gmd:URL>"
                                + "<che:PT_FreeURL><che:URLGroup><che:LocalisedURL locale=\"#FR\">http://wms.geo.admin.ch/?lang=fr&amp;</che:LocalisedURL></che:URLGroup></che:PT_FreeURL>"
                                + "</gmd:linkage>", false);

        Element transformed = Xml.transform(testData, pathToXsl);

        assertEquals(1, transformed.getChildren("PT_FreeURL", XslUtil.CHE_NAMESPACE).size());
        assertEquals(1, transformed.getChildren().size());
        assertEquals(2,
                transformed.getChild("PT_FreeURL", XslUtil.CHE_NAMESPACE)
                        .getChildren("URLGroup", XslUtil.CHE_NAMESPACE).size());
    }

    @Test
    public void xsiAttributeAddedIfNeeded() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp
                + "/xsl/characterstring-to-localisedcharacterstring.xsl";

        Element testData1 = Xml
                .loadString(
                        "<che:CHE_MD_Metadata  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" gco:isoType=\"gmd:MD_Metadata\">"
                                + "   <gmd:language><gco:CharacterString>deu</gco:CharacterString></gmd:language>"
                                + "   <gmd:contactInstructions><gmd:PT_FreeText><gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">Kundencenter</gmd:LocalisedCharacterString></gmd:textGroup></gmd:PT_FreeText></gmd:contactInstructions>"
                                + "</che:CHE_MD_Metadata>", false);
        Element testData2 = Xml
                .loadString(
                        "<che:CHE_MD_Metadata  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" gco:isoType=\"gmd:MD_Metadata\">"
                                + "   <gmd:language><gco:CharacterString>deu</gco:CharacterString></gmd:language>"
                                + "   <gmd:contactInstructions><gco:CharacterString>Kundencenter</gco:CharacterString></gmd:contactInstructions>"
                                + "</che:CHE_MD_Metadata>", false);

        Element transformed1 = Xml.transform(testData1, pathToXsl).getChild("contactInstructions",
                XslUtil.GMD_NAMESPACE);
        Element transformed2 = Xml.transform(testData2, pathToXsl).getChild("contactInstructions",
                XslUtil.GMD_NAMESPACE);

        assertNotNull(transformed1.getAttribute("type", XslUtil.XSI_NAMESPACE));
        assertNotNull(transformed2.getAttribute("type", XslUtil.XSI_NAMESPACE));
        assertEquals(1, transformed1.getChildren("PT_FreeText", XslUtil.GMD_NAMESPACE).size());
        assertEquals(1, transformed2.getChildren("PT_FreeText", XslUtil.GMD_NAMESPACE).size());
    }

    @Test
    public void bug_AddsExtraPT_FreeTextElement() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp
                + "/xsl/characterstring-to-localisedcharacterstring.xsl";

        Element testData1 = Xml
                .loadString(
                        "<che:CHE_MD_Metadata  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" gco:isoType=\"gmd:MD_Metadata\">"
                                + "   <gmd:language><gco:CharacterString>deu</gco:CharacterString></gmd:language>  <gmd:locale xmlns:xalan=\"http://xml.apache.org/xalan\" xmlns:comp=\"http://toignore\">\n" +
                                "    <gmd:PT_Locale id=\"DE\">\n" +
                                "      <gmd:languageCode>\n" +
                                "        <gmd:LanguageCode codeList=\"#LanguageCode\" codeListValue=\"ger\" />\n" +
                                "      </gmd:languageCode>\n" +
                                "      <gmd:characterEncoding>\n" +
                                "        <gmd:MD_CharacterSetCode codeList=\"#MD_CharacterSetCode\" codeListValue=\"utf8\">UTF8</gmd:MD_CharacterSetCode>\n" +
                                "      </gmd:characterEncoding>\n" +
                                "    </gmd:PT_Locale>\n" +
                                "  </gmd:locale>\n" +
                                "  <gmd:locale xmlns:xalan=\"http://xml.apache.org/xalan\" xmlns:comp=\"http://toignore\">\n" +
                                "    <gmd:PT_Locale id=\"FR\">\n" +
                                "      <gmd:languageCode>\n" +
                                "        <gmd:LanguageCode codeList=\"#LanguageCode\" codeListValue=\"fre\" />\n" +
                                "      </gmd:languageCode>\n" +
                                "      <gmd:characterEncoding>\n" +
                                "        <gmd:MD_CharacterSetCode codeList=\"#MD_CharacterSetCode\" codeListValue=\"utf8\">UTF8</gmd:MD_CharacterSetCode>\n" +
                                "      </gmd:characterEncoding>\n" +
                                "    </gmd:PT_Locale>\n" +
                                "  </gmd:locale>\n" +
                                "  <gmd:locale xmlns:xalan=\"http://xml.apache.org/xalan\" xmlns:comp=\"http://toignore\">\n" +
                                "    <gmd:PT_Locale id=\"IT\">\n" +
                                "      <gmd:languageCode>\n" +
                                "        <gmd:LanguageCode codeList=\"#LanguageCode\" codeListValue=\"ita\" />\n" +
                                "      </gmd:languageCode>\n" +
                                "      <gmd:characterEncoding>\n" +
                                "        <gmd:MD_CharacterSetCode codeList=\"#MD_CharacterSetCode\" codeListValue=\"utf8\">UTF8</gmd:MD_CharacterSetCode>\n" +
                                "      </gmd:characterEncoding>\n" +
                                "    </gmd:PT_Locale>\n" +
                                "  </gmd:locale>\n" +
                                "  <gmd:locale xmlns:xalan=\"http://xml.apache.org/xalan\" xmlns:comp=\"http://toignore\">\n" +
                                "    <gmd:PT_Locale id=\"EN\">\n" +
                                "      <gmd:languageCode>\n" +
                                "        <gmd:LanguageCode codeList=\"#LanguageCode\" codeListValue=\"eng\" />\n" +
                                "      </gmd:languageCode>\n" +
                                "      <gmd:characterEncoding>\n" +
                                "        <gmd:MD_CharacterSetCode codeList=\"#MD_CharacterSetCode\" codeListValue=\"utf8\">UTF8</gmd:MD_CharacterSetCode>\n" +
                                "      </gmd:characterEncoding>\n" +
                                "    </gmd:PT_Locale>\n" +
                                "  </gmd:locale>\n"
                                + "   <gmd:contactInstructions>"
                                + "     <gco:CharacterString>character string</gco:CharacterString>"
                                + "     <gmd:PT_FreeText><gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">Kundencenter</gmd:LocalisedCharacterString></gmd:textGroup></gmd:PT_FreeText>"
                                + "   </gmd:contactInstructions>"
                                + "</che:CHE_MD_Metadata>", false);

        Element transformed1 = Xml.transform(testData1, pathToXsl).getChild("contactInstructions",
                XslUtil.GMD_NAMESPACE);

        assertNotNull(transformed1.getAttribute("type", XslUtil.XSI_NAMESPACE));
        assertEquals(1, transformed1.getChildren("PT_FreeText", XslUtil.GMD_NAMESPACE).size());
        Element pt_freeText = (Element) transformed1.getChildren("PT_FreeText", XslUtil.GMD_NAMESPACE).get(0);

        assertEquals(0, pt_freeText.getChildren("PT_FreeText", XslUtil.GMD_NAMESPACE).size());
    }

    @Test
    public void noEmptyLocalisations() throws Exception {
        String pathToXsl = TransformationTestSupport.geonetworkWebapp
                + "/xsl/characterstring-to-localisedcharacterstring.xsl";
        String testData = "/data/non_validating/iso19139che/problemTitle_remove_charstrings.xml";
        Element data = TransformationTestSupport.transform(getClass(), pathToXsl, testData);

        List titleTextGroups = data.getChild("identificationInfo", XslUtil.GMD_NAMESPACE)
                .getChild("CHE_MD_DataIdentification", XslUtil.CHE_NAMESPACE)
                .getChild("citation", XslUtil.GMD_NAMESPACE).getChild("CI_Citation", XslUtil.GMD_NAMESPACE)
                .getChild("title", XslUtil.GMD_NAMESPACE).getChild("PT_FreeText", XslUtil.GMD_NAMESPACE)
                .getChildren("textGroup", XslUtil.GMD_NAMESPACE);
        assertEquals(1, titleTextGroups.size());
    }

    @Test
    public void testRefSysCode() throws Exception {
        Element data = Xml.loadString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                      + "<che:CHE_MD_Metadata xmlns:che=\"http://www.geocat.ch/2008/che\" xmlns:srv=\"http://www.isotc211.org/2005/srv\" xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" xmlns:geonet=\"http://www.fao.org/geonetwork\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" gco:isoType=\"gmd:MD_Metadata\" xsi:schemaLocation=\"http://www.geocat.ch/2008/che http://www.isotc211.org/2005/gmd http://www.isotc211.org/2005/gmd/gmd.xsd http://www.isotc211.org/2005/srv http://schemas.opengis.net/iso/19139/20060504/srv/srv.xsd\">\n"
                      + "  <gmd:language xmlns:comp=\"http://www.geocat.ch/2003/05/gateway/GM03Comprehensive\" xmlns:xalan=\"http://xml" +
                      ".apache.org/xalan\">\n"
                      + "    <gco:CharacterString>ger</gco:CharacterString>\n"
                      + "  </gmd:language>\n"
                      + "  <gmd:locale xmlns:comp=\"http://www.geocat.ch/2003/05/gateway/GM03Comprehensive\" xmlns:xalan=\"http://xml" +
                      ".apache.org/xalan\">\n"
                      + "    <gmd:PT_Locale id=\"DE\">\n"
                      + "      <gmd:languageCode>\n"
                      + "        <gmd:LanguageCode codeList=\"#LanguageCode\" codeListValue=\"ger\" />\n"
                      + "      </gmd:languageCode>\n"
                      + "      <gmd:characterEncoding>\n"
                      + "        <gmd:MD_CharacterSetCode codeList=\"#MD_CharacterSetCode\" " +
                      "codeListValue=\"utf8\">UTF8</gmd:MD_CharacterSetCode>\n"
                      + "      </gmd:characterEncoding>\n"
                      + "    </gmd:PT_Locale>\n"
                      + "  </gmd:locale>\n"
                      + "  <gmd:locale xmlns:comp=\"http://www.geocat.ch/2003/05/gateway/GM03Comprehensive\" xmlns:xalan=\"http://xml" +
                      ".apache.org/xalan\">\n"
                      + "    <gmd:PT_Locale id=\"FR\">\n"
                      + "      <gmd:languageCode>\n"
                      + "        <gmd:LanguageCode codeList=\"#LanguageCode\" codeListValue=\"fre\" />\n"
                      + "      </gmd:languageCode>\n"
                      + "      <gmd:characterEncoding>\n"
                      + "        <gmd:MD_CharacterSetCode codeList=\"#MD_CharacterSetCode\" " +
                      "codeListValue=\"utf8\">UTF8</gmd:MD_CharacterSetCode>\n"
                      + "      </gmd:characterEncoding>\n"
                      + "    </gmd:PT_Locale>\n"
                      + "  </gmd:locale>\n"
                      + "  <gmd:referenceSystemInfo>\n"
                      + "    <gmd:MD_ReferenceSystem>\n"
                      + "      <gmd:referenceSystemIdentifier>\n"
                      + "        <gmd:RS_Identifier>\n"
                      + "          <gmd:code xsi:type=\"gmd:PT_FreeText_PropertyType\">\n"
                      + "            <gco:CharacterString>EPSG:21781</gco:CharacterString>\n"
                      + "            <gmd:PT_FreeText>\n"
                      + "              <gmd:textGroup>\n"
                      + "                <gmd:LocalisedCharacterString locale=\"#FR\">EPSG:21781</gmd:LocalisedCharacterString>\n"
                      + "              </gmd:textGroup>\n"
                      + "              <gmd:textGroup>\n"
                      + "                <gmd:LocalisedCharacterString locale=\"#DE\">EPSG:21781</gmd:LocalisedCharacterString>\n"
                      + "              </gmd:textGroup>\n"
                      + "            </gmd:PT_FreeText>\n"
                      + "          </gmd:code>\n"
                      + "        </gmd:RS_Identifier>\n"
                      + "      </gmd:referenceSystemIdentifier>\n"
                      + "    </gmd:MD_ReferenceSystem>\n"
                      + "  </gmd:referenceSystemInfo>\n"
                      + "</che:CHE_MD_Metadata>\n", false);

        String pathToXsl = TransformationTestSupport.geonetworkWebapp
                           + "/xsl/characterstring-to-localisedcharacterstring.xsl";

        final Element afterTransform = Xml.transform(data, pathToXsl);

        assertNull(Xml.selectElement(afterTransform, "gmd:referenceSystemInfo//gmd:code/gco:CharacterString",
                Lists.newArrayList(GMD, GCO)));
    }

    private void assertNoLocalisationString(Element data, String baseXPath) throws Exception {
        assertNoLocalisation(data, baseXPath, "gmd:PT_FreeText", "gco:CharacterString");
    }

    private void assertLocalisationString(Element data, String baseXPath) throws Exception {
        assertLocalisation(data, baseXPath, "gmd:LocalisedCharacterString", "gco:CharacterString", "gmd:PT_FreeText_PropertyType");
    }

    private void assertLocalisationURL(Element data, String baseXPath) throws Exception {
        assertLocalisation(data, baseXPath, "che:LocalisedURL", "gmd:URL", "che:PT_FreeURL_PropertyType");
    }

    private void assertNoLocalisation(Element data, String baseXPath, String multiple, String single) throws Exception {
        assertEquals(0, Xml.selectNodes(data, baseXPath + "//" + multiple).size());
        assertFalse(Xml.selectNodes(data, baseXPath + "//" + single).isEmpty());
    }

    @SuppressWarnings("unchecked")
    private void assertLocalisation(Element data, String baseXPath, String multiple, String single, String attribute)
            throws Exception {
        List<Element> e = (List<Element>) Xml.selectNodes(data, baseXPath);
        for (Element elem : e) {
            assertEquals(attribute,
                    elem.getAttributeValue("type", Namespace.getNamespace("http://www.w3.org/2001/XMLSchema-instance")));
        }
        assertEquals(0, Xml.selectNodes(data, baseXPath + "//" + single).size());
        assertFalse(Xml.selectNodes(data, baseXPath + "//" + multiple).isEmpty());
        
        Collection<String> allowed = Arrays.asList(new String[] { "#IT", "#DE", "#FR", "#EN", "#RM" });
        List<Element> baseNodes = (List<Element>) Xml.selectNodes(data, baseXPath);
        for (Element baseNode : baseNodes) {
            Set<String> encountered = new HashSet<String>();
            
            List<Element> nodes = (List<Element>) Xml.selectNodes(baseNode, ".//"+multiple);
            for (Element element : nodes) {
                String lang = element.getAttributeValue("locale");
                assertTrue(lang+" is not one of the allowed locales.  Options include: "+allowed, allowed.contains(lang));
                assertFalse("The locale "+lang+" occurs twice in\n"+Xml.getString(baseNode), encountered.contains(lang));
                encountered.add(lang);
            }
        }
    }

}

package org.fao.geonet.services.metadata.format.groovy;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import jeeves.constants.ConfigFile;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.guiservices.XmlFile;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.services.metadata.format.Format;
import org.fao.geonet.services.metadata.format.FormatType;
import org.fao.geonet.services.metadata.format.FormatterParams;
import org.fao.geonet.services.metadata.format.SchemaLocalization;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.fao.geonet.services.metadata.format.groovy.Functions.LANG_CODELIST_NS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class FunctionsTest {
    private static final String schema = "iso19139";
    private static FormatterParams fparams;
    private static Functions functions;

    @BeforeClass
    public static void setUp() throws Exception {
        fparams = new FormatterParams() {
            @Override
            public boolean isDevMode() {
                return false;
            }
        };
        fparams.schema = schema;
        fparams.context = new ServiceContext(null, null, Maps.<String, Object>newHashMap(), null);
        fparams.context.setLanguage("eng");

        fparams.format = new Format() {
            @Override
            protected Map<String, SchemaLocalization> getSchemaLocalizations(ServiceContext context) throws IOException, JDOMException {
                Map<String, SchemaLocalization> localizations = Maps.newHashMap();
                Map<String, XmlFile> schemaInfo = Maps.newHashMap();
                schemaInfo.put("labels.xml", createXmlFile(new Element("labels").addContent(Arrays.asList(
                        createLabelElement("elem1", "parent", "Element One", "Desc Element One"),
                        createLabelElement("elem1", null, "Element One No Parent", "Desc Element One No Parent"),
                        createLabelElement("elem2", null, "Element Two", "Desc Element Two")
                ))));
                schemaInfo.put("codelists.xml", createXmlFile(new Element("codelists").addContent(Arrays.asList(
                        createCodelistElement("gmd:codelist1", "code1", "Code One", "Desc Code One").addContent(
                                createCodelistElement("gmd:codelist1", "code2", "Code Two", "Desc Code Two").getChild("entry").detach()
                        ),
                        createCodelistElement("gmd:codelist2", "code1", "Code Three", "Desc Code Three"),
                        createCodelistElement("gmd:codelist1", "code1", "Code Four", "Desc Code Four")
                ))));
                schemaInfo.put("strings.xml", createXmlFile(new Element("strings").addContent(Arrays.asList(
                       new Element("string1").setText("String One"),
                       new Element("string2").addContent(new Element("part2").setText("String Two Part Two"))
                ))));


                SchemaLocalization sl = new SchemaLocalization(fparams.context, fparams.schema, schemaInfo);
                localizations.put(fparams.schema, sl);
                return localizations;
            }

            @Override
            protected synchronized Element getPluginLocResources(ServiceContext context, File formatDir, String lang) throws Exception {
                return new Element("loc"); // todo tests
            }
        };

        Environment env = new Environment(){

            @Override
            public String getLang3() {
                return "eng";
            }

            @Override
            public String getLang2() {
                return "#EN";
            }

            @Override
            public int getMetadataId() {
                return 0;
            }

            @Override
            public String getMetadataUUID() {
                return null;
            }

            @Override
            public String getResourceUrl() {
                return null;
            }

            @Override
            public Authentication getAuth() {
                return null;
            }

            @Override
            public FormatType getFormatType() {
                return null;
            }

            @Override
            public Multimap<String, ParamValue> params() {
                return null;
            }

            @Override
            public ParamValue param(String paramName) {
                return null;
            }

            @Override
            public Collection<ParamValue> paramValues(String paramName) {
                return null;
            }
        };
        final IsoLanguageRepository repository = Mockito.mock(IsoLanguageRepository.class);
        Mockito.when(repository.findAllByCode("ger")).thenReturn(Arrays.asList(isoLang("German")));
        Mockito.when(repository.findAllByCode("eng")).thenReturn(Arrays.asList(isoLang("English")));
        Mockito.when(repository.findAllByShortCode("en")).thenReturn(Arrays.asList(isoLang("English")));
        Mockito.when(repository.findAllByShortCode("de")).thenReturn(Arrays.asList(isoLang("German")));
        functions = new Functions(fparams, env, repository);
    }

    private static IsoLanguage isoLang(String engTranslation) {
        final IsoLanguage isoLanguage = new IsoLanguage();
        isoLanguage.getLabelTranslations().put("eng", engTranslation);
        return isoLanguage;
    }

    @Test
    public void testLabel() throws Exception {
        assertEquals("Element One", functions.nodeLabel("elem1", "parent"));
        assertEquals("Element One No Parent", functions.nodeLabel("elem1", null));
        assertEquals("Desc Element One No Parent", functions.nodeDesc("elem1", null));
        assertEquals("Desc Element One No Parent", functions.nodeDesc("elem1", "random parent"));

        GPathResult gPathResult = new XmlSlurper().parseText("<elem1>hi</elem1>");
        assertEquals("Element One No Parent", functions.nodeLabel(gPathResult));
        assertEquals("Desc Element One No Parent", functions.nodeDesc(gPathResult));

        gPathResult = new XmlSlurper().parseText("<parent><elem1>hi</elem1></parent>");
        assertEquals("Element One", functions.nodeLabel((GPathResult) gPathResult.children().getAt(0)));
        assertEquals("Desc Element One", functions.nodeDesc((GPathResult) gPathResult.children().getAt(0)));


        gPathResult = new XmlSlurper().parseText("<m><elem1>hi</elem1></m>");
        assertEquals("Element One No Parent", functions.nodeLabel((GPathResult) gPathResult.children().getAt(0)));
        assertEquals("Desc Element One No Parent", functions.nodeDesc((GPathResult) gPathResult.children().getAt(0)));


        assertEquals("Desc Element Two", functions.nodeDesc("elem2", "random parent"));


    }
    @Test
    public void testCodeListValue() throws Exception {
        assertEquals("Code One", functions.codelistValueLabel("http://yaya.com#codelist1", "code1"));
        assertEquals("Code Three", functions.codelistValueLabel("http://yaya.com#codelist2", "code1"));
        assertEquals("Desc Code One", functions.codelistValueDesc("http://yaya.com#codelist1", "code1"));
        assertEquals("Desc Code Three", functions.codelistValueDesc("http://yaya.com#codelist2", "code1"));
        final GPathResult gPathResult = new XmlSlurper().parseText("<cl codeList='http://yaya.com#codelist1' codeListValue='code1'/>");
        assertEquals("Code One", functions.codelistValueLabel(gPathResult));
        assertEquals("Desc Code One", functions.codelistValueDesc(gPathResult));
    }@Test
    public void testLangCodeTranslations() throws Exception {
        assertEquals("English", functions.codelistValueLabel(LANG_CODELIST_NS, "eng"));
        assertEquals("English", functions.codelistValueLabel(LANG_CODELIST_NS, "en"));
        assertEquals("German", functions.codelistValueLabel(LANG_CODELIST_NS, "ger"));
        assertEquals("German", functions.codelistValueLabel(LANG_CODELIST_NS, "de"));
        assertEquals("German", functions.codelistValueLabel(LANG_CODELIST_NS, "deu"));
        assertEquals("xyz", functions.codelistValueLabel(LANG_CODELIST_NS, "xyz"));
        assertEquals("dd", functions.codelistValueLabel(LANG_CODELIST_NS, "dd"));
        assertEquals(null, functions.codelistValueLabel(LANG_CODELIST_NS, null));
    }
    @Test
    public void testCodeList() throws Exception {
        final Collection<String> codelist1 = functions.codelist("gmd:codelist1");
        assertArrayEquals(codelist1.toString(), new String[]{"code1", "code2"}, sort(codelist1.toArray()));
        final Collection<String> codelist2 = functions.codelist("gmd:codelist2");
        assertArrayEquals(codelist2.toString(), new String[]{"code1"}, sort(codelist2.toArray()));
    }
    @Test
    public void testStrings() throws Exception {
        assertEquals("String One", functions.schemaString("string1"));
        assertEquals("String Two Part Two", functions.schemaString("string2", "part2"));
    }

    private static Object[] sort(Object[] sort) {
        Arrays.sort(sort);
        return sort;
    }

    private static Element createLabelElement(String name, String parentName, String label, String desc) {
        final Element element = new Element("element");
        if (parentName != null) {
            element.setAttribute("context", parentName);
        }
        return element.setAttribute("name", name).addContent(Arrays.asList(
                new Element("label").setText(label),
                new Element("description").setText(desc)
        ));
    }


    private static Element createCodelistElement(String name, String code, String label, String desc) {
        return new Element("codelist").setAttribute("name", name).addContent(
                new Element("entry").addContent(Arrays.asList(
                        new Element("code").setText(code),
                        new Element("label").setText(label),
                        new Element("description").setText(desc)
                )));
    }

    private static XmlFile createXmlFile(final Element xml) {
        Element config = new Element("config").
                setAttribute(ConfigFile.Xml.Attr.NAME, "name").
                setAttribute(ConfigFile.Xml.Attr.FILE, "FILE").
                setAttribute(ConfigFile.Xml.Attr.BASE, "loc");
        return new XmlFile(config, "eng", true){
            @Override
            public Element getXml(ServiceContext context, String lang, boolean makeCopy) throws JDOMException, IOException {
                return xml;
            }
        };
    }}
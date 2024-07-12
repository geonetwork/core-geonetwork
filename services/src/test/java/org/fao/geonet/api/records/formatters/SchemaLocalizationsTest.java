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

package org.fao.geonet.api.records.formatters;

import com.google.common.collect.Maps;
import jeeves.server.dispatchers.guiservices.XmlFile;
import org.fao.geonet.domain.IsoLanguage;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.fao.geonet.api.records.formatters.SchemaLocalizations.LANG_CODELIST_NS;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class SchemaLocalizationsTest {

    public static final String SCHEMA = "iso19139";
    private SchemaLocalizations localizations;

    private static IsoLanguage isoLang(String engTranslation) {
        final IsoLanguage isoLanguage = new IsoLanguage();
        isoLanguage.getLabelTranslations().put("eng", engTranslation);
        return isoLanguage;
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
            setAttribute(jeeves.constants.ConfigFile.Xml.Attr.NAME, "name").
            setAttribute(jeeves.constants.ConfigFile.Xml.Attr.FILE, "FILE").
            setAttribute(jeeves.constants.ConfigFile.Xml.Attr.BASE, "loc");
        return new XmlFile(config, "eng", true) {
            @Override
            public Element getXml(ApplicationContext context, String lang, boolean makeCopy) throws JDOMException, IOException {
                return xml;
            }
        };
    }

    @Before
    public void setUp() throws Exception {

        CurrentLanguageHolder env = Mockito.mock(CurrentLanguageHolder.class);
        Mockito.when(env.getLang3()).thenReturn("eng");
        Mockito.when(env.getLang2()).thenReturn("en");

        final IsoLanguageRepository repository = Mockito.mock(IsoLanguageRepository.class);
        Mockito.when(repository.findAllByCode("ger")).thenReturn(Arrays.asList(isoLang("German")));
        Mockito.when(repository.findAllByCode("eng")).thenReturn(Arrays.asList(isoLang("English")));
        Mockito.when(repository.findAllByShortCode("en")).thenReturn(Arrays.asList(isoLang("English")));
        Mockito.when(repository.findAllByShortCode("de")).thenReturn(Arrays.asList(isoLang("German")));
        SchemaManager schemaManager = Mockito.mock(SchemaManager.class);
        final ApplicationContext appContext = Mockito.mock(ApplicationContext.class);
        Mockito.when(appContext.getBean(SchemaManager.class)).thenReturn(schemaManager);
        Mockito.when(appContext.getBean(IsoLanguageRepository.class)).thenReturn(repository);

        localizations = new SchemaLocalizations(appContext, env, SCHEMA, null) {
            @Override
            protected Map<String, SchemaLocalization> getSchemaLocalizations(ApplicationContext context, SchemaManager schemaManager)
                throws IOException, JDOMException {
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

                SchemaLocalization sl = new SchemaLocalization(appContext, SCHEMA, schemaInfo);
                localizations.put(SCHEMA, sl);
                return localizations;
            }

            @Override
            protected ConfigFile getConfigFile(SchemaManager schemaManager, String schema) throws IOException {
                return Mockito.mock(ConfigFile.class);
            }
        };
    }

    @Test
    public void testLabel() throws Exception {
        assertEquals("Element One", localizations.nodeLabel("elem1", "parent"));
        assertEquals("Element One No Parent", localizations.nodeLabel("elem1", null));
        assertEquals("Desc Element One No Parent", localizations.nodeDesc("elem1", null));
        assertEquals("Desc Element One No Parent", localizations.nodeDesc("elem1", "random parent"));
        assertEquals("Desc Element Two", localizations.nodeDesc("elem2", "random parent"));
    }

    @Test
    public void testCodeListValue() throws Exception {
        assertEquals("Code One", localizations.codelistValueLabel("http://yaya.com#codelist1", "code1"));
        assertEquals("Code Three", localizations.codelistValueLabel("http://yaya.com#codelist2", "code1"));
        assertEquals("Desc Code One", localizations.codelistValueDesc("http://yaya.com#codelist1", "code1"));
        assertEquals("Desc Code Three", localizations.codelistValueDesc("http://yaya.com#codelist2", "code1"));
    }

    @Test
    public void testLangCodeTranslations() throws Exception {
        assertEquals("English", localizations.codelistValueLabel(LANG_CODELIST_NS, "eng"));
        assertEquals("English", localizations.codelistValueLabel(LANG_CODELIST_NS, "en"));
        assertEquals("German", localizations.codelistValueLabel(LANG_CODELIST_NS, "ger"));
        assertEquals("German", localizations.codelistValueLabel(LANG_CODELIST_NS, "de"));
        assertEquals("German", localizations.codelistValueLabel(LANG_CODELIST_NS, "deu"));
        assertEquals("xyz", localizations.codelistValueLabel(LANG_CODELIST_NS, "xyz"));
        assertEquals("dd", localizations.codelistValueLabel(LANG_CODELIST_NS, "dd"));
        assertEquals(null, localizations.codelistValueLabel(LANG_CODELIST_NS, null));
    }

    @Test
    public void testCodeList() throws Exception {
        final Collection<String> codelist1 = localizations.codelist("gmd:codelist1");
        assertArrayEquals(codelist1.toString(), new String[]{"code1", "code2"}, sort(codelist1.toArray()));
        final Collection<String> codelist2 = localizations.codelist("gmd:codelist2");
        assertArrayEquals(codelist2.toString(), new String[]{"code1"}, sort(codelist2.toArray()));
    }

    @Test
    public void testStrings() throws Exception {
        assertEquals("String One", localizations.schemaString("string1"));
        assertEquals("String Two Part Two", localizations.schemaString("string2", "part2"));
    }

}

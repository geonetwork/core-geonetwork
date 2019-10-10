package org.fao.geonet.kernel.search;


import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SearchManagerTest {

    @Test
    public void mergeNominal() {
        SearchManager toTest = new SearchManager();
        Element indexFromDefault = singletonIndexList("default", "fre");
        Element indexFromFreLocale = singletonIndexList("locale", "fre");
        Element indexFromEngLocale = singletonIndexList("locale", "eng");
        Element indexFromGerLocale = singletonIndexList("locale", "ger");
        List<Element> indexFromLocales= new ArrayList<>();
        indexFromLocales.add(indexFromFreLocale);
        indexFromLocales.add(indexFromEngLocale);
        indexFromLocales.add(indexFromGerLocale);

       toTest.mergeDefaultLang(indexFromDefault, indexFromLocales);

       assertTrue(indexFromLocales.contains(indexFromEngLocale));
       assertTrue(indexFromLocales.contains(indexFromGerLocale));
       assertFalse(indexFromLocales.contains(indexFromFreLocale));
       assertNotNull(indexFromDefault.getChild("momo_from_default_fre"));
       assertNotNull(indexFromDefault.getChild("momo_from_locale_fre"));
    }

    @Test
    public void mergeDefinedTwiceForDefaultAtBothSide() {
        SearchManager toTest = new SearchManager();
        Element indexFromDefault = singletonIndexList("indexedTwice", "fre");
        Element indexFromFreLocale = singletonIndexList("indexedTwice", "fre");
        Element indexFromEngLocale = singletonIndexList("locale", "eng");
        Element indexFromGerLocale = singletonIndexList("locale", "ger");
        List<Element> indexFromLocales= new ArrayList<>();
        indexFromLocales.add(indexFromFreLocale);
        indexFromLocales.add(indexFromEngLocale);
        indexFromLocales.add(indexFromGerLocale);

        toTest.mergeDefaultLang(indexFromDefault, indexFromLocales);

        assertTrue(indexFromLocales.contains(indexFromEngLocale));
        assertTrue(indexFromLocales.contains(indexFromGerLocale));
        assertFalse(indexFromLocales.contains(indexFromFreLocale));
        assertNotNull(indexFromDefault.getChild("momo_from_indexedTwice_fre"));
        assertEquals(1, indexFromDefault.getContentSize());
    }

    @Test
    public void mergeDefinedTwiceForDefaultAtDefaultSide() {
        SearchManager toTest = new SearchManager();
        Element indexFromDefault = singletonIndexList("indexedTwice", "fre");
        indexFromDefault.addContent(indexFromDefault.cloneContent());
        Element indexFromEngLocale = singletonIndexList("locale", "eng");
        Element indexFromGerLocale = singletonIndexList("locale", "ger");
        List<Element> indexFromLocales= new ArrayList<>();
        indexFromLocales.add(indexFromEngLocale);
        indexFromLocales.add(indexFromGerLocale);

        toTest.mergeDefaultLang(indexFromDefault, indexFromLocales);

        assertNotNull(indexFromDefault.getChild("momo_from_indexedTwice_fre"));
        assertEquals(1, indexFromDefault.getContentSize());
    }

    @Test
    public void mergeDefinedTwiceForDefaultAtLocalSide() {
        SearchManager toTest = new SearchManager();
        Element indexFromDefault = singletonIndexList("indexedTwice", "fre");
        indexFromDefault.removeContent();
        Element indexFromFreLocale = singletonIndexList("indexedTwice", "fre");
        indexFromFreLocale.addContent(indexFromFreLocale.cloneContent());
        Element indexFromEngLocale = singletonIndexList("locale", "eng");
        Element indexFromGerLocale = singletonIndexList("locale", "ger");
        List<Element> indexFromLocales= new ArrayList<>();
        indexFromLocales.add(indexFromFreLocale);
        indexFromLocales.add(indexFromEngLocale);
        indexFromLocales.add(indexFromGerLocale);

        toTest.mergeDefaultLang(indexFromDefault, indexFromLocales);

        assertNotNull(indexFromDefault.getChild("momo_from_indexedTwice_fre"));
        assertEquals(1, indexFromDefault.getContentSize());
    }


    private Element singletonIndexList(String source, String language) {
        String elementName = "momo_from_" + source + "_" + language;
        Element indexFromDefault = new Element("Document").setAttribute(Geonet.IndexFieldNames.LOCALE, language);
        indexFromDefault.addContent(new Element(elementName).setAttribute("name", elementName));
        return indexFromDefault;
    }

}
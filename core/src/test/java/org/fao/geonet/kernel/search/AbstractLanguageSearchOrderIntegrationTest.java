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

package org.fao.geonet.kernel.search;

import jeeves.server.context.ServiceContext;

import org.apache.commons.io.IOUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.repository.Updater;
import org.jdom.JDOMException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;

import static org.fao.geonet.kernel.setting.SettingInfo.SearchRequestLanguage.ONLY_DOC_LOCALE;
import static org.fao.geonet.kernel.setting.SettingInfo.SearchRequestLanguage.ONLY_LOCALE;
import static org.fao.geonet.kernel.setting.SettingInfo.SearchRequestLanguage.ONLY_UI_LOCALE;
import static org.fao.geonet.kernel.setting.SettingInfo.SearchRequestLanguage.PREFER_LOCALE;
import static org.fao.geonet.kernel.setting.SettingInfo.SearchRequestLanguage.PREFER_UI_LOCALE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test the order of search results with regards to language settings.
 * <p/>
 * Created by Jesse on 1/27/14.
 */
public abstract class AbstractLanguageSearchOrderIntegrationTest extends AbstractCoreIntegrationTest {
    public static List<String> METADATA_TO_IMPORT = new ArrayList<String>(10);
    protected MetaSearcher _luceneSearcher;
    protected ServiceContext _serviceContext;
    protected String _abstractSearchTerm;
    @Autowired
    private SettingRepository _settingRepository;
    @Autowired
    private SearchManager _searchManager;

    @BeforeClass
    public static synchronized void bareMetadataXml() throws IOException, JDOMException {
        if (METADATA_TO_IMPORT.isEmpty()) {
            final URL url = AbstractLanguageSearchOrderIntegrationTest.class.getResource("templated-name-lang.iso19139.xml");
            final String xmlString = IOUtils.toString(url,
                "UTF-8");

            METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#DE\">zz</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));
            METADATA_TO_IMPORT.add(loadMetadata("ita",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#DE\">yy</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("ita",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#DE\">xx</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">A ENG EN and FR is " +
                    "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">A ENG EN and FR is " +
                    "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">E2 ENG EN and FR is " +
                    "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">Z2 ENG EN and FR is " +
                    "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">G eng is " +
                    "fr</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">e eng en and fr is " +
                    "fr</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">e eng en and fr is " +
                    "en</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("fre",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">é fra is " +
                    "fr</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("fre",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">A FRA EN and FR is " +
                    "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">A FRA EN and FR is " +
                    "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));

            METADATA_TO_IMPORT.add(loadMetadata("fre",
                "<gmd:PT_FreeText>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">Z3 FRA EN and FR is " +
                    "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">E3 FRA EN and FR is " +
                    "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                    + "</gmd:PT_FreeText>", xmlString));
        }
    }


    private static String loadMetadata(String lang, String title, String xmlString) throws IOException, JDOMException {
        final String updatedXmlString = xmlString.replace("{lang}", lang).replace("{title}", title);
        return updatedXmlString;
    }

    public void importMetadata(String searchTerm) throws Exception {
        this._serviceContext = createServiceContext();
        loginAsAdmin(_serviceContext);
        _abstractSearchTerm = searchTerm;
        for (String element : METADATA_TO_IMPORT) {
            byte[] bytes = element.replace("{uuid}", "" + _abstractSearchTerm).getBytes("UTF-8");
            importMetadataXML(_serviceContext, "uuid:" + System.currentTimeMillis(), new ByteArrayInputStream(bytes),
                MetadataType.METADATA, ReservedGroup.intranet.getId(), Params.GENERATE_UUID);
        }

        this._luceneSearcher = _searchManager.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);
    }

    protected abstract String[] doSearch(String lang) throws Exception;

    @After
    public void tearDownResources() throws Exception {
        if (this._luceneSearcher != null) {
            this._luceneSearcher.close();
        }
    }

    @Test
    public void freTitleSearch_RequestLangNotSorted_AllLanguagesAllowed() throws Exception {
        importMetadata("" + System.currentTimeMillis());
        setSearchSettings(PREFER_LOCALE, false, false);
        String[] titles = doSearch("fre");
        assertArrayEquals(new String[]{
            "A ENG EN and FR is FR",
            "A FRA EN and FR is FR",
            "E2 ENG EN and FR is FR",
            "e eng en and fr is fr",
            "é fra is fr",
            "G eng is fr",
            "xx",
            "yy",
            "Z3 FRA EN and FR is FR",
            "zz"}, titles);
    }

    @Test
    public void engTitleSearch_RequestLangNotSorted_AllLanguagesAllowed() throws Exception {
        importMetadata("" + System.currentTimeMillis());
        setSearchSettings(PREFER_LOCALE, false, false);
        String[] titles = doSearch("eng");
        assertArrayEquals(new String[]{"A ENG EN and FR is EN", "A FRA EN and FR is EN", "E3 FRA EN and FR is EN",
            "e eng en and fr is en", "é fra is fr", "G eng is fr", "xx", "yy", "Z2 ENG EN and FR is EN", "zz"}, titles);
    }

    @Test
    public void freTitleSearch_RequestLangNotSorted_OnlyResultsInSearchLanguageAllowed() throws Exception {
        importMetadata("" + System.currentTimeMillis());
        setSearchSettings(ONLY_DOC_LOCALE, false, false);
        String[] titles = doSearch("fre");
        assertArrayEquals(new String[]{"A FRA EN and FR is FR", "é fra is fr", "Z3 FRA EN and FR is FR"}, titles);
    }

    @Test
    public void engTitleSearch_RequestLangNotSorted_OnlyResultsInSearchLanguageAllowed() throws Exception {
        importMetadata("" + System.currentTimeMillis());
        setSearchSettings(ONLY_DOC_LOCALE, false, false);
        String[] titles = doSearch("eng");
        assertArrayEquals(new String[]{"A ENG EN and FR is EN", "e eng en and fr is en", "G eng is fr", "Z2 ENG EN and FR is EN",
            "zz"}, titles);
    }

    @Test
    public void freTitleSearch_RequestLangNotSorted_OnlyResultsContainingDataInSearchLanguageAllowed() throws Exception {
        importMetadata("" + System.currentTimeMillis());
        setSearchSettings(ONLY_LOCALE, false, false);
        String[] titles = doSearch("fre");
        assertArrayEquals(new String[]{"A FRA EN and FR is FR", "é fra is fr", "Z3 FRA EN and FR is FR"}, titles);
    }

    @Test
    public void engTitleSearch_RequestLangNotSorted_OnlyResultsContainingDataInSearchLanguageAllowed() throws Exception {
        importMetadata("" + System.currentTimeMillis());
        setSearchSettings(ONLY_LOCALE, false, false);
        String[] titles = doSearch("eng");
        assertArrayEquals(new String[]{"A ENG EN and FR is EN", "A FRA EN and FR is EN", "E3 FRA EN and FR is EN",
            "e eng en and fr is en", "é fra is fr", "G eng is fr", "xx", "yy", "Z2 ENG EN and FR is EN", "zz"}, titles);
    }

    @Test
    public void freAutoDetect_RequestLangNotSorted_OnlyResultsContainingDataInSearchLanguageAllowed() throws Exception {
        importMetadata("comment allez-vous aujourd'hui");
        setSearchSettings(ONLY_LOCALE, false, true);
        String[] titles = doSearch("eng");
        assertArrayEquals(new String[]{"A FRA EN and FR is FR", "é fra is fr", "Z3 FRA EN and FR is FR"}, titles);
    }

    @Test
    public void engAutoDetect_RequestLangNotSorted_OnlyResultsContainingDataInSearchLanguageAllowed() throws Exception {
        importMetadata("it is a very nice day");
        setSearchSettings(ONLY_LOCALE, false, true);
        String[] titles = doSearch("fre");
        assertArrayEquals(new String[]{"A ENG EN and FR is EN", "A FRA EN and FR is EN", "E3 FRA EN and FR is EN",
            "e eng en and fr is en", "é fra is fr", "G eng is fr", "xx", "yy", "Z2 ENG EN and FR is EN", "zz"}, titles);
    }

    @Test
    public void freAutoDetect_RequestLangNotSorted_OnlyResultsContainingDataInSearchLanguageAllowed_UseDisplayLanguageAsPreferredLanguage() throws Exception {
        importMetadata("comment allez-vous aujourd'hui");
        setSearchSettings(ONLY_UI_LOCALE, false, true);
        String[] titles = doSearch("eng");
        assertArrayEquals(new String[]{"A ENG EN and FR is EN", "A FRA EN and FR is EN", "E3 FRA EN and FR is EN",
            "e eng en and fr is en", "é fra is fr", "G eng is fr", "xx", "yy", "Z2 ENG EN and FR is EN", "zz"}, titles);
    }

    @Test
    public void engAutoDetect_RequestLangNotSorted_OnlyResultsContainingDataInSearchLanguageAllowed_UseDisplayLanguageAsPreferredLanguage() throws Exception {
        importMetadata("it is a very nice day");
        setSearchSettings(ONLY_UI_LOCALE, false, true);
        String[] titles = doSearch("fre");
        assertArrayEquals(new String[]{"A FRA EN and FR is FR", "é fra is fr", "Z3 FRA EN and FR is FR"}, titles);
    }

    @Test
    public void freAutoDetect_RequestLangNotSorted_AllLanguagesAllowed_UseDisplayLanguageAsPreferredLanguage() throws Exception {
        importMetadata("comment allez-vous aujourd'hui");
        setSearchSettings(PREFER_UI_LOCALE, false, true);
        String[] titles = doSearch("eng");
        assertArrayEquals(new String[]{"A ENG EN and FR is EN", "A FRA EN and FR is EN", "E3 FRA EN and FR is EN",
            "e eng en and fr is en", "é fra is fr", "G eng is fr", "xx", "yy", "Z2 ENG EN and FR is EN", "zz"}, titles);
    }

    @Test
    public void engAutoDetect_RequestLangNotSorted_AllLanguagesAllowed_UseDisplayLanguageAsPreferredLanguage() throws Exception {
        importMetadata("it is a very nice day");
        setSearchSettings(PREFER_UI_LOCALE, false, true);
        String[] titles = doSearch("fre");
        assertArrayEquals(new String[]{"A ENG EN and FR is FR", "A FRA EN and FR is FR", "E2 ENG EN and FR is FR",
            "e eng en and fr is fr", "é fra is fr", "G eng is fr", "xx", "yy", "Z3 FRA EN and FR is FR", "zz"}, titles);
    }

    @Test
    public void freTitleSearch_RequestLangSorted_AllLanguagesAllowed() throws Exception {
        importMetadata("" + System.currentTimeMillis());
        setSearchSettings(PREFER_LOCALE, true, false);
        String[] titles = doSearch("fre");
        assertContainsOnly(titles, "A ENG EN and FR is FR", "A FRA EN and FR is FR", "e eng en and fr is fr", "é fra is fr",
            "E2 ENG EN and FR is FR", "G eng is fr", "xx", "yy", "Z3 FRA EN and FR is FR", "zz");
        assertContainsInOrderOnly(titles, 0, 3, "A FRA EN and FR is FR", "é fra is fr", "Z3 FRA EN and FR is FR");
        assertContainsInOrder(titles, 3, titles.length, "A ENG EN and FR is FR", "e eng en and fr is fr", "G eng is fr", "zz");
        assertContainsInOrder(titles, 3, titles.length, "xx", "yy");
    }

    @Test
    public void engTitleSearch_RequestLangSorted_AllLanguagesAllowed() throws Exception {
        importMetadata("" + System.currentTimeMillis());
        setSearchSettings(PREFER_LOCALE, true, false);
        String[] titles = doSearch("eng");
        assertContainsOnly(titles, "A ENG EN and FR is EN", "A FRA EN and FR is EN", "E3 FRA EN and FR is EN",
            "e eng en and fr is en", "é fra is fr", "G eng is fr", "xx", "yy", "Z2 ENG EN and FR is EN", "zz");
        assertContainsInOrderOnly(titles, 0, 5, "A ENG EN and FR is EN", "e eng en and fr is en", "G eng is fr",
            "Z2 ENG EN and FR is EN", "zz");
        assertContainsInOrder(titles, 5, titles.length, "A FRA EN and FR is EN", "E3 FRA EN and FR is EN", "é fra is fr");
        assertContainsInOrder(titles, 5, titles.length, "xx", "yy");
    }

    protected void assertContainsInOrder(String[] titles, int start, int end, String... expectedValues) {
        final List<String> list = Arrays.asList(titles).subList(start, end);
        int lastIndex = -1;
        for (String expectedValue : expectedValues) {
            int index = list.indexOf(expectedValue);
            assertTrue(index != -1);
            assertTrue(index > lastIndex);
            lastIndex = index;
        }
    }

    private void assertContainsInOrderOnly(String[] titles, int start, int end, String... expectedValues) {
        assertArrayEquals(expectedValues, Arrays.asList(titles).subList(start, end).toArray());
    }

    private void assertContainsOnly(String[] titles, String... expectedValues) {
        assertEquals(expectedValues.length, titles.length);
        final List<String> titlesList = Arrays.asList(titles);
        final List<String> expectedList = Arrays.asList(expectedValues);

        HashSet extras = new HashSet(titlesList);
        extras.removeAll(expectedList);
        HashSet missing = new HashSet(expectedList);
        missing.removeAll(titlesList);

        if (!extras.isEmpty() || !missing.isEmpty()) {
            throw new AssertionError("Following strings should not be in results: " + extras + "\nFollowing strings should have been in" +
                " results: " + missing);
        }
    }

    private void setSearchSettings(final SettingInfo.SearchRequestLanguage searchSetting, final Boolean sorted,
                                   final Boolean autoDetectSearchLanguage) {
        _settingRepository.update(Settings.SYSTEM_REQUESTED_LANGUAGE_ONLY, new Updater<Setting>() {
            @Override
            public void apply(@Nonnull Setting entity) {
                entity.setValue(searchSetting.databaseValue);
            }
        });
        _settingRepository.update(Settings.SYSTEM_REQUESTED_LANGUAGE_SORTED, new Updater<Setting>() {
            @Override
            public void apply(@Nonnull Setting entity) {
                entity.setValue(sorted.toString());
            }
        });
        _settingRepository.update(Settings.SYSTEM_AUTODETECT_ENABLE, new Updater<Setting>() {
            @Override
            public void apply(@Nonnull Setting entity) {
                entity.setValue(autoDetectSearchLanguage.toString());
            }
        });
    }

}

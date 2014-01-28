package org.fao.geonet.kernel.search;

import jeeves.server.context.ServiceContext;
import org.apache.commons.io.IOUtils;
import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.repository.Updater;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test the order of search results with regards to language settings.
 * <p/>
 * Created by Jesse on 1/27/14.
 */
public abstract class AbstractLanguageSearchOrderIntegrationTest extends AbstractCoreIntegrationTest {

    @Autowired
    private SettingRepository _settingRepository;

    @Autowired
    private SearchManager _searchManager;

    @Autowired
    private LuceneConfig _luceneConfig;

    protected MetaSearcher _luceneSearcher;
    protected ServiceContext _serviceContext;
    protected long _timestamp;

    @BeforeClass
    public static void bareMetadataXml() throws IOException, JDOMException {
        SettingManager.METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#DE\">zz</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "</gmd:PT_FreeText>"));
        SettingManager.METADATA_TO_IMPORT.add(loadMetadata("ita",
                "<gmd:PT_FreeText>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#DE\">yy</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "</gmd:PT_FreeText>"));

        SettingManager.METADATA_TO_IMPORT.add(loadMetadata("ita",
                "<gmd:PT_FreeText>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#DE\">xx</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "</gmd:PT_FreeText>"));

        SettingManager.METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">A ENG EN and FR is " +
                "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">A ENG EN and FR is " +
                "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "</gmd:PT_FreeText>"));

        SettingManager.METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">E2 ENG EN and FR is " +
                "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">Z2 ENG EN and FR is " +
                "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "</gmd:PT_FreeText>"));

        SettingManager.METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">G eng is " +
                "fr</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "</gmd:PT_FreeText>"));

        SettingManager.METADATA_TO_IMPORT.add(loadMetadata("eng",
                "<gmd:PT_FreeText>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">e eng en and fr is " +
                "fr</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">e eng en and fr is " +
                "en</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "</gmd:PT_FreeText>"));

        SettingManager.METADATA_TO_IMPORT.add(loadMetadata("fre",
                "<gmd:PT_FreeText>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">é fra is " +
                "fr</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "</gmd:PT_FreeText>"));

        SettingManager.METADATA_TO_IMPORT.add(loadMetadata("fre",
                "<gmd:PT_FreeText>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">A FRA EN and FR is " +
                "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">A FRA EN and FR is " +
                "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "</gmd:PT_FreeText>"));

        SettingManager.METADATA_TO_IMPORT.add(loadMetadata("fre",
                "<gmd:PT_FreeText>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#FR\">Z3 FRA EN and FR is " +
                "FR</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "<gmd:textGroup><gmd:LocalisedCharacterString locale=\"#EN\">E3 FRA EN and FR is " +
                "EN</gmd:LocalisedCharacterString></gmd:textGroup>\n"
                + "</gmd:PT_FreeText>"));

    }


    private static String loadMetadata(String lang, String title) throws IOException, JDOMException {
        final String xmlString = IOUtils.toString(AbstractLanguageSearchOrderIntegrationTest.class.getResource("templated-name-lang" +
                                                                                                               ".iso19139.xml"),
                "UTF-8");
        final String updatedXmlString = xmlString.replace("{lang}", lang).replace("{title}", title);
        return updatedXmlString;
    }

    @Before
    public void importMetadata() throws Exception {
        _timestamp = System.currentTimeMillis();
        this._serviceContext = createServiceContext();
        loginAsAdmin(_serviceContext);
        for (String element : SettingManager.METADATA_TO_IMPORT) {
            byte[] bytes = element.replace("{uuid}", "" + _timestamp).getBytes("UTF-8");
            importMetadataXML(_serviceContext, "uuid:" + System.currentTimeMillis(), new ByteArrayInputStream(bytes),
                    MetadataType.METADATA, ReservedGroup.intranet.getId(), Params.GENERATE_UUID);
        }

        this._luceneSearcher = _searchManager.newSearcher(SearchManager.LUCENE, Geonet.File.SEARCH_LUCENE);
    }

    protected abstract String[] doSearch(String lang) throws Exception;

    @Test
    public void freTitleSearch_RequestLangNotSorted_AllLanguagesAllowed() throws Exception {
        setSearchSettings("prefer_locale", false);
        String[] titles = doSearch("fre");
        assertArrayEquals(new String[]{"A ENG EN and FR is FR", "A FRA EN and FR is FR", "E2 ENG EN and FR is FR",
                "e eng en and fr is fr", "é fra is fr", "G eng is fr", "xx", "yy", "Z3 FRA EN and FR is FR", "zz"}, titles);
    }

    @Test
    public void engTitleSearch_RequestLangNotSorted_AllLanguagesAllowed() throws Exception {
        setSearchSettings("prefer_locale", false);
        String[] titles = doSearch("eng");
        assertArrayEquals(new String[]{"A ENG EN and FR is EN", "A FRA EN and FR is EN", "E3 FRA EN and FR is EN",
                "e eng en and fr is en", "é fra is fr", "G eng is fr", "xx", "yy", "Z2 ENG EN and FR is EN", "zz"}, titles);
    }

    @Test
    public void freTitleSearch_RequestLangNotSorted_OnlyResultsInSearchLanguageAllowed() throws Exception {
        setSearchSettings("only_docLocale", false);
        String[] titles = doSearch("fre");
        assertArrayEquals(new String[]{"A FRA EN and FR is FR", "é fra is fr", "Z3 FRA EN and FR is FR"}, titles);
    }

    @Test
    public void engTitleSearch_RequestLangNotSorted_OnlyResultsInSearchLanguageAllowed() throws Exception {
        setSearchSettings("only_docLocale", false);
        String[] titles = doSearch("eng");
        assertArrayEquals(new String[]{"A ENG EN and FR is EN", "e eng en and fr is en", "G eng is fr", "Z2 ENG EN and FR is EN",
                "zz"}, titles);
    }

    @Test
    public void freTitleSearch_RequestLangNotSorted_OnlyResultsContainingDataInSearchLanguageAllowed() throws Exception {
        setSearchSettings("only_locale", false);
        String[] titles = doSearch("fre");
        assertArrayEquals(new String[]{"A FRA EN and FR is FR", "é fra is fr", "Z3 FRA EN and FR is FR"}, titles);
    }

    @Test
    public void engTitleSearch_RequestLangNotSorted_OnlyResultsContainingDataInSearchLanguageAllowed() throws Exception {
        setSearchSettings("only_locale", false);
        String[] titles = doSearch("eng");
        assertArrayEquals(new String[]{"A ENG EN and FR is EN", "A FRA EN and FR is EN", "E3 FRA EN and FR is EN",
                "e eng en and fr is en", "é fra is fr", "G eng is fr", "xx", "yy", "Z2 ENG EN and FR is EN", "zz"}, titles);
    }

    @Test
    public void freTitleSearch_RequestLangSorted_AllLanguagesAllowed() throws Exception {
        setSearchSettings("prefer_locale", true);
        String[] titles = doSearch("fre");
        assertContainsOnly(titles, new String[]{"A ENG EN and FR is FR", "A FRA EN and FR is FR", "e eng en and fr is fr", "é fra is fr",
                "E2 ENG EN and FR is FR", "G eng is fr", "xx", "yy", "Z3 FRA EN and FR is FR", "zz"});
        assertContainsInOrderOnly(titles, 0, 3, "A FRA EN and FR is FR", "é fra is fr", "Z3 FRA EN and FR is FR");
        assertContainsInOrder(titles, 3, titles.length, "A ENG EN and FR is FR", "e eng en and fr is fr", "G eng is fr", "zz");
        assertContainsInOrder(titles, 3, titles.length, "xx", "yy");
    }

    @Test
    public void engTitleSearch_RequestLangSorted_AllLanguagesAllowed() throws Exception {
        setSearchSettings("prefer_locale", true);
        String[] titles = doSearch("eng");
        assertContainsOnly(new String[]{"A ENG EN and FR is EN", "A FRA EN and FR is EN", "E3 FRA EN and FR is EN",
                "e eng en and fr is en", "é fra is fr", "G eng is fr", "xx", "yy", "Z2 ENG EN and FR is EN", "zz"}, titles);
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

    private void setSearchSettings(final String searchSetting, final Boolean sorted) {
        _settingRepository.update(SettingManager.SYSTEM_REQUESTED_LANGUAGE_ONLY, new Updater<Setting>() {
            @Override
            public void apply(@Nonnull Setting entity) {
                entity.setValue(searchSetting);
            }
        });
        _settingRepository.update(SettingManager.SYSTEM_REQUESTED_LANGUAGE_SORTED, new Updater<Setting>() {
            @Override
            public void apply(@Nonnull Setting entity) {
                entity.setValue(sorted.toString());
            }
        });
    }

}

package org.fao.geonet.kernel.search;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AbstractThesaurusBasedTest;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusFinder;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.kernel.search.keyword.KeywordSearchType;
import org.fao.geonet.kernel.search.keyword.KeywordSort;
import org.fao.geonet.kernel.search.keyword.SortDirection;
import org.fao.geonet.utils.IO;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class KeywordsSearcherTest extends AbstractThesaurusBasedTest {

    private static final String FOO_COM_NS = "http://foo.com#";
    private static final String BLAH_COM_NS = "http://blah.com#";
    private ThesaurusFinder thesaurusFinder;
    private Thesaurus thesaurusBlah;
    private Thesaurus thesaurusFoo;
    private int smallWords = 20;

    public KeywordsSearcherTest() {
        super(true);
    }
    
    @Before
    public synchronized void createExtraThesauri() throws Exception {
        final URL resource = KeywordsSearcherTest.class.getResource(KeywordsSearcherTest.class.getSimpleName() + ".class");
        Path directory = IO.toPath(resource.toURI()).getParent();

        thesaurusBlah = createThesaurus(directory, "blah");
        thesaurusFoo = createThesaurus(directory, "foo");
        
        this.thesaurusFinder = new ThesaurusFinder() {
            
            private Map<String, Thesaurus> map = new HashMap<String, Thesaurus>();
            {
                map.put(thesaurus.getKey(), thesaurus);
                map.put(thesaurusBlah.getKey(), thesaurusBlah);
                map.put(thesaurusFoo.getKey(), thesaurusFoo);
            }

            @Override
            public Thesaurus getThesaurusByName(String thesaurusName) {
                return getThesauriMap().get(thesaurusName);
            }
            
            @Override
            public Thesaurus getThesaurusByConceptScheme(String conceptSchemeUri) {
                for (Thesaurus thesaurus: getThesauriMap().values()) {
                    if (thesaurus.hasConceptScheme(conceptSchemeUri)) return thesaurus;
                }
                return null;
            }

            @Override
            public Map<String, Thesaurus> getThesauriMap() {
                return Collections.unmodifiableMap(map);
            }
            
            @Override
            public boolean existsThesaurus(String name) {
                return getThesauriMap().containsKey(name);
            }
        };
    }
    
    private Thesaurus createThesaurus(Path directory, String string) throws Exception {
        Path thesaurusBlahFile = directory.resolve(string+".rdf");
        Thesaurus thes = new Thesaurus(isoLangMapper, string+".rdf", Geonet.CodeList.EXTERNAL, string, thesaurusBlahFile, "http://"+string+".com");
        thes.initRepository();
        if(!Files.exists(thesaurusBlahFile) || Files.size(thesaurusBlahFile) == 0) {
            populateThesaurus(thes, smallWords, "http://"+string+".com#", string+"Val", string+"Note", languages);
        }
        return thes;
    }

    @After
    public synchronized void deleteExtraThesauri() {
        thesaurusBlah.getRepository().shutDown();
        thesaurusFoo.getRepository().shutDown();
    }

    private String createBlahLabel(int i, String lang) {
        return createExampleLabel(i, "blahVal", lang);
    }
    
    private String createFooLabel(int i, String lang) {
        return createExampleLabel(i, "fooVal", lang);
    }
    private String createBlahNote(int i, String lang) {
        return createExampleLabel(i, "blahNote", lang);
    }
    
    private String createFooNote(int i, String lang) {
        return createExampleLabel(i, "fooNote", lang);
    }
 

    @Test
    public void testSearchByIdStringStringString() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        
        KeywordBean kw = searcher.searchById(FOO_COM_NS+"4", thesaurusFoo.getKey(), "eng");
        
        assertNotNull(kw);
        assertEquals(createFooLabel(4, "eng"), kw.getDefaultValue());
        assertEquals(createFooNote(4, "eng"), kw.getDefaultDefinition());
        
        kw = searcher.searchById(BLAH_COM_NS+"4", thesaurusBlah.getKey(), "eng", "fre", "ita", "ger", "chi");
        assertEquals(createBlahLabel(4, "eng"), kw.getDefaultValue());
        assertEquals(createBlahNote(4, "eng"), kw.getDefaultDefinition());
        assertEquals("eng", kw.getDefaultLang());
        assertNotNull(kw);
        assertEquals(createBlahLabel(4, "fre"), kw.getValues().get("fre"));
        assertEquals(createBlahNote(4, "ger"), kw.getDefinitions().get("ger"));
        assertEquals(createBlahLabel(4, "ita"), kw.getValues().get("ita"));
        assertEquals(createBlahNote(4, "eng"), kw.getDefinitions().get("eng"));
        assertEquals("", kw.getDefinitions().get("chi"));
        
        kw = searcher.searchById(FOO_COM_NS+"4", thesaurusBlah.getKey(), "eng");

        assertNull(kw);
    }

    @Test
    public void testSearchNoContextEngLangNoSearchAllThesauri() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addLang("eng");
        searcher.search(params.build());
        assertSearchNoContextEngLangNoSearchAllThesauri(searcher);
        
        searcher.search(null, params.toXmlParams());
        assertSearchNoContextEngLangNoSearchAllThesauri(searcher);
    }

    private void assertSearchNoContextEngLangNoSearchAllThesauri(KeywordsSearcher searcher) {
        for (KeywordBean word : searcher.getResults()) {
            assertEquals("eng", word.getDefaultLang());
            assertEquals(1, word.getValues().size());
            assertEquals(1, word.getDefinitions().size());
        }
        assertEquals(keywords+smallWords+smallWords , searcher.getNbResults());
    }

    @Test
    public void testSearchNoContextMultiLangNoSearchAllThesauri() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addLang("fre")
            .addLang("eng")
            .addLang("chi");
        searcher.search(params.build());
        assertSearchNoContextMultiLangNoSearchAllThesauri(searcher);
        
        searcher.search(null, params.toXmlParams());
        assertSearchNoContextMultiLangNoSearchAllThesauri(searcher);
    }

    private void assertSearchNoContextMultiLangNoSearchAllThesauri(KeywordsSearcher searcher) {
        for (KeywordBean word : searcher.getResults()) {
            assertEquals("fre", word.getDefaultLang());
            assertTrue(word.getValues().keySet().containsAll(Arrays.asList("fre","eng","chi")));
            assertEquals(3, word.getValues().size());
            assertEquals(3, word.getDefinitions().size());
        }
        assertEquals(keywords+smallWords+smallWords , searcher.getNbResults());
    }
        
    @Test
    public void testSearchNoContextMultiLangKeywordSearchAllThesauri() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .keyword("fooVal", KeywordSearchType.CONTAINS, false)
            .addLang("fre")
            .addLang("eng")
            .addLang("chi");
        searcher.search(params.build());
        assertSearchNoContextMultiLangSearchAllThesauri(searcher);
        
        searcher.search(null, params.toXmlParams());
        assertSearchNoContextMultiLangSearchAllThesauri(searcher);
    }

    private void assertSearchNoContextMultiLangSearchAllThesauri(KeywordsSearcher searcher) {
        for (KeywordBean word : searcher.getResults()) {
            assertEquals("fre", word.getDefaultLang());
            assertTrue(word.getValues().keySet().containsAll(Arrays.asList("fre","eng","chi")));
            assertEquals(thesaurusFoo.getKey(), word.getThesaurusKey());
            assertEquals(3, word.getValues().size());
            assertEquals(3, word.getDefinitions().size());
        }
        assertEquals(smallWords , searcher.getNbResults());
    }

    @Test
    public void testSearchNoContextMultiLangKeywordNoSearchOneCategory() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .thesauriDomainName(thesaurusBlah.getDname())
            .addLang("fre")
            .addLang("eng")
            .addLang("chi");
        searcher.search(params.build());
        assertSearchNoContextMultiLangNoSearchOneCategory(searcher);
        
        searcher.search(null, params.toXmlParams());
        assertSearchNoContextMultiLangNoSearchOneCategory(searcher);
    }

    private void assertSearchNoContextMultiLangNoSearchOneCategory(KeywordsSearcher searcher) {
        for (KeywordBean word : searcher.getResults()) {
            assertEquals(thesaurusBlah.getKey(), word.getThesaurusKey());
            assertEquals("fre", word.getDefaultLang());
            assertTrue(word.getValues().keySet().containsAll(Arrays.asList("fre","eng","chi")));
            assertEquals(3, word.getValues().size());
            assertEquals(3, word.getDefinitions().size());
        }
        assertEquals(smallWords , searcher.getNbResults());
    }

    @Test
    public void testSearchNoContextMultiLangKeywordSearchOneSpecificThesauri() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addThesaurus(thesaurusBlah.getKey())
            .addLang("fre")
            .addLang("eng")
            .addLang("chi");
        searcher.search(params.build());
        assertSearchNoContextMultiLangKeywordSearchOneSpecificThesauri(searcher);
        
        searcher.search(null, params.toXmlParams());
        assertSearchNoContextMultiLangKeywordSearchOneSpecificThesauri(searcher);
    }

    private void assertSearchNoContextMultiLangKeywordSearchOneSpecificThesauri(KeywordsSearcher searcher) {
        for (KeywordBean word : searcher.getResults()) {
            assertEquals(thesaurusBlah.getKey(), word.getThesaurusKey());
            assertEquals("fre", word.getDefaultLang());
            assertTrue(word.getValues().keySet().containsAll(Arrays.asList("fre","eng","chi")));
            assertEquals(3, word.getValues().size());
            assertEquals(3, word.getDefinitions().size());
        }
        assertEquals(smallWords , searcher.getNbResults());
    }

    @Test
    public void testSearchNoContextMultiLangKeywordSearchTwoSpecificThesauri() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addThesaurus(thesaurusBlah.getKey())
            .addThesaurus(thesaurusFoo.getKey())
            .addLang("fre")
            .addLang("eng")
            .addLang("chi");
        searcher.search(params.build());
        assertSearchNoContextMultiLangKeywordSearchTwoSpecificThesauri(searcher);
        
        searcher.search(null, params.toXmlParams());
        assertSearchNoContextMultiLangKeywordSearchTwoSpecificThesauri(searcher);
    }

    private void assertSearchNoContextMultiLangKeywordSearchTwoSpecificThesauri(KeywordsSearcher searcher) {
        for (KeywordBean word : searcher.getResults()) {
            assertEquals("fre", word.getDefaultLang());
            assertTrue(word.getValues().keySet().containsAll(Arrays.asList("fre","eng","chi")));
            assertEquals(3, word.getValues().size());
            assertEquals(3, word.getDefinitions().size());
        }
        assertEquals(smallWords*2, searcher.getNbResults());
    }
    @Test
    public void testSearchContextMultiLangKeywordNoSearchTwoSpecificThesauri() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addThesaurus(thesaurusBlah.getKey())
            .addThesaurus(thesaurusFoo.getKey())
            .addLang("fre")
            .addLang("eng")
            .addLang("chi");
        searcher.search("ger" , params.toXmlParams());
        assertSearchNoContextMultiLangKeywordSearchTwoSpecificThesauri(searcher);
    }

    @Test
    public void testSearchUriSearch() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .uri(THESAURUS_KEYWORD_NS+30)
            .addLang("fre")
            .addLang("eng")
            .addLang("chi");
        searcher.search(params.build());
        assertSearchUriSearch(searcher);
        
        searcher.search(null, params.toXmlParams());
        assertSearchUriSearch(searcher);
    }

    private void assertSearchUriSearch(KeywordsSearcher searcher) {
        assertEquals(1, searcher.getNbResults());
        KeywordBean word = searcher.getResults().get(0);
        assertEquals(THESAURUS_KEYWORD_NS+30, word.getUriCode());
        assertEquals(thesaurus.getKey(), word.getThesaurusKey());
        assertEquals("fre", word.getDefaultLang());
        assertTrue(word.getValues().keySet().containsAll(Arrays.asList("fre","eng","chi")));
        assertEquals(3, word.getValues().size());
        assertEquals(3, word.getDefinitions().size());
    }

    @Test
    public void testSearchMaxResults() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addLang("eng")
            .maxResults(5);
        searcher.search(params.build());
        assertEquals(5, searcher.getNbResults());
    }
    @Test
    public void testSearchOffest() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addLang("eng")
            .offset(5);
        try {
            params.build();
            fail("Expected an IllegalStateException because offset is not allowed if no thesaurus is defined");
        } catch (IllegalStateException e) {
            // good
        }
        params.addThesaurus(thesaurus.getKey());
        searcher.search(params.build());
        assertEquals(keywords-5, searcher.getNbResults());
        
        params.addThesaurus(thesaurusBlah.getKey());
        try {
            params.build();
            fail("Expected an IllegalStateException because offset is not allowed if more than one thesaurus");
        } catch (IllegalStateException e) {
            // good
        }
        
    }
    @Test
    public void testSearchOffestMaxResults() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addLang("eng")
            .offset(5)
            .addThesaurus(thesaurus.getKey())
            .maxResults(5);
        searcher.search(params.build());
        assertEquals(5, searcher.getNbResults());
        assertEquals("5", searcher.getResults().get(0).getRelativeCode());
    }
    @Test
    public void testSearchContextNoLangKeywordNoSearchTwoSpecificThesauri() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addThesaurus(thesaurusFoo.getKey())
            .addThesaurus(thesaurusBlah.getKey());
        
        searcher.search("ger" , params.toXmlParams());

        for (KeywordBean word : searcher.getResults()) {
            assertEquals("ger", word.getDefaultLang());
            assertTrue(word.getValues().keySet().containsAll(Arrays.asList("ger")));
            assertEquals(1, word.getValues().size());
            assertEquals(1, word.getDefinitions().size());
        }
        assertEquals(smallWords*2, searcher.getNbResults());
    
    }

    @Test
    public void testsearchForRelatedElementString() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);

        Element params = new Element("params");
        params.addContent(new Element("id").setText(THESAURUS_KEYWORD_NS+20));
        params.addContent(new Element("thesaurus").setText(thesaurus.getKey()));
        searcher.searchForRelated(params , KeywordRelation.BROADER, "eng", "fre");
        
        assertEquals(1, searcher.getNbResults());
        assertEquals(""+15, searcher.getResults().get(0).getRelativeCode());
        assertTrue(searcher.getResults().get(0).getValues().keySet().contains("eng"));
        assertTrue(searcher.getResults().get(0).getValues().keySet().contains("fre"));
        assertEquals(2, searcher.getResults().get(0).getValues().size());
    }

    @Test
    public void testsearchForRelated() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        
        // tests narrower
        searcher.searchForRelated(THESAURUS_KEYWORD_NS+20, thesaurus.getKey(), KeywordRelation.BROADER, "eng", "fre");
        assertEquals(1, searcher.getNbResults());
        assertEquals(""+15, searcher.getResults().get(0).getRelativeCode());
        assertTrue(searcher.getResults().get(0).getValues().keySet().contains("eng"));
        assertTrue(searcher.getResults().get(0).getValues().keySet().contains("fre"));
        assertEquals(2, searcher.getResults().get(0).getValues().size());
        
        // tests broader
        searcher.searchForRelated(THESAURUS_KEYWORD_NS+15, thesaurus.getKey(), KeywordRelation.NARROWER, "eng", "ger");
        assertEquals(1, searcher.getNbResults());
        assertEquals(""+20, searcher.getResults().get(0).getRelativeCode());
        assertTrue(searcher.getResults().get(0).getValues().keySet().contains("eng"));
        assertTrue(searcher.getResults().get(0).getValues().keySet().contains("ger"));
        assertEquals(2, searcher.getResults().get(0).getValues().size());
        
        // tests related
        searcher.searchForRelated(THESAURUS_KEYWORD_NS+25, thesaurus.getKey(), KeywordRelation.RELATED, "eng");
        assertEquals(1, searcher.getNbResults());
        assertEquals(""+22, searcher.getResults().get(0).getRelativeCode());
        assertTrue(searcher.getResults().get(0).getValues().keySet().contains("eng"));
        assertEquals(1, searcher.getResults().get(0).getValues().size());
        
    }

    @Test
    public void testSortResults() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addLang("eng")
            .maxResults(10);
        searcher.search(params.build());
        searcher.sortResults(KeywordSort.defaultLabelSorter(SortDirection.ASC));
        
        for(int i=0; i < searcher.getNbResults() - 2; i++ ){
            String val1 = searcher.getResults().get(i).getDefaultValue();
            String val2 = searcher.getResults().get(i+1).getDefaultValue();
            assertTrue(val1.compareToIgnoreCase(val2) >= 0);
        }
        searcher.sortResults(KeywordSort.defaultLabelSorter(SortDirection.DESC));
        
        for(int i=0; i < searcher.getNbResults() - 2; i++ ){
            String val1 = searcher.getResults().get(i).getDefaultValue();
            String val2 = searcher.getResults().get(i+1).getDefaultValue();
            assertTrue(val1.compareToIgnoreCase(val2) <= 0);
        }
    }

    @Test
    public void testSelectUnselectKeywords() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addLang("eng")
            .maxResults(10);
        searcher.search(params.build());
        Element selectParams = new Element("params");
        Set<String> ids = new HashSet<String>();
        for(int i = 0; i < 5; i++ ) {
            KeywordBean bean = searcher.getResults().get(i);
            if(i%2 == 0) {
                ids.add(bean.getUriCode());
            } else {
                ids.add(""+bean.getId());
            }
        }
        
        for (String string : ids) {
            selectParams.addContent(new Element("pIdKeyword").setText(string));

        }
        searcher.selectUnselectKeywords(selectParams);
        
        assertEquals(5, searcher.getSelectedKeywordsInList().size());
        assertEquals(5, searcher.getSelectedKeywordsAsXml().getChildren("keyword").size());
        
        searcher.selectUnselectKeywords(selectParams);
        
        assertEquals(0, searcher.getSelectedKeywordsInList().size());
        assertEquals(0, searcher.getSelectedKeywordsAsXml().getChildren("keyword").size());
        
        searcher.selectUnselectKeywords(ids);
        assertEquals(5, searcher.getSelectedKeywordsInList().size());

        searcher.selectUnselectKeywords(ids);
        assertEquals(0, searcher.getSelectedKeywordsInList().size());

        searcher.selectUnselectKeywords(ids);
        searcher.clearSelection();
        assertEquals(0, searcher.getSelectedKeywordsInList().size());
    }

    @Test
    public void testExistsResult() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        KeywordSearchParamsBuilder params = new KeywordSearchParamsBuilder(isoLangMapper)
            .addLang("eng")
            .maxResults(10);
        searcher.search(params.build());
        
        assertEquals(2, searcher.getKeywordFromResultsById(2).getId());
        assertNotNull(searcher.getKeywordFromResultsByUriCode(searcher.getResults().get(4).getUriCode()).getId());
        assertNull(searcher.getKeywordFromResultsById(100));
    }

    @Test
    public void testKeywordSearchUsingId() throws Exception {
        KeywordsSearcher searcher = new KeywordsSearcher(isoLangMapper, thesaurusFinder);
        String keywordId = FOO_COM_NS+1;
		Element params = new Element("params").
        		addContent(new Element("pNewSearch").setText("true")).
        		addContent(new Element("pTypeSearch").setText("2")).
        		addContent(new Element("pThesauri").setText(thesaurusFoo.getKey())).
        		addContent(new Element("pMode").setText("searchBox")).
        		addContent(new Element("maxResults").setText("50")).
        		addContent(new Element("pLanguage").setText("eng")).
        		addContent(new Element("pKeyword").setText(keywordId));
        searcher.search("fra", params);
        
        assertEquals(1, searcher.getResults().size());
        assertEquals(keywordId, searcher.getResults().get(0).getUriCode());
    }

}

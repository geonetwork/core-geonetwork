package org.fao.geonet.kernel;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.TermNotFoundException;
import org.fao.geonet.kernel.rdf.Query;
import org.fao.geonet.kernel.rdf.QueryBuilder;
import org.fao.geonet.kernel.rdf.Selectors;
import org.fao.geonet.kernel.rdf.Where;
import org.fao.geonet.kernel.rdf.Wheres;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.GraphException;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.config.ConfigurationException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ThesaurusTest extends AbstractThesaurusBasedTest {

    private Thesaurus writableThesaurus;
    private static final String TEST_KEYWORD = "http://test.com/keywords#testKeyword";

    @Before
    public void prepareEmptyThesaurus() throws ConfigurationException, IOException {
        Path file = this.thesaurusFile.getParent().resolve(ThesaurusTest.class.getSimpleName() + "_empyt.rdf");
        Files.deleteIfExists(file);

        this.writableThesaurus = new Thesaurus(isoLangMapper, file.getFileName().toString(), null, null, Geonet.CodeList.LOCAL,
                file.getFileName().toString(), file, null, true);
        writableThesaurus.initRepository();
    }
    
    @After
    public void deleteEmptyThesaurus() throws IOException {
        writableThesaurus.getRepository().shutDown();
        Files.deleteIfExists(writableThesaurus.getFile());
    }
    
    public ThesaurusTest() {
        super(false);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAddElementStringStringStringString() throws Exception {
        String label = "Hello";
        String note = "note";
        String code = "http://thesaurus.test#0";
        writableThesaurus.addElement(code, label, note, "eng");
        assertElement(1, code, label, note, "", "", "", "");

        label = "Hello2";
        note = "note2";
        code = "http://thesaurus.test#1";
        writableThesaurus.addElement(code, label, note, "en");
        assertElement(2, code, label, note, "", "", "", "");
    }

    private KeywordBean assertElement(int words, String code, String label, String note, String coordEast, String coordWest, String coordSouth, String coordNorth) throws GraphException, IOException, AccessDeniedException,
            MalformedQueryException, QueryEvaluationException {
        List<KeywordBean> keywords = QueryBuilder.keywordQueryBuilder(isoLangMapper, "eng", "fre").build().execute(writableThesaurus);
        assertEquals(words, keywords.size());
        KeywordBean keywordBean = keywords.get(keywords.size()-1);
        assertEquals(label, keywordBean.getDefaultValue());
        assertEquals(note, keywordBean.getDefaultDefinition());
        assertEquals("eng", keywordBean.getDefaultLang());
        assertTrue(keywordBean.getUriCode().endsWith(code));
        assertEquals(coordEast, keywordBean.getCoordEast());
        assertEquals(coordNorth, keywordBean.getCoordNorth());
        assertEquals(coordWest, keywordBean.getCoordWest());
        assertEquals(coordSouth, keywordBean.getCoordSouth());
        return keywordBean;
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testAddElementStringStringStringStringString() throws Exception {
        String label = "Hello";
        String note = "note";
        String code = "http://thesaurus.test#0";
        String coordEast = "12";
        String coordWest = "5";
        String coordNorth = "30";
        String coordSouth = "20";
        writableThesaurus.addElement(code, label, note, coordEast, coordWest, coordSouth, coordNorth, "eng");
        assertElement(1, code, label, note, coordEast, coordWest, coordSouth, coordNorth);
        label = "Hello2";
        note = "note2";
        code = "http://thesaurus.test#1";
        writableThesaurus.addElement(code, label, note, coordEast, coordWest, coordSouth, coordNorth, "eng");
        assertElement(2, code, label, note, coordEast, coordWest, coordSouth, coordNorth);
    }

    @Test
    public void testAddElementKeywordBean() throws Exception {
        String label = "Hello";
        String note = "note";
        String code = "http://thesaurus.test#0";
        String coordEast = "12";
        String coordWest = "5";
        String coordNorth = "30";
        String coordSouth = "20";
        String lang = "eng";
        KeywordBean keyword = new KeywordBean(isoLangMapper)
            .setUriCode(code)
            .setCoordEast(coordEast)
            .setCoordNorth(coordNorth)
            .setCoordSouth(coordSouth)
            .setCoordWest(coordWest)
            .setDefinition(note, lang)
            .setValue(label, lang)
            .setValue("labelf", "fre");
        writableThesaurus.addElement(keyword);
        keyword = assertElement(1, code, label, note, coordEast, coordWest, coordSouth, coordNorth);
        assertEquals("labelf", keyword.getValues().get("fre"));
        
        label = "Hello2";
        note = "note2";
        code = "http://thesaurus.test#1";
        keyword = new KeywordBean(isoLangMapper)
            .setUriCode(code)
            .setDefinition(note, lang)
            .setValue(label, lang);
        writableThesaurus.addElement(keyword);
        keyword = assertElement(2, code, label, note, "", "", "", "");
        assertEquals("",keyword.getValues().get("fre"));
        assertEquals(label, keyword.getDefaultValue());
        assertEquals(note, keyword.getDefaultDefinition());
        assertEquals(lang, keyword.getDefaultLang());

        code = "http://thesaurus.test#2";
        keyword = new KeywordBean(isoLangMapper)
        .setUriCode(code)
        .setCoordEast(null)
        .setCoordNorth(null)
        .setCoordSouth(null)
        .setCoordWest(null)
        .setDefinition(note, lang)
        .setValue(label, lang);
    writableThesaurus.addElement(keyword);
    keyword = assertElement(3, code, label, note, "", "", "", "");

    }

    @Test
    public void testRemoveElementKeywordBean() throws Exception {
        KeywordBean keyword = QueryBuilder.keywordQueryBuilder(isoLangMapper, "eng").limit(1).build().execute(thesaurus).get(0);
        Where idMatches = Wheres.ID(keyword.getUriCode());
        Query<KeywordBean> query = QueryBuilder.keywordQueryBuilder(isoLangMapper, "eng").where(idMatches).build();
        assertEquals(1, query.execute(thesaurus).size());
        thesaurus.removeElement(keyword);
        assertEquals(0, query.execute(thesaurus).size());
    }

    @Test
    public void testRemoveElementStringString() throws Exception {
        KeywordBean keyword = QueryBuilder.keywordQueryBuilder(isoLangMapper, "eng").limit(1).build().execute(thesaurus).get(0);
        Where idMatches = Wheres.ID(keyword.getUriCode());
        Query<KeywordBean> query = QueryBuilder.keywordQueryBuilder(isoLangMapper, "eng").where(idMatches).build();
        assertEquals(1, query.execute(thesaurus).size());
        
        thesaurus.removeElement(keyword.getNameSpaceCode(), keyword.getRelativeCode());
        assertEquals(0, query.execute(thesaurus).size());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUpdateElementStringStringStringStringString() throws Exception {
        String label = "Hello";
        String note = "note";
        String code = "http://thesaurus.test#0";
        String coordEast = "12";
        String coordWest = "5";
        String coordNorth = "30";
        String coordSouth = "20";
        String lang = "eng";
        KeywordBean keyword = new KeywordBean(isoLangMapper)
            .setUriCode(code)
            .setCoordEast(coordEast)
            .setCoordNorth(coordNorth)
            .setCoordSouth(coordSouth)
            .setCoordWest(coordWest)
            .setDefinition(note, lang)
            .setValue(label, lang);

        writableThesaurus.addElement(keyword);
        assertElement(1, code, label, note, coordEast, coordWest, coordSouth, coordNorth);
        label = "Hello2";
        note = "note2";
        writableThesaurus.updateElement(keyword.getNameSpaceCode(), keyword.getRelativeCode(), label, note, "eng");
        assertElement(1, code, label, note, coordEast, coordWest, coordSouth, coordNorth);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUpdateElementStringStringStringStringStringStringStringStringString() throws Exception {
        String label = "Hello";
        String note = "note";
        String code = "http://thesaurus.test#0";
        String coordEast = "12";
        String coordWest = "5";
        String coordNorth = "30";
        String coordSouth = "20";
        String lang = "eng";
        KeywordBean keyword = new KeywordBean(isoLangMapper)
            .setUriCode(code)
            .setCoordEast(coordEast)
            .setCoordNorth(coordNorth)
            .setCoordSouth(coordSouth)
            .setCoordWest(coordWest)
            .setDefinition(note, lang)
            .setValue(label, lang);

        writableThesaurus.addElement(keyword);
        assertElement(1, code, label, note, coordEast, coordWest, coordSouth, coordNorth);
        label = "Hello2";
        note = "note2";
        coordEast = "0";
        coordWest = "-10";
        coordNorth = "20";
        coordSouth = "10";
        writableThesaurus.updateElement(keyword.getNameSpaceCode(), keyword.getRelativeCode(), label, note, coordEast, coordWest, coordSouth, coordNorth, "eng");
        assertElement(1, code, label, note, coordEast, coordWest, coordSouth, coordNorth);
    }

    @Test
    public void testUpdateElementKeywordBean() throws Exception {
        String label = "Hello";
        String note = "note";
        String code = "http://thesaurus.test#0";
        String coordEast = "12";
        String coordWest = "5";
        String coordNorth = "30";
        String coordSouth = "20";
        String lang = "eng";
        KeywordBean keyword = new KeywordBean(isoLangMapper)
            .setUriCode(code)
            .setCoordEast(coordEast)
            .setCoordNorth(coordNorth)
            .setCoordSouth(coordSouth)
            .setCoordWest(coordWest)
            .setDefinition(note, lang)
            .setValue(label, lang)
            .setDefinition("deff", "fre")
            .setValue("labelf", "fre");
        writableThesaurus.addElement(keyword);
        assertElement(1, code, label, note, coordEast, coordWest, coordSouth, coordNorth);
        
        coordEast = "0";
        coordWest = "-10";
        coordNorth = "20";
        coordSouth = "10";
        label = "Hello2";
        note = "note2";
        keyword = new KeywordBean(isoLangMapper)
            .setUriCode(code)
            .setCoordEast(coordEast)
            .setCoordNorth(coordNorth)
            .setCoordSouth(coordSouth)
            .setCoordWest(coordWest)
            .setDefinition(note, lang)
            .setValue(label, lang);
        
        writableThesaurus.updateElement(keyword, false);
        keyword = assertElement(1, code, label, note, coordEast, coordWest, coordSouth, coordNorth);
        assertEquals("labelf", keyword.getValues().get("fre"));
        assertEquals("deff", keyword.getDefinitions().get("fre"));
        
        label = "Hello3";
        note = "note3";
        keyword = new KeywordBean(isoLangMapper)
            .setUriCode(code)
            .setCoordEast("")
            .setCoordNorth("")
            .setCoordSouth("")
            .setCoordWest("")
            .setDefinition(note, lang)
            .setValue(label, lang);
        
        writableThesaurus.updateElement(keyword, false);
        keyword = assertElement(1, code, label, note, coordEast, coordWest, coordSouth, coordNorth);
        assertEquals("labelf", keyword.getValues().get("fre"));
        assertEquals("deff", keyword.getDefinitions().get("fre"));

        label = "Hello4";
        note = "note4";
        keyword = new KeywordBean(isoLangMapper)
            .setUriCode(code)
            .setCoordEast("")
            .setCoordNorth("")
            .setCoordSouth("")
            .setCoordWest("")
            .setDefinition(note, lang)
            .setValue(label, lang);
        
        writableThesaurus.updateElement(keyword, true);
        keyword = assertElement(1, code, label, note, "", "", "", "");
        assertEquals("", keyword.getValues().get("fre"));
        assertEquals("", keyword.getDefinitions().get("fre"));

        label = "Hello5";
        note = "note5";
        keyword = new KeywordBean(isoLangMapper)
            .setUriCode(code)
            .setCoordEast(null)
            .setCoordNorth(null)
            .setCoordSouth(null)
            .setCoordWest(null)
            .setDefinition(note, lang)
            .setValue(label, lang);
        
        writableThesaurus.updateElement(keyword, true);
        keyword = assertElement(1, code, label, note, "", "", "", "");
    }

    @Test
    public void testIsFreeCode() throws Exception {
        Query<KeywordBean> query = QueryBuilder.keywordQueryBuilder(isoLangMapper, "eng").limit(1).build();
        KeywordBean keyword = query.execute(thesaurus).get(0);

        assertFalse(thesaurus.isFreeCode(keyword.getNameSpaceCode(), keyword.getRelativeCode()));
        assertTrue(writableThesaurus.isFreeCode(keyword.getNameSpaceCode(), keyword.getRelativeCode()));
    }

    @Test
    public void testUpdateCode() throws Exception {
        String label = "Hello";
        String note = "note";
        String code = "http://thesaurus.test#0";
        String lang = "eng";
        KeywordBean keyword = new KeywordBean(isoLangMapper)
            .setUriCode(code)
            .setDefinition(note, lang)
            .setValue(label, lang);
        
        writableThesaurus.addElement(keyword);

        QueryBuilder<KeywordBean> builder = QueryBuilder.keywordQueryBuilder(isoLangMapper, "eng").where(Wheres.ID(code));
        assertEquals(1, builder.build().execute(writableThesaurus).size());
        
        writableThesaurus.updateCode(keyword.getNameSpaceCode(), keyword.getRelativeCode(), "10");
        assertEquals(0, builder.build().execute(writableThesaurus).size());
        
        builder.where(Wheres.ID(keyword.getNameSpaceCode()+10));
        assertEquals(1, builder.build().execute(writableThesaurus).size());
        
        keyword.setRelativeCode("10");
        writableThesaurus.updateCode(keyword, "15");
        assertEquals(0, builder.build().execute(writableThesaurus).size());
        
        builder.where(Wheres.ID(keyword.getNameSpaceCode()+15));
        assertEquals(1, builder.build().execute(writableThesaurus).size());
        
        writableThesaurus.updateCodeByURI(keyword.getNameSpaceCode()+15, "http://thesaurus.test#101");
        assertEquals(0, builder.build().execute(writableThesaurus).size());
        
        builder.where(Wheres.ID("http://thesaurus.test#101"));
        assertEquals(1, builder.build().execute(writableThesaurus).size());
        
        writableThesaurus.updateCodeByURI("http://thesaurus.test#101", "http://not.namespace.based.ids/101");
        assertEquals(0, builder.build().execute(writableThesaurus).size());
        
        builder.where(Wheres.ID("http://not.namespace.based.ids/101"));
        assertEquals(1, builder.build().execute(writableThesaurus).size());
        
        
    }
    
    @Test
    public void testAddRelationCode() throws Exception {
        String label = "Hello";
        String note = "note";
        String code1 = "http://thesaurus.test#0";
        String code2 = "http://thesaurus.test#1";
        String lang = "eng";
        KeywordBean keyword = new KeywordBean(isoLangMapper)
        .setUriCode(code1)
        .setDefinition(note, lang)
        .setValue(label, lang);
        writableThesaurus.addElement(keyword);

        keyword.setUriCode(code2);
        writableThesaurus.addElement(keyword);
        
        writableThesaurus.addRelation(code1, KeywordRelation.BROADER, code2);
        
        Query<KeywordBean> query = QueryBuilder.keywordQueryBuilder(isoLangMapper, "eng").
                select(Selectors.related(code2, KeywordRelation.BROADER), true).build();
        
        List<KeywordBean> result = query.execute(writableThesaurus);
        assertEquals(1, result.size());
        assertEquals(code1, result.get(0).getUriCode());
        
        query = QueryBuilder.keywordQueryBuilder(isoLangMapper, "eng").
                select(Selectors.related(code1, KeywordRelation.NARROWER), true).build();
        
        result = query.execute(writableThesaurus);
        assertEquals(1, result.size());
        assertEquals(code2, result.get(0).getUriCode());
    }

    @Test
    public void testHasConceptSchemeTrue() throws Exception {
        writableThesaurus.addTitleElement("testScheme");

        boolean hasConceptScheme = writableThesaurus.hasConceptScheme("http://geonetwork-opensource.org/testScheme");

        assertTrue(hasConceptScheme);
    }

    @Test
    public void testHasConceptSchemeFalse() throws Exception {
        writableThesaurus.addTitleElement("testScheme");

        boolean hasConceptScheme = writableThesaurus.hasConceptScheme("http://geonetwork-opensource.org/anotherScheme");

        assertFalse(hasConceptScheme);
    }

    @Test
    public void testGetKeywordFound() throws Exception {
        addKeywordToWritableThesaurus(TEST_KEYWORD);

        KeywordBean result = writableThesaurus.getKeyword(TEST_KEYWORD);

        assertEquals(result.getUriCode(), TEST_KEYWORD);
    }

    @Test(expected=TermNotFoundException.class)
    public void testGetKeywordNotFound() throws Exception {
        writableThesaurus.getKeyword("http://test.com/keywords#testKeyword");
    }

    @Test
    public void testHasKeyword() throws Exception {
        addKeywordToWritableThesaurus(TEST_KEYWORD);

        boolean result = writableThesaurus.hasKeyword(TEST_KEYWORD);

        assertTrue(result);
    }

    @Test
    public void testDoesntHavekeyword() throws Exception {
        boolean result = writableThesaurus.hasKeyword("http://test.com/keywords#testKeyword");

        assertFalse(result);
    }

    private void addKeywordToWritableThesaurus(String uri)
            throws IOException, AccessDeniedException, GraphException {
        KeywordBean keyword = new KeywordBean(isoLangMapper);
        keyword.setUriCode(uri);
        writableThesaurus.addElement(keyword);
    }
}

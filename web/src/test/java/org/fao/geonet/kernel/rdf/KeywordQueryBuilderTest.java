package org.fao.geonet.kernel.rdf;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.fao.geonet.kernel.AbstractThesaurusBasedTest;
import org.fao.geonet.kernel.KeywordBean;
import org.junit.Test;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;

public class KeywordQueryBuilderTest extends AbstractThesaurusBasedTest {

    public KeywordQueryBuilderTest() {
        super(true);
    }

    @Test
    public void keywords() throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        Query<KeywordBean> query = QueryBuilder.keywordQueryBuilder(isoLangMapper, Arrays.asList(languages)).build();
        List<KeywordBean> results = query.execute(thesaurus);
        
        assertCorrectKeywords(results);
    }

    private void assertCorrectKeywords(List<KeywordBean> results) {
        assertEquals(keywords, results.size());
        KeywordBean keywordBean = results.get(0);
        assertTrue(keywordBean.getUriCode().endsWith("#0"));
        assertEquals(createExampleLabel(0,"eng"), keywordBean.getValues().get("eng"));
        assertEquals(createExampleNote(0,"ger"), keywordBean.getDefinitions().get("ger"));
        assertEquals(4, keywordBean.getValues().size());
        assertEquals("0", keywordBean.getCoordEast());
        assertEquals("10", keywordBean.getCoordWest());
        assertEquals("5", keywordBean.getCoordSouth());
        assertEquals("15", keywordBean.getCoordNorth());

        KeywordBean keywordBean2 = results.get(1);
        
        assertTrue(keywordBean2.getUriCode().endsWith("#1"));
        assertEquals(createExampleLabel(1,"eng"), keywordBean2.getValues().get("eng"));
        assertEquals(createExampleNote(1,"ger"), keywordBean2.getDefinitions().get("ger"));
        assertEquals(4, keywordBean2.getValues().size());
        assertEquals("", keywordBean2.getCoordEast());
        assertEquals("", keywordBean2.getCoordWest());
        assertEquals("", keywordBean2.getCoordSouth());
        assertEquals("", keywordBean2.getCoordNorth());
    }

    @Test
    public void languages() throws Exception {
        Query<String> query = QueryBuilder.languagesQueryBuilder(isoLangMapper).build();
        List<String> readLanguages = query.execute(thesaurus);
        assertArrayEquals(languages, readLanguages.toArray(new String[readLanguages.size()]));
    }
    
    @Test
    public void keywordsCaseInsensitive() throws Exception {
        String[] upperCase = new String[languages.length];
        for (int i = 0; i < upperCase.length; i++) {
            upperCase[i] = languages[i].toUpperCase();
        }

        Query<KeywordBean> query = QueryBuilder.keywordQueryBuilder(isoLangMapper, Arrays.asList(upperCase)).build();
        List<KeywordBean> results = query.execute(thesaurus);
        
        assertCorrectKeywords(results);
    }
}

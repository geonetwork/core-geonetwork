package org.fao.geonet;

import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.fao.geonet.kernel.search.SearchManager;
import org.mockito.Mockito;

public class IndexedMetadataFetcherTest {

    private SearchManager searchManager = Mockito.mock(SearchManager.class);

    @Test
    public void parsingTreeField() throws IOException, JSONException {
        String toParse = IOUtils.toString(this.getClass().getResourceAsStream("ApplicationProfile.json"));
        IndexedMetadataFetcher toTest = new IndexedMetadataFetcher(searchManager);
        toTest.setIndex(toParse);

        List<String> treeField = toTest.getTreeField();

        String[] expected = new String[]{"PARAMETRES"};
        assertArrayEquals("parse error", expected, treeField.toArray());
    }

    @Test
    public void parsingTokenizedField() throws IOException, JSONException {
        String toParse = IOUtils.toString(this.getClass().getResourceAsStream("ApplicationProfile.json"));
        IndexedMetadataFetcher toTest = new IndexedMetadataFetcher(searchManager);
        toTest.setIndex(toParse);

        Map<String, String> tokenizedField = toTest.getTokenizedField();

        assertEquals("parse error", ";", tokenizedField.get("PROGRAMMES"));
        assertEquals("parse error", ";", tokenizedField.get("PARAMETRES"));
        assertEquals("parse error", ";", tokenizedField.get("SUPPORTS_NIVEAUX_PRELEVEMENT"));
    }
}

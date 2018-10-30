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

package org.fao.geonet.kernel;

import com.google.common.collect.Maps;

import org.fao.geonet.exceptions.TermNotFoundException;
import org.fao.geonet.kernel.search.keyword.KeywordRelation;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.utils.IO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.openrdf.sesame.repository.local.LocalRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AllThesaurusTest extends AbstractThesaurusBasedTest {
    public static final int SECOND_THES_WORDS = 5;
    private AllThesaurus allThesaurus;
    private Thesaurus secondThesaurus;
    private ThesaurusFinder thesaurusFinder;

    public AllThesaurusTest() {
        super(true);
    }

    @Before
    public void setUp() throws Exception {
        Path gcThesaurusFile = this.folder.getRoot().toPath().resolve("secondThesaurus.rdf");
        this.secondThesaurus = new Thesaurus(isoLangMapper, gcThesaurusFile.getFileName().toString(), null, null, "external",
            "local", gcThesaurusFile, "http://org.fao.geonet", true);
        final boolean thesauriExist = Files.exists(gcThesaurusFile);
        if (!thesauriExist) {
            String thesaurusName = "secondThesaurus";
            secondThesaurus.initRepository();
            populateThesaurus(secondThesaurus, SECOND_THES_WORDS, secondThesaurus.getDefaultNamespace(), thesaurusName, thesaurusName,
                "eng", "fre", "ger", "ita");
        } else {
            secondThesaurus.initRepository();
        }


        this.thesaurusFinder = new ThesaurusFinder() {

            @Override
            public boolean existsThesaurus(String name) {
                return getThesauriMap().containsKey(name);
            }

            @Override
            public Thesaurus getThesaurusByName(String thesaurusName) {
                return getThesauriMap().get(thesaurusName);
            }

            @Override
            public Thesaurus getThesaurusByConceptScheme(String conceptSchemeUri) {
                return null;
            }

            @Override
            public Map<String, Thesaurus> getThesauriMap() {
                Map<String, Thesaurus> thesauri = Maps.newHashMap();
                thesauri.put(secondThesaurus.getKey(), secondThesaurus);
                thesauri.put(AllThesaurus.ALL_THESAURUS_KEY, allThesaurus);
                thesauri.put(thesaurus.getKey(), thesaurus);

                return thesauri;
            }
        };

        this.allThesaurus = new AllThesaurus(thesaurusFinder, isoLangMapper, "http://test.com");
    }

    @Test
    public void testGetKey() throws Exception {
        assertEquals(AllThesaurus.ALL_THESAURUS_KEY, this.allThesaurus.getKey());
    }

    @Test
    public void testGetDname() throws Exception {
        assertEquals(AllThesaurus.DNAME, this.allThesaurus.getDname());
    }

    @Test
    public void testGetFname() throws Exception {
        assertEquals(AllThesaurus.FNAME, this.allThesaurus.getFname());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetFile() throws Exception {
        this.allThesaurus.getFile();
    }

    @Test
    public void testGetType() throws Exception {
        assertEquals(AllThesaurus.TYPE, this.allThesaurus.getType());
    }

    @Test
    public void testGetTitle() throws Exception {
        assertEquals(AllThesaurus.TITLE, this.allThesaurus.getTitle());
    }

    @Test
    public void testGetDate() throws Exception {
        assertNotNull(this.allThesaurus.getDate());

    }

    @Test
    public void testGetDownloadUrl() throws Exception {
        assertNotNull(this.allThesaurus.getDownloadUrl());
    }

    @Test
    public void testGetKeywordUrl() throws Exception {
        assertNotNull(this.allThesaurus.getKeywordUrl());
    }

    @Test
    public void testRetrieveThesaurusTitle() throws Exception {
        this.allThesaurus.retrieveThesaurusTitle(); // does nothing, assure there is no error
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetRepository() throws Exception {
        this.allThesaurus.getRepository();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSetRepository() throws Exception {
        this.allThesaurus.setRepository(Mockito.mock(LocalRepository.class));
    }

    @Test
    public void testInitRepository() throws Exception {
        this.allThesaurus.initRepository(); // does nothing, assure there is no error
    }

    @Test
    public void testPerformRequest() throws Exception {
        KeywordSearchParamsBuilder builder = new KeywordSearchParamsBuilder(isoLangMapper);
        builder.addLang("eng").maxResults(10).addThesaurus(AllThesaurus.ALL_THESAURUS_KEY);

        final List<KeywordBean> search = builder.build().search(this.thesaurusFinder);

        assertEquals(10, search.size());

        for (KeywordBean keywordBean : search) {
            assertAllKeywordBean(keywordBean);
        }
    }

    @Test
    public void testHasConceptScheme() throws Exception {
        assertFalse(this.allThesaurus.hasConceptScheme("any"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddElement() throws Exception {
        this.allThesaurus.addElement(new KeywordBean(isoLangMapper));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveElement() throws Exception {
        this.allThesaurus.removeElement(new KeywordBean(isoLangMapper));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveElement1() throws Exception {
        this.allThesaurus.removeElement("namespace", "code");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveElement2() throws Exception {
        this.allThesaurus.removeElement("uri");

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateElement() throws Exception {
        this.allThesaurus.updateElement(new KeywordBean(isoLangMapper), false);
    }

    @Test
    public void testIsFreeCode() throws Exception {
        final KeywordBean keywordBean = getExistingKeywordBean();
        assertFalse(this.allThesaurus.isFreeCode(keywordBean.getNameSpaceCode(), keywordBean.getRelativeCode()));

        assertTrue(this.allThesaurus.isFreeCode("non existant", "non existant"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateCode() throws Exception {
        this.allThesaurus.updateCode(new KeywordBean(isoLangMapper), "newcode");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateCode1() throws Exception {
        this.allThesaurus.updateCode("oldns", "oldcode", "newcode");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateCodeByURI() throws Exception {
        this.allThesaurus.updateCodeByURI("olduri", "newuri");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddTitleElement() throws Exception {
        this.allThesaurus.addTitleElement("newTitle");
    }

    @Test
    public void testGetIsoLanguageMapper() throws Exception {
        assertSame(isoLangMapper, this.allThesaurus.getIsoLanguageMapper());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddRelation() throws Exception {
        this.allThesaurus.addRelation("subject", KeywordRelation.RELATED, "relatedSubject");
    }

    @Test
    public void testGetKeyword() throws Exception {
        final KeywordBean existingKeywordBean = getExistingKeywordBean();
        final String uri = AllThesaurus.buildKeywordUri(existingKeywordBean);
        final KeywordBean keyword = this.allThesaurus.getKeyword(uri, "eng", "fre");
        assertAllKeywordBean(keyword);
    }

    @Test
    public void testHasKeyword() throws Exception {
        final KeywordBean existingKeywordBean = getExistingKeywordBean();
        final String uri = AllThesaurus.buildKeywordUri(existingKeywordBean);

        assertTrue(this.allThesaurus.hasKeyword(uri));
    }

    @Test
    public void testGetRelated() throws Exception {
        final String regionsPath = "/org/fao/geonet/services/region/external/thesauri/place/regions.rdf";
        Path regionsFile = Paths.get(AllThesaurusTest.class.getResource(regionsPath).toURI());
        final Thesaurus regionsThesaurus = new Thesaurus(isoLangMapper, regionsFile.getFileName().toString(), null, null, "external",
            "place", regionsFile, "http://org.fao.geonet", false);
        regionsThesaurus.initRepository();
        final HashMap<String, Thesaurus> thesauri = Maps.newHashMap();
        thesauri.put(regionsThesaurus.getKey(), regionsThesaurus);

        this.thesaurusFinder = Mockito.mock(ThesaurusFinder.class);
        Mockito.when(thesaurusFinder.getThesauriMap()).thenReturn(thesauri);
        Mockito.when(thesaurusFinder.getThesaurusByName(regionsThesaurus.getKey())).thenReturn(regionsThesaurus);
        Mockito.when(thesaurusFinder.existsThesaurus(regionsThesaurus.getKey())).thenReturn(true);

        this.allThesaurus = new AllThesaurus(thesaurusFinder, isoLangMapper, "http://blah.com");
        thesauri.put(AllThesaurus.ALL_THESAURUS_KEY, this.allThesaurus);

        final KeywordBean country = regionsThesaurus.getKeyword("http://geonetwork-opensource.org/regions#country", "eng");
        String countryAllURI = AllThesaurus.buildKeywordUri(country);

        assertRelated(countryAllURI, KeywordRelation.BROADER, 278);
        assertRelated(countryAllURI, KeywordRelation.NARROWER, 0);
        assertRelated(countryAllURI, KeywordRelation.RELATED, 0);

        final KeywordBean brazil = regionsThesaurus.getKeyword("http://geonetwork-opensource.org/regions#21", "eng");
        String brazilAllURI = AllThesaurus.buildKeywordUri(brazil);
        assertRelated(brazilAllURI, KeywordRelation.BROADER, 0);
        assertRelated(brazilAllURI, KeywordRelation.NARROWER, 1);
        assertRelated(brazilAllURI, KeywordRelation.RELATED, 0);
    }

    private void assertRelated(String countryAllURI, KeywordRelation relation, int numCountries) {
        final List<KeywordBean> related = this.allThesaurus.getRelated(countryAllURI, relation, "eng");
        assertEquals(numCountries, related.size());
        for (KeywordBean keywordBean : related) {
            assertAllKeywordBean(keywordBean);
        }
    }

    @Test
    public void testHasKeywordWithLabel() throws Exception {
        final KeywordBean existingKeywordBean = getExistingKeywordBean();

        assertTrue(this.allThesaurus.hasKeywordWithLabel(existingKeywordBean.getPreferredLabel("eng"), "eng"));
        assertFalse(this.allThesaurus.hasKeywordWithLabel("xyz dfdf eesd", "eng"));
    }

    @Test
    public void testGetKeywordWithLabel() throws Exception {
        final KeywordBean existingKeywordBean = getExistingKeywordBean();

        final KeywordBean found = this.allThesaurus.getKeywordWithLabel(existingKeywordBean.getPreferredLabel("eng"), "eng");
        assertAllKeywordBean(found);
    }

    @Test(expected = TermNotFoundException.class)
    public void testGetKeywordWithLabelCantFind() throws Exception {
        this.allThesaurus.getKeywordWithLabel("jkdjfdklsdj", "eng");
    }

    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void testAddElement1() throws Exception {
        this.allThesaurus.addElement("code", "prefLab", "note", "lang");
    }

    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void testAddElement2() throws Exception {
        this.allThesaurus.addElement("code", "prefLab", "note", "east", "west", "south", "north", "lang");

    }

    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateElement1() throws Exception {
        this.allThesaurus.updateElement("namespace", "id", "prefLab", "note", "lang");
    }

    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateElement2() throws Exception {
        this.allThesaurus.updateElement("namespace", "id", "prefLab", "note", "east", "west",
            "south", "north", "lang");

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testClear() throws Exception {
        this.allThesaurus.clear();
    }

    @Test
    public void testGetDefaultNamespace() throws Exception {
        final String defaultNamespace = this.allThesaurus.getDefaultNamespace();
        boolean foundNS = false;
        for (Thesaurus thes : this.thesaurusFinder.getThesauriMap().values()) {
            foundNS |= thes.getDefaultNamespace().equals(defaultNamespace);
        }

        assertTrue(foundNS);
    }

    @Test
    public void testBuildKeywordUri() throws Exception {
        final KeywordBean existingKeywordBean = getExistingKeywordBean();

        String allUri = AllThesaurus.buildKeywordUri(existingKeywordBean);
        final AllThesaurus.DecomposedAllUri decomposedAllUri = new AllThesaurus.DecomposedAllUri(allUri);

        assertEquals(existingKeywordBean.getThesaurusKey(), decomposedAllUri.thesaurusKey);
        assertEquals(existingKeywordBean.getUriCode(), decomposedAllUri.keywordUri);
    }

    private KeywordBean getExistingKeywordBean() throws IOException, MalformedQueryException, QueryEvaluationException, AccessDeniedException {
        final KeywordSearchParamsBuilder builder = new KeywordSearchParamsBuilder(isoLangMapper);
        builder.addLang("eng").maxResults(1).addThesaurus(secondThesaurus.getKey());
        return builder.build().search(this.thesaurusFinder).get(0);
    }

    private void assertAllKeywordBean(KeywordBean keywordBean) {
        assertEquals(allThesaurus.getKey(), keywordBean.getThesaurusKey());
        assertEquals(allThesaurus.getDate(), keywordBean.getThesaurusDate());
        assertEquals(allThesaurus.getTitle(), keywordBean.getThesaurusTitle());
        assertEquals(allThesaurus.getType(), keywordBean.getThesaurusType());

        final AllThesaurus.DecomposedAllUri decomposedAllUri = new AllThesaurus.DecomposedAllUri(keywordBean.getUriCode());

        final String actualThesaurusKey = decomposedAllUri.thesaurusKey;
        assertFalse(actualThesaurusKey.equals(this.allThesaurus.getKey()));
        assertTrue(this.thesaurusFinder.existsThesaurus(actualThesaurusKey));

        String actualKeywordUri = decomposedAllUri.keywordUri;
        final Thesaurus actualThesaurus = this.thesaurusFinder.getThesaurusByName(actualThesaurusKey);
        final KeywordBean keyword = actualThesaurus.getKeyword(actualKeywordUri);

        assertNotNull(keyword);

        assertEquals(keywordBean.getUriCode(),
            this.allThesaurus.getKeyword(keywordBean.getUriCode(), "eng", "fre").getUriCode());
    }
}

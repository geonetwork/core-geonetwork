/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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
package org.fao.geonet.api.registries.vocabularies;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AllThesaurus;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.Thesaurus;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class KeywordsApiThesaurusTest extends AbstractServiceIntegrationTest {

    private static final String REGIONS = "external.place.regions";
    private static final String CONTINENT = "http://www.naturalearthdata.com/ne_admin#Continent";
    private static final String API = "/srv/api/registries/vocabularies";

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ThesaurusManager thesaurusManager;

    @Autowired
    private SettingManager settingManager;

    @Autowired
    private IsoLanguagesMapper isoLanguagesMapper;

    private MockMvc mockMvc;
    private Thesaurus localThesaurus;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @After
    public void cleanUp() throws Exception {
        settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, false);
        if (localThesaurus != null) {
            thesaurusManager.remove(localThesaurus.getKey());
            Files.deleteIfExists(localThesaurus.getFile());
        }
    }

    @Test
    public void getThesauri() throws Exception {
        mockMvc.perform(get(API).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].key", hasItem(REGIONS)))
            .andExpect(jsonPath("$[?(@.key == '" + REGIONS + "')].dname", hasItem("place")))
            .andExpect(jsonPath("$[?(@.key == '" + REGIONS + "')].title", hasItem(not(isEmptyOrNullString()))));
    }

    @Test
    public void getTopConceptsAndConcept() throws Exception {
        mockMvc.perform(get(API + "/" + REGIONS + "/concepts/top").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uri", is(REGIONS)))
            .andExpect(jsonPath("$.thesaurus", is(REGIONS)))
            .andExpect(jsonPath("$.narrower[*].uri", hasItem(CONTINENT)));

        mockMvc.perform(get(API + "/" + REGIONS + "/concept").param("id", CONTINENT).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uri", is(CONTINENT)))
            .andExpect(jsonPath("$.prefLabel.eng", is("Continents")))
            .andExpect(jsonPath("$.narrower[*].prefLabel.eng", hasItem("Africa")));

        mockMvc.perform(get(API + "/unknown.thesaurus/concepts/top").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void getRelated() throws Exception {
        mockMvc.perform(get(API + "/" + REGIONS + "/keyword/related")
                .param("id", CONTINENT).param("type", "narrower").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].value", hasItem("Africa")));

        mockMvc.perform(get(API + "/" + REGIONS + "/keyword/related")
                .param("id", CONTINENT).param("type", "sibling").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void keywordLifecycle() throws Exception {
        localThesaurus = createLocalThesaurus();
        String url = API + "/" + localThesaurus.getKey() + "/keyword";
        MockHttpSession session = loginAsAdmin();
        String uri = "http://www.example.org/thesaurus#1";

        // Characters that broke the former XML request body
        mockMvc.perform(json(post(url), "{\"uri\": \"" + uri + "\", \"labels\": {\"eng\": \"Rivers & <lakes>\"}, " +
                "\"definitions\": {\"eng\": \"Water\"}}").session(session))
            .andExpect(status().isCreated());
        assertEquals("Rivers & <lakes>", localThesaurus.getKeyword(uri, "eng").getDefaultValue());

        mockMvc.perform(json(post(url), "{\"uri\": \"" + uri + "\", \"labels\": {\"eng\": \"Duplicate\"}}").session(session))
            .andExpect(status().isBadRequest());

        String newUri = "http://www.example.org/thesaurus#2";
        mockMvc.perform(json(put(url), "{\"uri\": \"" + newUri + "\", \"oldUri\": \"" + uri + "\", " +
                "\"labels\": {\"eng\": \"Rivers\"}}").session(session))
            .andExpect(status().isNoContent());
        assertTrue(localThesaurus.isFreeCode(null, uri));
        assertEquals("Rivers", localThesaurus.getKeyword(newUri, "eng").getDefaultValue());

        mockMvc.perform(delete(url).param("id", newUri).session(session))
            .andExpect(status().isNoContent());
        assertTrue(localThesaurus.isFreeCode(null, newUri));
        mockMvc.perform(delete(url).param("id", newUri).session(session))
            .andExpect(status().isNotFound());
    }

    // The following tests check the conversion of keywords to ISO19139 (convert.xsl).

    @Test
    public void keywordAsIso19139AllThesaurus() throws Exception {
        settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, true);
        List<String> uris = getExampleKeywordUris(AllThesaurus.ALL_THESAURUS_KEY, 2);

        Element keywordXml = getKeywordAsXml(AllThesaurus.ALL_THESAURUS_KEY, uris);

        assertEquals("gmd:descriptiveKeywords", keywordXml.getQualifiedName());
        for (int i = 0; i < uris.size(); i++) {
            String thesaurusKey = new AllThesaurus.DecomposedAllUri(uris.get(i)).thesaurusKey;
            assertEqualsText("thesaurus::" + thesaurusKey, keywordXml, "*//gmd:keyword[" + (i + 1) + "]/@gco:nilReason",
                ISO19139Namespaces.GCO, ISO19139Namespaces.GMD);
        }
    }

    @Test
    public void keywordAsIso19139TextGroupOnly() throws Exception {
        String thesaurusKey = findThesaurusWithKeywords(2);
        Element keywordXml = getKeywordAsXml(thesaurusKey, getExampleKeywordUris(thesaurusKey, 2), "textgroupOnly", "");

        assertEquals(2, Xml.selectNodes(keywordXml, "*//gmd:keyword/gco:CharacterString[normalize-space(text()) != '']",
            Arrays.asList(ISO19139Namespaces.GCO, ISO19139Namespaces.GMD)).size());
    }

    @Test
    public void keywordAsIso19139MdKeywords() throws Exception {
        String thesaurusKey = findThesaurusWithKeywords(2);
        Element keywordXml = getKeywordAsXml(thesaurusKey, getExampleKeywordUris(thesaurusKey, 2), "skipdescriptivekeywords", "");

        assertEquals("gmd:MD_Keywords", keywordXml.getQualifiedName());
        assertFalse(Xml.getString(keywordXml), Xml.selectNodes(keywordXml, "gmd:keyword/gco:CharacterString[normalize-space(text()) != '']",
            Arrays.asList(ISO19139Namespaces.GCO, ISO19139Namespaces.GMD)).isEmpty());
    }

    @Test
    public void keywordAsIso19139XlinkAllThesaurus() throws Exception {
        settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, true);
        List<String> uris = getExampleKeywordUris(AllThesaurus.ALL_THESAURUS_KEY, 2);

        Element keywordXml = getKeywordAsXml(AllThesaurus.ALL_THESAURUS_KEY, uris,
            "transformation", "to-iso19139-keyword-as-xlink", "textgroupOnly", "", "lang", "ger,fre,eng,ita");

        assertEquals("gmd:descriptiveKeywords", keywordXml.getQualifiedName());
        assertEquals(0, keywordXml.getChildren().size());
        assertNotNull(keywordXml.getAttributeValue("href", Geonet.Namespaces.XLINK));
    }

    private Element getKeywordAsXml(String thesaurusKey, List<String> uris, String... extraParams) throws Exception {
        MockHttpServletRequestBuilder request = get(API + "/keyword")
            .param("thesaurus", thesaurusKey)
            .param("id", String.join(",", uris))
            .param("multiple", "true")
            .param("lang", "eng,fre")
            .accept(MediaType.APPLICATION_XML);
        for (int i = 0; i < extraParams.length; i += 2) {
            request.param(extraParams[i], extraParams[i + 1]);
        }
        String xml = mockMvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return Xml.loadString(xml, false);
    }

    private String findThesaurusWithKeywords(int count) throws Exception {
        for (String key : thesaurusManager.getThesauriMap().keySet()) {
            if (getExampleKeywordUris(key, count).size() == count) {
                return key;
            }
        }
        throw new AssertionError("No thesaurus with " + count + " keywords in the test data");
    }

    private List<String> getExampleKeywordUris(String thesaurusKey, int count) throws Exception {
        List<KeywordBean> keywords = new KeywordSearchParamsBuilder(isoLanguagesMapper)
            .addThesaurus(thesaurusKey).maxResults(1000).addLang("eng").addLang("fre").build()
            .search(thesaurusManager);
        return keywords.stream()
            .filter(k -> !"".equals(k.getValues().get("eng")) && !"".equals(k.getValues().get("fre")))
            .limit(count)
            .map(KeywordBean::getUriCode)
            .collect(Collectors.toList());
    }

    private Thesaurus createLocalThesaurus() throws Exception {
        String fname = "keywords-api-test.rdf";
        Path rdfFile = thesaurusManager.buildThesaurusFilePath(fname, "local", "theme");
        Thesaurus thesaurus = new Thesaurus(isoLanguagesMapper, fname,
            Collections.singletonMap("eng", "Test thesaurus"), new java.util.HashMap<>(),
            "http://www.example.org/thesaurus", "local", "theme", rdfFile, "http://localhost/",
            false, thesaurusManager.getThesaurusCacheMaxSize());
        thesaurusManager.addThesaurus(thesaurus, true);
        return thesaurus;
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }
}

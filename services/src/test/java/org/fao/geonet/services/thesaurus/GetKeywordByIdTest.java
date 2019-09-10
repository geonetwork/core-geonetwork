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

package org.fao.geonet.services.thesaurus;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.AllThesaurus;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.KeywordBean;
import org.fao.geonet.kernel.ThesaurusManager;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParams;
import org.fao.geonet.kernel.search.keyword.KeywordSearchParamsBuilder;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.languages.IsoLanguagesMapper;
import org.fao.geonet.repository.IsoLanguageRepository;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.After;
import org.junit.Test;
import org.openrdf.sesame.config.AccessDeniedException;
import org.openrdf.sesame.query.MalformedQueryException;
import org.openrdf.sesame.query.QueryEvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GetKeywordByIdTest extends AbstractServiceIntegrationTest {
    @Autowired
    private ThesaurusManager thesaurusManager;
    @Autowired
    private SettingManager settingManager;
    @Autowired
    private IsoLanguagesMapper isoLanguagesMapper;
    @Autowired
    private GeonetworkDataDirectory geonetworkDataDirectory;
    @Autowired
    private IsoLanguageRepository languageRepository;

    @After
    public void tearDown2() throws Exception {
        settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, false);
    }

    @Test
    public void testExecAllThesaurus() throws Exception {
        final String thesaurusKey = AllThesaurus.ALL_THESAURUS_KEY;
        settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, true);
        final java.util.List<KeywordBean> keywordBeans = getExampleKeywords(thesaurusKey, 2);

        String uri1 = keywordBeans.get(0).getUriCode();
        String uri2 = keywordBeans.get(1).getUriCode();

        final Element keywordXml = getKeywordsById(uri1, uri2, thesaurusKey, Functions.<Element>identity());

        assertEquals("gmd:descriptiveKeywords", keywordXml.getQualifiedName());
        String thes1 = new AllThesaurus.DecomposedAllUri(uri1).thesaurusKey;
        assertEqualsText("thesaurus::" + thes1, keywordXml, "*//gmd:keyword[1]/@gco:nilReason", ISO19139Namespaces.GCO,
            ISO19139Namespaces.GMD);
        String thes2 = new AllThesaurus.DecomposedAllUri(uri2).thesaurusKey;
        assertEqualsText("thesaurus::" + thes2, keywordXml, "*//gmd:keyword[2]/@gco:nilReason", ISO19139Namespaces.GCO,
            ISO19139Namespaces.GMD);
    }

    @Test
    public void testExecTextGroupOnly() throws Exception {
        final Iterator<String> thesaurusIterator = getThesaurusKeys();
        int maxResults = 2;
        while(thesaurusIterator.hasNext()) {
            String thesaurusKey = thesaurusIterator.next();
            final java.util.List<KeywordBean> keywordBeans = getExampleKeywords(thesaurusKey, 2);

            // Skip thesaurus with no or non matching keywords
            if (keywordBeans.size() == maxResults) {
                String uri1 = keywordBeans.get(0).getUriCode();
                String uri2 = keywordBeans.get(1).getUriCode();

                final Element keywordXml = getKeywordsById(uri1, uri2, thesaurusKey, new Function<Element, Void>() {
                    @Nullable
                    @Override
                    public Void apply(Element input) {
                        input.addContent(new Element("textgroupOnly").setText(""));
                        return null;
                    }
                });

                final java.util.List<?> charStrings = Xml.selectNodes(keywordXml, "*//gmd:keyword/gco:CharacterString[normalize-space(text()) != '']", Arrays.asList
                    (ISO19139Namespaces.GCO,
                        ISO19139Namespaces.GMD));
                assertEquals(maxResults, charStrings.size());
                return;
            }
        }
    }

    @Test
    public void testExecMD_Keywords() throws Exception {
        final Iterator<String> thesaurusIterator = getThesaurusKeys();
        int maxResults = 2;
        while(thesaurusIterator.hasNext()) {
            String thesaurusKey = thesaurusIterator.next();
            final java.util.List<KeywordBean> keywordBeans = getExampleKeywords(thesaurusKey, maxResults);

            // Skip thesaurus with no or non matching keywords
            if (keywordBeans.size() == maxResults) {
                String uri1 = keywordBeans.get(0).getUriCode();
                String uri2 = keywordBeans.get(1).getUriCode();

                final Element keywordXml = getKeywordsById(uri1, uri2, thesaurusKey, new Function<Element, Void>() {
                    @Nullable
                    @Override
                    public Void apply(Element input) {
                        input.addContent(new Element("skipdescriptivekeywords").setText(""));
                        return null;
                    }
                });

                assertEquals("gmd:MD_Keywords", keywordXml.getQualifiedName());
                assertTrue(Xml.getString(keywordXml), Xml.selectNodes(keywordXml, "gmd:keyword/gco:CharacterString[normalize-space(text()) != '']", Arrays.asList(ISO19139Namespaces.GCO,
                    ISO19139Namespaces.GMD)).size() > 0);
                return;
            }
        }
    }

    @Test
    public void testExecMD_KeywordsAsXlinkAllThesaurus() throws Exception {
        settingManager.setValue(Settings.SYSTEM_ENABLE_ALL_THESAURUS, true);
        final String thesaurusKey = AllThesaurus.ALL_THESAURUS_KEY;
        final java.util.List<KeywordBean> keywordBeans = getExampleKeywords(thesaurusKey, 2);

        String uri1 = keywordBeans.get(0).getUriCode();
        String uri2 = keywordBeans.get(1).getUriCode();

        final Element keywordXml = getKeywordsById(uri1, uri2, thesaurusKey, new Function<Element, Void>() {
            @Nullable
            @Override
            public Void apply(Element input) {
                input.addContent(new Element("transformation").setText("to-iso19139-keyword-as-xlink"));
                input.addContent(new Element("textgroupOnly").setText(""));
                input.getChild("lang").setText("ger,fre,eng,ita");
                return null;
            }
        });

        assertEquals("gmd:descriptiveKeywords", keywordXml.getQualifiedName());
        assertEquals(0, keywordXml.getChildren().size());
        assertNotNull(keywordXml.getAttributeValue("href", Geonet.Namespaces.XLINK));
    }

    private Element getKeywordsById(String uri1, String uri2, String thesaurusKey,
                                    Function<Element, ?> updateRequestParams) throws Exception {
        final GetKeywordById service = new GetKeywordById();
        final ServiceContext context = createServiceContext();
        final Element requestParams = createParams(
            Pair.read("thesaurus", thesaurusKey),
            Pair.read("id", uri1 + "," + uri2),
            Pair.read("multiple", "true"),
            Pair.read("lang", "eng,fre"));
        updateRequestParams.apply(requestParams);
        requestParams.setName("request");

        Element thesaurusXml = new GetList().exec(createParams(), context).setName("thesaurus");
        final Element results = service.exec(requestParams, context);
        Element xmlForTransformation = new Element("root").addContent(Arrays.asList(
            new Element("gui").addContent(Arrays.asList(
                new Element("strings"),
                languageRepository.findAllAsXml(),
                thesaurusXml
            )),
            results,
            requestParams
            )
        );
        final Element keywordXml;
        try {
            keywordXml = Xml.transform(xmlForTransformation, geonetworkDataDirectory.getWebappDir().resolve
                ("xslt/services/thesaurus/convert.xsl"));
        } catch (Exception e) {
            throw new RuntimeException("Error transforming xml: " + Xml.getString(xmlForTransformation), e);
        }
        return keywordXml;
    }

    private java.util.List<KeywordBean> getExampleKeywords(String thesaurusKey, int maxResults) throws IOException, MalformedQueryException,
        QueryEvaluationException, AccessDeniedException {
        final KeywordSearchParams searchParams = new KeywordSearchParamsBuilder(isoLanguagesMapper).
            addThesaurus(thesaurusKey).
            maxResults(1000).
            addLang("eng").addLang("fre").
            build();

        final ArrayList<KeywordBean> finalResults = Lists.newArrayList();
        for (KeywordBean keywordBean : searchParams.search(thesaurusManager)) {
            if (finalResults.size() >= maxResults) {
                break;
            }
            if (!nonEmptyKeyword(keywordBean, "eng") && !nonEmptyKeyword(keywordBean, "fre")) {
                finalResults.add(keywordBean);
            }
        }
        return finalResults;
    }

    private boolean nonEmptyKeyword(KeywordBean keywordBean, String lang) {
        final String value = keywordBean.getValues().get(lang);
        return value != null && value.isEmpty();
    }

    private Iterator<String> getThesaurusKeys() {
        return thesaurusManager.getThesauriMap().keySet().iterator();
    }
}

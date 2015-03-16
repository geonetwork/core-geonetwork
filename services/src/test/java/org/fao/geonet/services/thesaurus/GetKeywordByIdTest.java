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
        settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, false);
    }

    @Test
    public void testExecAllThesaurus() throws Exception {
        final String thesaurusKey = AllThesaurus.ALL_THESAURUS_KEY;
        settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, true);
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
        final String thesaurusKey = firstThesaurusKey();
        final java.util.List<KeywordBean> keywordBeans = getExampleKeywords(thesaurusKey, 2);

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
        assertEquals(0, charStrings.size());
    }

    @Test
    public void testExecMD_Keywords() throws Exception {
        final String thesaurusKey = firstThesaurusKey();
        final java.util.List<KeywordBean> keywordBeans = getExampleKeywords(thesaurusKey, 2);

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
    }
    @Test
    public void testExecMD_KeywordsAsXlinkAllThesaurus() throws Exception {
        settingManager.setValue(SettingManager.ENABLE_ALL_THESAURUS, true);
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

    private String firstThesaurusKey() {
        return thesaurusManager.getThesauriMap().keySet().iterator().next();
    }
}

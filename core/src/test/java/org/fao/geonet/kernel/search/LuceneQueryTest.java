package org.fao.geonet.kernel.search;

import jeeves.server.ServiceConfig;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.search.Query;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.search.facet.Dimension;
import org.fao.geonet.kernel.search.facet.Facets;
import org.fao.geonet.kernel.search.facet.SummaryType;
import org.fao.geonet.kernel.search.facet.SummaryTypes;
import org.fao.geonet.utils.IO;
import org.jdom.DefaultJDOMFactory;
import org.jdom.Element;
import org.jdom.JDOMFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for building Lucene queries.
 *
 * @author heikki doeleman
 */
@ContextConfiguration(locations = "classpath:web-test-context.xml")
public class LuceneQueryTest {

    private Set<String> _tokenizedFieldSet;
    private Map<String, LuceneConfigNumericField> _numericFieldSet;
    private PerFieldAnalyzerWrapper _analyzer;
    private FacetsConfig _taxonomyConfiguration;

    @Before
    public void before() throws Exception {
        Map<String, Analyzer> analyzers = new HashMap<String, Analyzer>();
        analyzers.put("_uuid", new GeoNetworkAnalyzer());
        analyzers.put("parentUuid", new GeoNetworkAnalyzer());
        analyzers.put("operatesOn", new GeoNetworkAnalyzer());
        analyzers.put("subject", new KeywordAnalyzer());

        _analyzer = new PerFieldAnalyzerWrapper(new GeoNetworkAnalyzer(), analyzers);

        final String configFile = "/WEB-INF/config-lucene.xml";
        final Path appDir = IO.toPath(LuceneQueryTest.class.getResource(configFile).toURI()).getParent().getParent();
        final GeonetworkDataDirectory dataDirectory = new GeonetworkDataDirectory();
        dataDirectory.init("test", appDir, new ServiceConfig(), null);
        Facets facets = new Facets(new ArrayList<Dimension>());
        SummaryTypes summaryTypes = new SummaryTypes(new ArrayList<SummaryType>());
        LuceneConfig lc = new LuceneConfig(facets, summaryTypes);
        lc.geonetworkDataDirectory = dataDirectory;
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
        lc._appContext = context;
        context.refresh();
        lc.configure(configFile);

        _tokenizedFieldSet = lc.getTokenizedField();
        _numericFieldSet = lc.getNumericFields();
        _taxonomyConfiguration = lc.getTaxonomyConfiguration();
    }

    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testSingleORSingleValue() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("any_OR_title");
        OR.addContent("xxx");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(title:xxx any:xxx) +_isTemplate:n", query.toString());
    }

    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testMoreThanOneORParamSingleValue() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("inspiretheme_OR_title");
        OR.addContent("xxx");
        request.addContent(OR);

        Element OR2 = factory.element("any_OR_category");
        OR2.addContent("yyy");
        request.addContent(OR2);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(category:yyy inspiretheme:xxx title:xxx any:yyy) +_isTemplate:n", query.toString());
    }

    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testSingleORSingleValueAndANDparam() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("inspiretheme_OR_title");
        OR.addContent("xxx");
        request.addContent(OR);

        Element any = factory.element("any");
        any.addContent("yyy");
        request.addContent(any);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(inspiretheme:xxx title:xxx) +_isTemplate:n +any:yyy", query.toString());
    }

    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testSingleORMultipleValue() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("any_OR_title");
        OR.addContent("xxx zzz");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(title:xxx title:zzz (+any:xxx +any:zzz)) +_isTemplate:n", query.toString());

    }

    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testMultiORSingleValue() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("any_OR_title_OR_inspiretheme");
        OR.addContent("xxx");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(title:xxx inspiretheme:xxx any:xxx) +_isTemplate:n", query.toString());
    }


    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testMultiORMultipleValue() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("any_OR_title_OR_inspiretheme");
        OR.addContent("xxx zzz");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(title:xxx title:zzz inspiretheme:xxx zzz (+any:xxx +any:zzz)) +_isTemplate:n",
                query.toString());
    }

    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testMultiORWithSecurityField() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("any_OR_owner");
        OR.addContent("xxx");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(any:xxx) +_isTemplate:n", query.toString());
    }

    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testMultiORMultipleNonTokenizedValuesWithOr() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("any_OR_title_OR_inspiretheme");
        OR.addContent("xxx or zzz");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(title:xxx title:zzz inspiretheme:xxx inspiretheme:zzz any:xxx any:zzz) " +
                                                "+_isTemplate:n", query.toString());
    }

    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testSingleORSingleTokenWithUUID() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("_uuid_OR_title");
        OR.addContent("xxx");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(title:xxx _uuid:xxx) +_isTemplate:n", query.toString());
    }

    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testSingleORMultipleTokenWithUUID() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("_uuid_OR_title");
        OR.addContent("xxx or yyy");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(title:xxx title:yyy _uuid:xxx _uuid:yyy) +_isTemplate:n", query.toString());
    }

    @Test
    public void testSingleORWithBB() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element anyE = factory.element("eastBL_OR_title");
        anyE.addContent("55");
        request.addContent(anyE);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        // (no BBox supported for FIELD OR: query is stripped)
        assertEquals("unexpected Lucene query", "+_isTemplate:n", query.toString());
    }

    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testSingleORSingleValueWithALL() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("all_OR_title");
        OR.addContent("xxx");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(title:xxx any:xxx) +_isTemplate:n", query.toString());
    }


    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testSingleORMultiValueWithOR() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("or_OR_title");
        OR.addContent("xxx yyy");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        LuceneQueryBuilder luceneQueryBuilder = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null);
        Query query = luceneQueryBuilder.build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(title:xxx title:yyy (+any:xxx +any:yyy)) +_isTemplate:n", query.toString());
    }


    /**
     * Tests parameters for disjunctions. They are of the form paramA_OR_paramB.
     */
    @Test
    public void testSingleORMultiValueWithWithout() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element OR = factory.element("without_OR_title");
        OR.addContent("xxx yyy");
        request.addContent(OR);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        // (no 'without' supported for FIELD OR: query is stripped)
        assertEquals("unexpected Lucene query", "+_isTemplate:n", query.toString());
    }

    /**
     * 'phrase' parameter with a multiple token value.
     */
    @Test
    public void testSingleORWithMultipleTokenPhrase() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("phrase_OR_title");
        any.addContent("xxx yyy");
        request.addContent(any);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        // (no phrase queries supported for FIELD OR: query is stripped)
        assertEquals("unexpected Lucene query", "+_isTemplate:n", query.toString());
    }

    /**
     * 'phrase' parameter with a multiple token value.
     */
    @Test
    public void testSingleORWithTemporal() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("extFrom_OR_title");
        any.addContent("xxx yyy");
        request.addContent(any);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);

        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        // (no temporal queries supported for FIELD OR: query is stripped)
        assertEquals("unexpected Lucene query", "+_isTemplate:n", query.toString());
    }


    /**
     * 'any' parameter with a single token value.
     */
    @Test
    public void testSingleTokenAny() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("hoeperdepoep");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:hoeperdepoep +_isTemplate:n", query.toString());
    }

    /**
     * 'any' parameter with a single token value.
     */
    @Test
    public void testMultiAny() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("hoeperdepoep");
        request.addContent(any);
        Element any2 = factory.element("any");
        any2.addContent("demo");
        request.addContent(any2);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+any:demo +any:hoeperdepoep) +_isTemplate:n", query.toString());
    }

    @Test
    public void testSingleTokenQMarkWildcardAny() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("hoeper?poep");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:hoeper?poep +_isTemplate:n", query.toString());
    }

    /**
     * 'any' parameter with a single token value that has a wildcard.
     */
    @Test
    public void testSingleTokenWildcardAny() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("hoeper*poep");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:hoeper*poep +_isTemplate:n", query.toString());
    }

    /**
     * 'any' parameter with a single token value that has a wildcard.
     */
    @Test
    public void testSingleTokenWildcardWhitespace() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("hoeper *");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+any:hoeper +any:*) +_isTemplate:n", query.toString());
    }

    /**
     * 'any' parameter with a no value.
     */
    @Test
    public void testNoTokenAny() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_isTemplate:n", query.toString());
    }

    /**
     * 'any' parameter check case insensitivity..
     */
    @Test
    public void testSingleTokenAnyCaseInsensitive() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("hOeperdEpoeP");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:hoeperdepoep +_isTemplate:n", query.toString());
    }

    /**
     * 'any' parameter with a single token value and with fuzziness.
     */
    @Test
    public void testSingleTokenAnyFuzzy() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("asjemenou");
        request.addContent(any);
        Element similarity = factory.element("similarity");
        similarity.addContent("0.5740458015267176");
        request.addContent(similarity);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:asjemenou~2 +_isTemplate:n", query.toString());
    }

    /**
     * 'uuid' parameter with a single token value.
     */
    @Test
    public void testSingleTokenUUID() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element uuid = factory.element("uuid");
        uuid.addContent("ad2aa2c7-f099-47cb-8a38-4effe2a2d250");
        request.addContent(uuid);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_uuid:ad2aa2c7-f099-47cb-8a38-4effe2a2d250 +_isTemplate:n", query.toString());
    }

    /**
     * 'uuid' parameter with a double token value.
     */
    @Test
    public void testDoubleTokenUUID() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element uuid = factory.element("uuid");
        uuid.addContent("63C2378A-17A7-B863-BFF4-CC3EF507D10D or ad2aa2c7-f099-47cb-8a38-4effe2a2d250");
        request.addContent(uuid);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(_uuid:63C2378A-17A7-B863-BFF4-CC3EF507D10D " +
                                                "_uuid:ad2aa2c7-f099-47cb-8a38-4effe2a2d250) +_isTemplate:n", query.toString());
    }

    /**
     * 'uuid' parameter with a double token value.
     */
    @Test
    public void testDoubleTokenUUIDWithNonStandardUUID() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element uuid = factory.element("uuid");
        uuid.addContent("63C2378A-17A7-B863-BFF4-CC3EF507D10D or BAR_DEN");
        request.addContent(uuid);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(_uuid:63C2378A-17A7-B863-BFF4-CC3EF507D10D _uuid:BAR_DEN) +_isTemplate:n",
                query.toString());
    }


    /**
     * 'any' parameter with a single token value and with fuzziness 1.
     */
    @Test
    public void testSingleTokenAnyFuzzy1() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("asjemenou");
        request.addContent(any);
        Element similarity = factory.element("similarity");
        similarity.addContent("1");
        request.addContent(similarity);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:asjemenou +_isTemplate:n", query.toString());
    }

    /**
     * 'any' parameter with multiple token values.
     */
    @Test
    public void testMultipleTokensAny() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("deze die");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+any:deze +any:die) +_isTemplate:n", query.toString());
    }

    /**
     * 'any' parameter with multiple token values and with fuzziness.
     */
    @Test
    public void testMultipleTokensAnyFuzzy() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("bloody hell");
        request.addContent(any);
        Element similarity = factory.element("similarity");
        similarity.addContent("0.6885496183206108");
        request.addContent(similarity);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+any:bloody~2 +any:hell~2) +_isTemplate:n", query.toString());
    }

    /**
     * 'or' parameter with a single token value.
     */
    @Test
    public void testSingleTokenOr() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("or");
        any.addContent("hoeperdepoep");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(any:hoeperdepoep) +_isTemplate:n", query.toString());
    }

    /**
     * 'or' parameter with a multiple token value.
     */
    @Test
    public void testMultipleTokenOr() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("or");
        any.addContent("hoep poep");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(any:hoep any:poep) +_isTemplate:n", query.toString());
    }

    /**
     * 'or' parameter with a multiple token value and fuzziness.
     */
    @Test
    public void testMultipleTokenOrFuzzy() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("or");
        any.addContent("hoep poep");
        request.addContent(any);
        Element similarity = factory.element("similarity");
        similarity.addContent("0.5969465648854961");
        request.addContent(similarity);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(any:hoep~2 any:poep~2) +_isTemplate:n", query.toString());
    }

    /**
     * 'all' parameter with a single token value.
     */
    @Test
    public void testSingleTokenAll() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("all");
        any.addContent("hoeperdepoep");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:hoeperdepoep +_isTemplate:n", query.toString());
    }

    /**
     * 'all' parameter with multiple token values and with fuzziness.
     */
    @Test
    public void testMultipleTokensAllFuzzy() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("all");
        any.addContent("bloody hell");
        request.addContent(any);
        Element similarity = factory.element("similarity");
        similarity.addContent("0.6885496183206108");
        request.addContent(similarity);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+any:bloody~2 +any:hell~2) +_isTemplate:n", query.toString());
    }

    /**
     * 'without' parameter with a single token value.
     */
    @Test
    public void testSingleTokenWithout() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("without");
        any.addContent("hoeperdepoep");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        //assertEquals("+(+MatchAllDocsQuery -any:hoeperdepoep) +_isTemplate:n", query.toString());
        assertEquals("unexpected Lucene query", "+(+*:* -any:hoeperdepoep) +_isTemplate:n", query.toString());
    }

    /**
     * 'without' parameter with a multiple token value.
     */
    @Test
    public void testMultipleTokenWithout() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("without");
        any.addContent("hip hop");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+*:* -any:hip -any:hop) +_isTemplate:n", query.toString());
    }

    /**
     * 'without' parameter with a multiple token value and fuzziness.
     */
    @Test
    public void testMultipleTokenWithoutfuzzy() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("without");
        any.addContent("hip hop");
        request.addContent(any);
        Element similarity = factory.element("similarity");
        similarity.addContent("0.6885496183206108");
        request.addContent(similarity);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+*:* -any:hip -any:hop) +_isTemplate:n", query.toString());
    }

    /**
     * 'phrase' parameter with a single token value.
     */
    @Test
    public void testSingleTokenPhrase() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("phrase");
        any.addContent("humph");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:\"humph\" +_isTemplate:n", query.toString());
    }

    /**
     * 'phrase' parameter with a multiple token value.
     */
    @Test
    public void testMultipleTokenPhrase() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("phrase");
        any.addContent("that one");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:\"that one\" +_isTemplate:n", query.toString());
    }

    /**
     * 'phrase' parameter with a multiple token value and fuzziness.
     */
    @Test
    public void testMultipleTokenPhraseFuzzy() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("phrase");
        any.addContent("that one");
        request.addContent(any);
        Element similarity = factory.element("similarity");
        similarity.addContent("0.6885496183206108");
        request.addContent(similarity);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:\"that one\" +_isTemplate:n", query.toString());
    }

    /**
     * 'topic-category' parameter with single value.
     */
    @Test
    public void testSingleTopicCategory() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element tc = factory.element("topic-category");
        tc.addContent("biota*");
        request.addContent(tc);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+topicCat:biota* +_isTemplate:n", query.toString());
    }

    /**
     * 'topic-category' parameter with multiple values.
     */
    @Test
    public void testMultipleAndTopicCategories() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element tc = factory.element("topic-category");
        tc.addContent("biota*");
        request.addContent(tc);
        Element tc2 = factory.element("topic-category");
        tc2.addContent("boundaries");
        request.addContent(tc2);
        Element tc3 = factory.element("topic-category");
        tc3.addContent("environment*");
        request.addContent(tc3);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+topicCat:environment* +topicCat:boundaries +topicCat:biota*) +_isTemplate:n",
                query.toString());
    }

    /**
     * 'any' parameter with a single token value that has a wildcard.
     */
    @Test
    public void testSingleTokenStarWildcardAtTheEndAny() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("hoeper*");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:hoeper* +_isTemplate:n", query.toString());
    }

    /**
     * 'any' parameter with a single token value that has a wildcard.
     */
    @Test
    public void testSingleTokenQMarkWildcardAtTheEndAny() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("hoeper?");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+any:hoeper? +_isTemplate:n", query.toString());
    }

    @Test
    public void testMultipleOrTopicCategories() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element tc = factory.element("topic-category");
        tc.addContent("biota* or boundaries or environment");
        request.addContent(tc);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(topicCat:biota* topicCat:boundaries topicCat:environment) +_isTemplate:n",
                query.toString());
    }

    /**
     * 'template' parameter with value 'y'.
     */
    @Test
    public void testIsTemplateY() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("template");
        any.addContent("y");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_isTemplate:y", query.toString());
    }

    /**
     * 'template' parameter with value 's'.
     */
    @Test
    public void testIsTemplateS() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("template");
        any.addContent("s");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_isTemplate:s", query.toString());
    }

    /**
     * 'template' parameter with value 'WTF'.
     */
    @Test
    public void testIsTemplateWTF() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("template");
        any.addContent("WTF");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_isTemplate:n", query.toString());
    }

    /**
     * 'template' parameter with no value.
     */
    @Test
    public void testIsTemplateNoValue() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_isTemplate:n", query.toString());
    }

    /**
     * 'dateFrom' parameter.
     */
    @Test
    public void testDateFrom() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("dateFrom");
        any.addContent("12-05-1989");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+changeDate:[12-05-1989 TO *] +_isTemplate:n", query.toString());
    }

    /**
     * 'dateTo' parameter.
     */
    @Test
    public void testDateTo() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("dateTo");
        any.addContent("12-05-1989");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+changeDate:[* TO 12-05-1989T23:59:59] +_isTemplate:n", query.toString());
    }


    /**
     * 'dateTo' and 'dateFrom' parameter.
     */
    @Test
    public void testDateToFrom() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("dateTo");
        any.addContent("12-05-1989");
        request.addContent(any);
        Element any2 = factory.element("dateFrom");
        any2.addContent("11-05-1989");
        request.addContent(any2);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+changeDate:[11-05-1989 TO 12-05-1989T23:59:59] +_isTemplate:n", query.toString());
    }

    /**
     * 'siteId' parameter.
     */
    @Test
    public void testSource() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("siteId");
        any.addContent("f74e4ccf-755a-48ef-bedf-990f9872298b");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_source:f74e4ccf-755a-48ef-bedf-990f9872298b +_isTemplate:n", query.toString());
    }

    /**
     * 'title' parameter.
     */
    @Test
    public void testTitle() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("title");
        any.addContent("humph");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+title:humph +_isTemplate:n", query.toString());
    }
    
     /**	
     * 'altTitle' parameter.	
     */	
    @Test
    public void testAltTitle() {	
        // create request object	
        JDOMFactory factory = new DefaultJDOMFactory();	
        Element request = factory.element("request");	
        Element altTitle = factory.element("altTitle");	
        altTitle.addContent("humph");	
        request.addContent(altTitle);	
        // build lucene query input	
        LuceneQueryInput lQI = new LuceneQueryInput(request);	
        // build lucene query	
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, 
                _taxonomyConfiguration, _analyzer, null).build(lQI);	
        // verify query	
        assertEquals("unexpected Lucene query", "+altTitle:humph +_isTemplate:n", 
                query.toString());	
    }

    /**
     * 'protocol' parameter.
     */
    @Test
    public void testProtocol() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("protocol");
        any.addContent("download");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+protocol:download +_isTemplate:n", query.toString());
    }

    /**
     * 'type' parameter.
     */
    @Test
    public void testType() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("type");
        any.addContent("dataset");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+type:dataset +_isTemplate:n", query.toString());
    }

    /**
     * 'inspire' parameter with a single value.
     */
    @Test
    public void testSingleInspire() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("inspire");
        any.addContent("true");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+inspirecat:true +_isTemplate:n", query.toString());
    }

    /**
     * 'inspiretheme' parameter with a single value.
     */
    @Test
    public void testSingleInspireTheme() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("inspiretheme");
        any.addContent("Addresses");
        // CHANGED any.addContent("Addresses*");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+inspiretheme:Addresses +_isTemplate:n", query.toString());
    }

    /**
     * 'inspiretheme' parameter with a single multi-token value.
     */
    @Test
    public void testSingleMultiTokenInspireTheme() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("inspiretheme");
        any.addContent("\"Administrative units\"");
        // CHANGE any.addContent("Administrative units*");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+inspiretheme:\"Administrative units\" +_isTemplate:n", query.toString());
    }

    /**
     * 'inspiretheme' parameter with multiple values.
     */
    @Test
    public void testMultipleInspireTheme() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("inspiretheme");
        any.addContent("\"Cadastral parcels\"");
        request.addContent(any);
        Element any2 = factory.element("inspiretheme");
        any2.addContent("Hydrography*");
        request.addContent(any2);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+inspiretheme:Hydrography* +inspiretheme:\"Cadastral parcels\") +_isTemplate:n",
                query.toString());
    }

    /**
     * 'inspireannex' parameter with a single token value.
     */
    @Test
    public void testSingleTokenInspireAnnex() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("inspireannex");
        any.addContent("joostmaghetweten");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+inspireannex:joostmaghetweten +_isTemplate:n", query.toString());
    }

    /**
     * 'themekey' parameter with a single value.
     */
    @Test
    public void testSingleThemeKey() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("themekey");
        any.addContent("hoeperdepoep");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+keyword:hoeperdepoep +_isTemplate:n", query.toString());
    }

    /**
     * 'themekey' parameter with multiple values.
     */
    @Test
    public void testMultipleThemeKey() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("themekey");
        any.addContent("hoeperdepoep");
        request.addContent(any);
        Element any2 = factory.element("themekey");
        any2.addContent("\"zat op de stoep\"");
        request.addContent(any2);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+keyword:\"zat op de stoep\" +keyword:hoeperdepoep) +_isTemplate:n", query.toString());
    }

    /**
     * 'themekey' parameter in a single element separating multiple themekeys by ' [keywordseparator] '.
     * This is how the search page JS delivers themekey; that's unwanted behaviour, it's better
     * to have it deliver multiple themekey elements as in the testcase above.
     */
    @Test
    public void testMultipleThemeKeyOrSeparated() {
        String keywordSeparator = " or ";
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("themekey");
        any.addContent("\"hoeperdepoep\"" + keywordSeparator + "\"zat op de stoep\"");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(keyword:\"hoeperdepoep\" keyword:\"zat op de stoep\") +_isTemplate:n",
                query.toString());

    }

    /**
     * 'category' parameter with a single value.
     */
    @Test
    public void testSingleCategory() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("category");
        any.addContent("hoeperdepoep");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_cat:hoeperdepoep +_isTemplate:n", query.toString());
    }

    /**
     * 'parentUUID' parameter.
     */
    @Test
    public void testParentUUID() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("parentUuid");
        any.addContent("as432f-s45hj3-vcx35s-fsd8sf");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+parentUuid:as432f-s45hj3-vcx35s-fsd8sf +_isTemplate:n", query.toString());
    }

    /**
     * 'operatesOn' parameter.
     */
    @Test
    public void testOperatesOn() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("operatesOn");
        any.addContent("value");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+operatesOn:value +_isTemplate:n", query.toString());
    }

    /**
     * '_schema' parameter.
     */
    @Test
    public void testSchema() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("_schema");
        any.addContent("value");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_schema:value +_isTemplate:n", query.toString());
    }

    /**
     * 'temporalExtent' parameters
     */
    @Test
    public void testTemporalExtent() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();

        // test extFrom
        Element request = factory.element("request");
        Element extFrom = factory.element("extFrom");
        extFrom.addContent("2010-04-01T17:35:00");
        request.addContent(extFrom);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);

        String expected = "+(tempExtentBegin:[2010-04-01T17:35:00 TO *] tempExtentEnd:[2010-04-01T17:35:00 TO *]) +_isTemplate:n";
        assertEquals("unexpected Lucene query", expected, query.toString());

        // test extTo
        request = factory.element("request");
        Element extTo = factory.element("extTo");
        extTo.addContent("2010-04-27T17:43:00");
        request.addContent(extTo);
        // build lucene query input
        LuceneQueryInput lQI2 = new LuceneQueryInput(request);
        // build lucene query
        query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI2);

        expected = "+(tempExtentBegin:[* TO 2010-04-27T17:43:00] tempExtentEnd:[* TO 2010-04-27T17:43:00]) +_isTemplate:n";
        assertEquals("unexpected Lucene query", expected, query.toString());

        // test extfrom and extTo
        request = factory.element("request");

        extFrom = factory.element("extFrom");
        extFrom.addContent("2010-04-08T17:46:00");
        request.addContent(extFrom);

        extTo = factory.element("extTo");
        extTo.addContent("2010-04-27T17:43:00");
        request.addContent(extTo);
        // build lucene query input
        LuceneQueryInput lQI3 = new LuceneQueryInput(request);
        // build lucene query
        query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI3);

        expected = "+(tempExtentBegin:[2010-04-08T17:46:00 TO 2010-04-27T17:43:00] tempExtentEnd:[2010-04-08T17:46:00 TO " +
                   "2010-04-27T17:43:00] (+tempExtentEnd:[2010-04-27T17:43:00 TO *] +tempExtentBegin:[* TO 2010-04-08T17:46:00])) " +
                   "+_isTemplate:n";
        assertEquals("unexpected Lucene query", expected, query.toString());

        // create request object
        /*JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element extFrom = factory.element("extFrom");
        extFrom.addContent("2010-04-27T16:40:00");
        request.addContent(extFrom);
        Element extTo = factory.element("extTo");
        extTo.addContent("2010-04-29T16:40:00");
        request.addContent(extTo);
        // build lucene query
        Query query = new LuceneQueryBuilder2(_tokenizedFieldSet, _numericFieldSet, _analyzer, null).build(lQI);
        // verify query
        assertEquals("+_isTemplate:n +(tempExtentBegin:[2010-04-27T16:40:00 TO 2010-04-29T16:40:00]" +
                " tempExtentEnd:[2010-04-27T16:40:00 TO 2010-04-29T16:40:00] " +
                "(+tempExtentEnd:[2010-04-29T16:40:00 TO *] " +
                "+tempExtentBegin:[* TO 2010-04-27T16:40:00]))", query.toString());*/
    }

    /**
     * 'category' parameter with a multiple values.
     */
    @Test
    public void testMultipleCategory() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("category");
        any.addContent("hoeperdepoep");
        request.addContent(any);
        Element any2 = factory.element("category");
        any2.addContent("\"zat op de stoep\"");
        request.addContent(any2);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+_cat:\"zat op de stoep\" +_cat:hoeperdepoep) +_isTemplate:n", query.toString());
    }

    @Test
    public void testMultipleOrCategory() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("category");
        any.addContent("hoeperdepoep or \"zat op de stoep\"");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(_cat:hoeperdepoep _cat:\"zat op de stoep\") +_isTemplate:n", query.toString());
    }


    /**
     * 'editable' parameter true.
     */
    @Test
    public void testEditableTrue() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("editable");
        any.addContent("true");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_isTemplate:n", query.toString());
    }

    /**
     * 'featured' parameter true.
     */
    @Test
    public void testFeaturedTrue() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("featured");
        any.addContent("true");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(+_op6:1 +_op0:1) +_isTemplate:n", query.toString());
    }

    /**
     * 'featured' parameter not true.
     */
    @Test
    public void testFeaturedNotTrue() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("featured");
        any.addContent("not true");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_isTemplate:n", query.toString());
    }


    /**
     * 'groups' parameter single.
     */
    @Test
    public void testSingleGroup() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("group");
        any.addContent("hatsjekidee");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(_op0:hatsjekidee _op2:hatsjekidee) +_isTemplate:n", query.toString());
    }

    /**
     * 'groups' parameter multi.
     */
    @Test
    public void testMultiGroup() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("group");
        any.addContent("hatsjekidee");
        request.addContent(any);
        Element any2 = factory.element("group");
        any2.addContent("nou moe");
        request.addContent(any2);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(_op0:nou moe _op2:nou moe _op0:hatsjekidee _op2:hatsjekidee) +_isTemplate:n",
                query.toString());
    }

    /**
     * 'groups' parameter multi with reviewer.
     */
    @Test
    public void testMultiGroupReviewer() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("group");
        any.addContent("hatsjekidee");
        request.addContent(any);
        Element any2 = factory.element("group");
        any2.addContent("nou moe");
        request.addContent(any2);
        Element any3 = factory.element("isReviewer");
        any3.addContent("yeah!");
        request.addContent(any3);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(_op0:nou moe _op2:nou moe _op0:hatsjekidee _op2:hatsjekidee) +_isTemplate:n",
                query.toString());
    }

    /**
     * 'groups' parameter multi with owner.
     */
    @Test
    public void testMultiGroupOwner() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("group");
        any.addContent("hatsjekidee");
        request.addContent(any);
        Element any2 = factory.element("group");
        any2.addContent("nou moe");
        request.addContent(any2);
        Element any3 = factory.element("owner");
        any3.addContent("yeah!");
        request.addContent(any3);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(_op0:nou moe _op2:nou moe _op0:hatsjekidee _op2:hatsjekidee _owner:yeah!) " +
                                                "+_isTemplate:n", query.toString());
    }

    /**
     * 'groups' parameter multi with admin.
     */
    @Test
    public void testMultiGroupAdmin() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("group");
        any.addContent("hatsjekidee");
        request.addContent(any);
        Element any2 = factory.element("group");
        any2.addContent("nou moe");
        request.addContent(any2);
        Element any3 = factory.element("isAdmin");
        any3.addContent("yeah!");
        request.addContent(any3);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(_op0:nou moe _op2:nou moe _op0:hatsjekidee _op2:hatsjekidee _dummy:0) " +
                                                "+_isTemplate:n", query.toString());
    }

    /**
     * 'bounding box' parameter equals.
     * TODO verify with Jose why he put the float values here.
     */
    @Test
    public void testBBEquals() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element anyE = factory.element("eastBL");
        anyE.addContent("55");
        request.addContent(anyE);
        Element anyW = factory.element("westBL");
        anyW.addContent("43");
        request.addContent(anyW);
        Element anyN = factory.element("northBL");
        anyN.addContent("12");
        request.addContent(anyN);
        Element anyS = factory.element("southBL");
        anyS.addContent("9");
        request.addContent(anyS);
        Element anyR = factory.element("relation");
        anyR.addContent("equal");
        request.addContent(anyR);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+eastBL:[55.0 TO 55.0] +westBL:[43.0 TO 43.0] +southBL:[9.0 TO 9.0] +northBL:[12.0 TO " +
                                                "12.0] +_isTemplate:n", query.toString());
    }

    /**
     * 'bounding box' parameter overlaps.
     */
    @Test
    public void testBBOverlaps() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element anyE = factory.element("eastBL");
        anyE.addContent("55");
        request.addContent(anyE);
        Element anyW = factory.element("westBL");
        anyW.addContent("43");
        request.addContent(anyW);
        Element anyN = factory.element("northBL");
        anyN.addContent("12");
        request.addContent(anyN);
        Element anyS = factory.element("southBL");
        anyS.addContent("9");
        request.addContent(anyS);
        Element anyR = factory.element("relation");
        anyR.addContent("overlaps");
        request.addContent(anyR);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+westBL:[-180.0 TO 55.0] +eastBL:[43.0 TO 180.0] +northBL:[9.0 TO 90.0] +southBL:[-90" +
                                                ".0 TO 12.0] +_isTemplate:n", query.toString());
    }

    /**
     * 'bounding box' parameter encloses.
     */
    @Test
    public void testBBEncloses() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element anyE = factory.element("eastBL");
        anyE.addContent("55");
        request.addContent(anyE);
        Element anyW = factory.element("westBL");
        anyW.addContent("43");
        request.addContent(anyW);
        Element anyN = factory.element("northBL");
        anyN.addContent("12");
        request.addContent(anyN);
        Element anyS = factory.element("southBL");
        anyS.addContent("9");
        request.addContent(anyS);
        Element anyR = factory.element("relation");
        anyR.addContent("encloses");
        request.addContent(anyR);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+eastBL:[55.0 TO 180.0] +westBL:[-180.0 TO 43.0] +southBL:[-90.0 TO 9.0] +northBL:[12" +
                                                ".0 TO 90.0] +_isTemplate:n", query.toString());
    }

    /**
     * 'bounding box' parameter fullyEnclosedWithin.
     */
    @Test
    public void testBBFullyEnclosedWithin() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element anyE = factory.element("eastBL");
        anyE.addContent("55");
        request.addContent(anyE);
        Element anyW = factory.element("westBL");
        anyW.addContent("43");
        request.addContent(anyW);
        Element anyN = factory.element("northBL");
        anyN.addContent("12");
        request.addContent(anyN);
        Element anyS = factory.element("southBL");
        anyS.addContent("9");
        request.addContent(anyS);
        Element anyR = factory.element("relation");
        anyR.addContent("fullyEnclosedWithin");
        request.addContent(anyR);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+eastBL:[43.0 TO 55.0] +westBL:[43.0 TO 55.0] +southBL:[9.0 TO 12.0] +northBL:[9.0 TO " +
                                                "12.0] +_isTemplate:n", query.toString());
    }

    /**
     * 'bounding box' parameter fullyOutsideOf.
     */
    @Test
    public void testBBFullyOutsideOf() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element anyE = factory.element("eastBL");
        anyE.addContent("55");
        request.addContent(anyE);
        Element anyW = factory.element("westBL");
        anyW.addContent("43");
        request.addContent(anyW);
        Element anyN = factory.element("northBL");
        anyN.addContent("30");
        request.addContent(anyN);
        Element anyS = factory.element("southBL");
        anyS.addContent("0");
        request.addContent(anyS);
        Element anyR = factory.element("relation");
        anyR.addContent("fullyOutsideOf");
        request.addContent(anyR);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "westBL:[55.0 TO 180.0] eastBL:[-180.0 TO 43.0] northBL:[-90.0 TO 0.0] southBL:[30.0 TO" +
                                                " 90.0] +_isTemplate:n", query.toString());
    }

    /**
     * 'bounding box' parameter overlaps - standard values from search page.
     */
    @Test
    public void testBBOverlapsStandard() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element anyE = factory.element("eastBL");
        anyE.addContent("180");
        request.addContent(anyE);
        Element anyW = factory.element("westBL");
        anyW.addContent("-180");
        request.addContent(anyW);
        Element anyN = factory.element("northBL");
        anyN.addContent("90");
        request.addContent(anyN);
        Element anyS = factory.element("southBL");
        anyS.addContent("-90");
        request.addContent(anyS);
        Element anyR = factory.element("relation");
        anyR.addContent("overlaps");
        request.addContent(anyR);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+westBL:[-180.0 TO 180.0] +eastBL:[-180.0 TO 180.0] +northBL:[-90.0 TO 90.0] " +
                                                "+southBL:[-90.0 TO 90.0] +_isTemplate:n", query.toString());
    }

    /**
     * <request>
     * <eastBL>180</eastBL>
     * <title>hoi</title>
     * <sortBy>popularity</sortBy>
     * <southBL>-90</southBL>
     * <northBL>90</northBL>
     * <any />
     * <similarity>1</similarity>
     * <relation>overlaps</relation>
     * <westBL>-180</westBL>
     * <hitsPerPage>10</hitsPerPage>
     * <attrset>geo</attrset>
     * <group>1</group>
     * <group>0</group>
     * </request>
     */
    @Test
    public void testRandomTest1() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        Element anyT = factory.element("title");
        anyT.addContent("hoi");
        request.addContent(anyT);

        Element anySB = factory.element("sortBy");
        anySB.addContent("popularity");
        request.addContent(anySB);

        Element any = factory.element("any");
        request.addContent(any);

        Element anySM = factory.element("similarity");
        anySM.addContent("1");
        request.addContent(anySM);

        Element anyHP = factory.element("hitsPerPage");
        anyHP.addContent("10");
        request.addContent(anyHP);

        Element anyAS = factory.element("attrset");
        anyAS.addContent("geo");
        request.addContent(anyAS);

        Element anyG1 = factory.element("group");
        anyG1.addContent("0");
        request.addContent(anyG1);

        Element anyG2 = factory.element("group");
        anyG2.addContent("1");
        request.addContent(anyG2);


        Element anyE = factory.element("eastBL");
        anyE.addContent("180");
        request.addContent(anyE);
        Element anyW = factory.element("westBL");
        anyW.addContent("-180");
        request.addContent(anyW);
        Element anyN = factory.element("northBL");
        anyN.addContent("90");
        request.addContent(anyN);
        Element anyS = factory.element("southBL");
        anyS.addContent("-90");
        request.addContent(anyS);
        Element anyR = factory.element("relation");
        anyR.addContent("overlaps");
        request.addContent(anyR);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+(_op0:1 _op2:1 _op0:0 _op2:0) +westBL:[-180.0 TO 180.0] +eastBL:[-180.0 TO 180.0] " +
                                                "+northBL:[-90.0 TO 90.0] +southBL:[-90.0 TO 90.0] +title:hoi +_isTemplate:n",
                query.toString());
    }

    /**
     * All parameters as sent by the Popular Get service when it searches for metadata. This test
     * includes many parameters that are present, but empty.
     */
    @Test
    public void testPopularGet() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element title = factory.element("title");
        title.addContent("");
        request.addContent(title);
        Element abstrakt = factory.element("abstract");
        abstrakt.addContent("");
        request.addContent(abstrakt);
        Element any = factory.element("any");
        any.addContent("");
        request.addContent(any);
        Element region = factory.element("region");
        region.addContent("");
        request.addContent(region);
        Element phrase = factory.element("phrase");
        phrase.addContent("");
        request.addContent(phrase);
        Element all = factory.element("all");
        all.addContent("");
        request.addContent(all);
        Element or = factory.element("or");
        or.addContent("");
        request.addContent(or);
        Element without = factory.element("without");
        without.addContent("");
        request.addContent(without);
        Element siteId = factory.element("siteId");
        siteId.addContent("");
        request.addContent(siteId);
        Element group = factory.element("group");
        group.addContent("");
        request.addContent(group);
        Element profile = factory.element("profile");
        profile.addContent("");
        request.addContent(profile);
        Element servers = factory.element("servers");
        servers.addContent("");
        request.addContent(servers);
        Element protocol = factory.element("protocol");
        protocol.addContent("");
        request.addContent(protocol);
        Element topicCat = factory.element("topicCat");
        topicCat.addContent("");
        request.addContent(topicCat);
        Element category = factory.element("category");
        category.addContent("");
        request.addContent(category);
        Element themekey = factory.element("themekey");
        themekey.addContent("");
        request.addContent(themekey);
        Element keywords = factory.element("keywords");
        keywords.addContent("");
        request.addContent(keywords);
        Element dateTo = factory.element("dateTo");
        dateTo.addContent("");
        request.addContent(dateTo);
        Element dateFrom = factory.element("dateFrom");
        dateFrom.addContent("");
        request.addContent(dateFrom);

        Element southBL = factory.element("southBL");
        southBL.addContent("-90");
        request.addContent(southBL);
        Element northBL = factory.element("northBL");
        northBL.addContent("90");
        request.addContent(northBL);
        Element westBL = factory.element("westBL");
        westBL.addContent("-180");
        request.addContent(westBL);
        Element eastBL = factory.element("eastBL");
        eastBL.addContent("180");
        request.addContent(eastBL);
        Element relation = factory.element("relation");
        relation.addContent("overlaps");
        request.addContent(relation);
        Element template = factory.element("template");
        template.addContent("n");
        request.addContent(template);
        Element extended = factory.element("extended");
        extended.addContent("off");
        request.addContent(extended);
        Element remote = factory.element("remote");
        remote.addContent("off");
        request.addContent(remote);
        Element timeout = factory.element("timeout");
        timeout.addContent("20");
        request.addContent(timeout);
        Element hitsPerPage = factory.element("hitsPerPage");
        hitsPerPage.addContent("10");
        request.addContent(hitsPerPage);
        Element similarity = factory.element("similarity");
        similarity.addContent(".8");
        request.addContent(similarity);
        Element output = factory.element("output");
        output.addContent("full");
        request.addContent(output);
        Element sortBy = factory.element("sortBy");
        sortBy.addContent("popularity");
        request.addContent(sortBy);

        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+_isTemplate:n +westBL:[-180.0 TO 180.0] +eastBL:[-180.0 TO 180.0] +northBL:[-90.0 TO " +
                                                "90.0] +southBL:[-90.0 TO 90.0]", query.toString());
    }


    /**
     * 'dynamic' parameter.
     */
    @Test
    public void testDynamic() {
        // create request object with dynamic=on
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("dynamic");
        any.addContent("true");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+dynamic:true +_isTemplate:n", query.toString());
    }

    /**
     * 'download' parameter.
     */
    @Test
    public void testDownload() {
        // create request object with dynamic=on
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("download");
        any.addContent("true");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+download:true +_isTemplate:n", query.toString());

    }


    /**
     * 'download' parameter.
     */
    @Test
    public void testDigitalAndPaper() {
        // create request object with digital=on, paper=off
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("digital");
        any.addContent("true");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        // build lucene query
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        // verify query
        assertEquals("unexpected Lucene query", "+digital:true +_isTemplate:n", query.toString());


        // create request object with with digital=off, paper=on
        request = factory.element("request");
        any = factory.element("paper");
        any.addContent("true");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQIa = new LuceneQueryInput(request);
        // build lucene query
        query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQIa);
        // verify query
        assertEquals("unexpected Lucene query", "+paper:true +_isTemplate:n", query.toString());
    }

    /**
     * Tests passing operations (download and/or dynamic for instance) parameters criterias.
     *
     */
    @Test
    public void testDownloadDynamicParameter() {
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element download = factory.element("_operation5").addContent("1 or 2 or 3");
        Element dynamic  = factory.element("_operation1").addContent("1 or 2 or 3");
        request.addContent(download).addContent(dynamic);
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        assertTrue(query.toString().contains("+(_op5:1 _op5:2 _op5:3) +(_op1:1 _op1:2 _op1:3)"));
    }
    /**
     * Same test as above, but only download.
     */
    @Test
    public void testDownloadParameter() {
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element download = factory.element("_operation1").addContent("1 or 2 or 3");
        request.addContent(download);
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        assertTrue(query.toString().contains("+(_op1:1 _op1:2 _op1:3)"));
    }
    /**
     * Only dynamic operation parameter.
     */
    @Test
    public void testDynamicParameter() {
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element dynamic = factory.element("_operation5").addContent("1 or 2 or 3");
        request.addContent(dynamic);
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        assertTrue(query.toString().contains("+(_op5:1 _op5:2 _op5:3)"));
    }
    /**
     * Only editing operation parameter.
     */
    @Test
    public void testEditingParameter() {
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element editing = factory.element("_operation2").addContent("1 or 2 or 3");
        request.addContent(editing);
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        assertTrue(query.toString().contains("+(_op2:1 _op2:2 _op2:3)"));
    }    
    
    /**
     * No operation parameter.
     */
    @Test
    public void testWithoutOperationParameter() {
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        assertTrue(query.toString().equals("+_isTemplate:n"));
    }

    /**
     * 'facet.q' parameter. Single drilldown
     */
    @Test
    public void testDrilldownQuery() {
        Element request = buildSingleDrilldownQuery("keyword/ocean/salinity");
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        assertEquals("unexpected Lucene query", "+(+_isTemplate:n) +ConstantScore($facets:keywordoceansalinity)^0.0", query.toString());
    }

    /**
     * 'facet.q' parameter. Encoded value
     */
    @Test
    public void testEncodedDrilldownQuery() {
        Element request = buildSingleDrilldownQuery("keyword/oceans%2Frivers/salinity");
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        assertEquals("unexpected Lucene query", "+(+_isTemplate:n) +ConstantScore($facets:keywordoceans/riverssalinity)^0.0", query.toString());
    }

    /**
     * 'facet.q' parameter. Multiple drilldown using AND separator
     */

    @Test
    public void testMultipleDrilldownQueryUsingAnd() {
        Element request = buildMultipleDrilldownQueryUsingAnd(
            "keyword/ocean/salinity",
            "keyword/ocean/chemistry",
            "keyword/ocean/temperature"
        );
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        assertEquals("unexpected Lucene query", "+(+_isTemplate:n) +ConstantScore($facets:keywordoceansalinity $facets:keywordoceanchemistry $facets:keywordoceantemperature)^0.0", query.toString());
    }

    /**
     * 'facet.q' parameter. Multiple drilldown using multiple facet query parameters
     */

    @Test
    public void testMultipleDrilldownUsingFacetParameters() {
        Element request = buildMultipleDrilldownFacetQueries(
            "keyword/ocean/salinity",
            "keyword/ocean/chemistry",
            "keyword/ocean/temperature"
        );
        LuceneQueryInput lQI = new LuceneQueryInput(request);
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _taxonomyConfiguration, _analyzer, null).build(lQI);
        assertEquals("unexpected Lucene query", "+(+_isTemplate:n) +ConstantScore($facets:keywordoceanchemistry $facets:keywordoceansalinity $facets:keywordoceantemperature)^0.0", query.toString());
    }

    private Element buildSingleDrilldownQuery(String drilldownPath) {
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element facetQuery = factory.element(SearchParameter.FACET_QUERY);
        facetQuery.addContent(drilldownPath);
        request.addContent(facetQuery);
        return request;
    }

    private Element buildMultipleDrilldownQueryUsingAnd(String... drilldowns) {
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element facetQuery = factory.element(SearchParameter.FACET_QUERY);

        StringBuilder queryString = new StringBuilder();
        
        for (String drilldown : drilldowns) {
            queryString.append(drilldown);
            queryString.append("&");
        }

        facetQuery.addContent(queryString.toString());
        request.addContent(facetQuery);

        return request;
    }

    private Element buildMultipleDrilldownFacetQueries(String... drilldowns) {
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");

        for (String drilldown : drilldowns) {
            Element facetQuery = factory.element(SearchParameter.FACET_QUERY);
            facetQuery.addContent(drilldown);
            request.addContent(facetQuery);
        }
        
        return request;
    }
}

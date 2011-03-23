package org.fao.geonet.kernel.search;

import java.util.HashMap;
import java.util.HashSet;

import junit.framework.TestCase;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.search.Query;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.jdom.DefaultJDOMFactory;
import org.jdom.Element;
import org.jdom.JDOMFactory;

/**
 *
 * Unit test for building Lucene queries.
 *
 * @author heikki doeleman
 *
 */
public class LuceneQueryTest extends TestCase {

	private HashSet<String> _tokenizedFieldSet;
	private HashMap<String, LuceneConfigNumericField> _numericFieldSet;
	private PerFieldAnalyzerWrapper _analyzer;

	public LuceneQueryTest(String name) throws Exception {
		super(name);

		_analyzer = new PerFieldAnalyzerWrapper(new GeoNetworkAnalyzer());
    _analyzer.addAnalyzer("_uuid", new GeoNetworkAnalyzer());
    _analyzer.addAnalyzer("parentUuid", new GeoNetworkAnalyzer());
    _analyzer.addAnalyzer("operatesOn", new GeoNetworkAnalyzer());
    _analyzer.addAnalyzer("subject", new KeywordAnalyzer());

    LuceneConfig lc = new LuceneConfig("src/main/webapp/", "WEB-INF/config-lucene.xml");
    	
		_tokenizedFieldSet = lc.getTokenizedField();
		_numericFieldSet = lc.getNumericFields();
	}

	/**
	 * 'any' parameter with a single token value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+any:hoeperdepoep +_isTemplate:n", query.toString());
	}

	/**
     * 'any' parameter with a single token value.
     */
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+(+any:demo +any:hoeperdepoep) +_isTemplate:n", query.toString());
    }
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
       Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
       // verify query
       assertEquals("+any:hoeper?poep +_isTemplate:n", query.toString());
    }
	
	/**
	 * 'any' parameter with a single token value that has a wildcard.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+any:hoeper*poep +_isTemplate:n", query.toString());
	}

	/**
	 * 'any' parameter with a no value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_isTemplate:n", query.toString());
	}

	/**
	 * 'any' parameter check case insensitivity..
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+any:hoeperdepoep +_isTemplate:n", query.toString());
	}

	/**
	 * 'any' parameter with a single token value and with fuzziness.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+any:asjemenou~0.5740458 +_isTemplate:n", query.toString());
	}

	/**
	 * 'uuid' parameter with a single token value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(_uuid:ad2aa2c7-f099-47cb-8a38-4effe2a2d250) +_isTemplate:n", query.toString());
	}

	/**
	 * 'uuid' parameter with a double token value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(_uuid:63c2378a-17a7-b863-bff4-cc3ef507d10d _uuid:ad2aa2c7-f099-47cb-8a38-4effe2a2d250) +_isTemplate:n", query.toString());
	}


	/**
	 * 'any' parameter with a single token value and with fuzziness 1.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+any:asjemenou +_isTemplate:n", query.toString());
	}

	/**
	 * 'any' parameter with multiple token values.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(+any:deze +any:die) +_isTemplate:n", query.toString());
	}

	/**
	 * 'any' parameter with multiple token values and with fuzziness.
	 */
	public void testMultipleTokensAnyFuzzy() {
		// create request object
		JDOMFactory factory = new DefaultJDOMFactory();
		Element request = factory.element("request");
		Element any = factory.element("any");
		any.addContent("fucking hell");
		request.addContent(any);
		Element similarity = factory.element("similarity");
		similarity.addContent("0.6885496183206108");
		request.addContent(similarity);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
		// build lucene query
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(+any:fucking~0.68854964 +any:hell~0.68854964) +_isTemplate:n", query.toString());
	}

	/**
	 * 'or' parameter with a single token value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(any:hoeperdepoep) +_isTemplate:n", query.toString());
	}

	/**
	 * 'or' parameter with a multiple token value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(any:hoep any:poep) +_isTemplate:n", query.toString());
	}

	/**
	 * 'or' parameter with a multiple token value and fuzziness.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(any:hoep~0.59694654 any:poep~0.59694654) +_isTemplate:n", query.toString());
	}

	/**
	 * 'all' parameter with a single token value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+any:hoeperdepoep +_isTemplate:n", query.toString());
	}

	/**
	 * 'all' parameter with multiple token values and with fuzziness.
	 */
	public void testMultipleTokensAllFuzzy() {
		// create request object
		JDOMFactory factory = new DefaultJDOMFactory();
		Element request = factory.element("request");
		Element any = factory.element("all");
		any.addContent("fucking hell");
		request.addContent(any);
		Element similarity = factory.element("similarity");
		similarity.addContent("0.6885496183206108");
		request.addContent(similarity);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
		// build lucene query
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(+any:fucking~0.68854964 +any:hell~0.68854964) +_isTemplate:n", query.toString());
	}

	/**
	 * 'without' parameter with a single token value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		//assertEquals("+(+MatchAllDocsQuery -any:hoeperdepoep) +_isTemplate:n", query.toString());
        assertEquals("+(+*:* -any:hoeperdepoep) +_isTemplate:n", query.toString());
	}

	/**
	 * 'without' parameter with a multiple token value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(+*:* -any:hip -any:hop) +_isTemplate:n", query.toString());
	}

	/**
	 * 'without' parameter with a multiple token value and fuzziness.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(+*:* -any:hip -any:hop) +_isTemplate:n", query.toString());
	}

	/**
	 * 'phrase' parameter with a single token value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+any:\"humph\" +_isTemplate:n", query.toString());
	}

	/**
	 * 'phrase' parameter with a multiple token value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+any:\"that one\" +_isTemplate:n", query.toString());
	}

	/**
	 * 'phrase' parameter with a multiple token value and fuzziness.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+any:\"that one\" +_isTemplate:n", query.toString());
	}

	/**
	 * 'topic-category' parameter with single value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+topicCat:biota* +_isTemplate:n", query.toString());
	}

	/**
	 * 'topic-category' parameter with multiple values.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(+topicCat:environment* +topicCat:boundaries +topicCat:biota*) +_isTemplate:n", query.toString());
	}
	/**
     * 'any' parameter with a single token value that has a wildcard.
     */
    public void testSingleTokenStarWildcardAtTheEndAny() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("hoeper*");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput0 lQI = new LuceneQueryInput0(request);
        // build lucene query
        Query query = new LuceneQueryBuilder0(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+any:hoeper* +_isTemplate:n", query.toString());
    }

    /**
     * 'any' parameter with a single token value that has a wildcard.
     */
    public void testSingleTokenQMarkWildcardAtTheEndAny() {
        // create request object
        JDOMFactory factory = new DefaultJDOMFactory();
        Element request = factory.element("request");
        Element any = factory.element("any");
        any.addContent("hoeper?");
        request.addContent(any);
        // build lucene query input
        LuceneQueryInput0 lQI = new LuceneQueryInput0(request);
        // build lucene query
        Query query = new LuceneQueryBuilder0(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+any:hoeper? +_isTemplate:n", query.toString());
    }
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+(topicCat:biota* topicCat:boundaries topicCat:environment) +_isTemplate:n", query.toString());
    }

	/**
	 * 'template' parameter with value 'y'.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_isTemplate:y", query.toString());
	}

	/**
	 * 'template' parameter with value 's'.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_isTemplate:s", query.toString());
	}
	/**
	 * 'template' parameter with value 'WTF'.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_isTemplate:n", query.toString());
	}
	/**
	 * 'template' parameter with no value.
	 */
	public void testIsTemplateNoValue() {
		// create request object
		JDOMFactory factory = new DefaultJDOMFactory();
		Element request = factory.element("request");
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
		// build lucene query
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_isTemplate:n", query.toString());
	}

	/**
	 * 'dateFrom' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+changeDate:[12-05-1989 TO *] +_isTemplate:n", query.toString());
	}

	/**
	 * 'dateTo' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+changeDate:[* TO 12-05-1989T23:59:59] +_isTemplate:n", query.toString());
	}


	/**
	 * 'dateTo' and 'dateFrom' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+changeDate:[11-05-1989 TO 12-05-1989T23:59:59] +_isTemplate:n", query.toString());
	}

	/**
	 * 'siteId' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_source:f74e4ccf-755a-48ef-bedf-990f9872298b +_isTemplate:n", query.toString());
	}

	/**
	 * 'title' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+title:humph +_isTemplate:n", query.toString());
	}

	/**
	 * 'protocol' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+protocol:download +_isTemplate:n", query.toString());
	}

	/**
	 * 'type' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+type:dataset +_isTemplate:n", query.toString());
	}

    /**
     * 'inspire' parameter with a single value.
     */
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+inspirecat:true +_isTemplate:n", query.toString());
    }

    /**
     * 'inspiretheme' parameter with a single value.
     */
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+inspiretheme:Addresses +_isTemplate:n", query.toString());
    }

    /**
     * 'inspiretheme' parameter with a single multi-token value.
     */
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+inspiretheme:\"Administrative units\" +_isTemplate:n", query.toString());
    }
    /**
     * 'inspiretheme' parameter with multiple values.
     */
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+(+inspiretheme:Hydrography* +inspiretheme:\"Cadastral parcels\") +_isTemplate:n", query.toString());
    }

    /**
     * 'inspireannex' parameter with a single token value.
     */
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+inspireannex:joostmaghetweten +_isTemplate:n", query.toString());
    }

	/**
	 * 'themekey' parameter with a single value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+keyword:hoeperdepoep +_isTemplate:n", query.toString());
	}

	/**
	 * 'themekey' parameter with multiple values.
     *
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(+keyword:\"zat op de stoep\" +keyword:hoeperdepoep) +_isTemplate:n", query.toString());
	}

    /**
     * 'themekey' parameter in a single element separating multiple themekeys by ' [keywordseparator] '.
     * This is how the search page JS delivers themekey; that's unwanted behaviour, it's better
     * to have it deliver multiple themekey elements as in the testcase above.
     */
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+(keyword:\"hoeperdepoep\" keyword:\"zat op de stoep\") +_isTemplate:n", query.toString());

    }

	/**
	 * 'category' parameter with a single value.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_cat:hoeperdepoep +_isTemplate:n", query.toString());
	}

    /**
	 * 'parentUUID' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+parentUuid:as432f-s45hj3-vcx35s-fsd8sf +_isTemplate:n", query.toString());
	}

    /**
     * 'operatesOn' parameter.
     */
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+operatesOn:value +_isTemplate:n", query.toString());
    }

    /**
     * '_schema' parameter.
     */
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+_schema:value +_isTemplate:n", query.toString());
    }                                                                    

    /**
     * 'temporalExtent' parameters
     */
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);

        String expected = "+(tempExtentBegin:[2010-04-01T17:35:00 TO *] tempExtentEnd:[2010-04-01T17:35:00 TO *]) +_isTemplate:n";
        assertEquals(expected, query.toString());

        // test extTo
        request = factory.element("request");
        Element extTo = factory.element("extTo");
        extTo.addContent("2010-04-27T17:43:00");
        request.addContent(extTo);
        // build lucene query input
        LuceneQueryInput lQI2 = new LuceneQueryInput(request);
        // build lucene query
        query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI2);

        expected = "+(tempExtentBegin:[* TO 2010-04-27T17:43:00] tempExtentEnd:[* TO 2010-04-27T17:43:00]) +_isTemplate:n";
        assertEquals(expected, query.toString());

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
        query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI3);

        expected = "+(tempExtentBegin:[2010-04-08T17:46:00 TO 2010-04-27T17:43:00] tempExtentEnd:[2010-04-08T17:46:00 TO 2010-04-27T17:43:00] (+tempExtentEnd:[2010-04-27T17:43:00 TO *] +tempExtentBegin:[* TO 2010-04-08T17:46:00])) +_isTemplate:n";
        assertEquals(expected, query.toString());

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
        Query query = new LuceneQueryBuilder2(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+_isTemplate:n +(tempExtentBegin:[2010-04-27T16:40:00 TO 2010-04-29T16:40:00]" +
                " tempExtentEnd:[2010-04-27T16:40:00 TO 2010-04-29T16:40:00] " +
                "(+tempExtentEnd:[2010-04-29T16:40:00 TO *] " +
                "+tempExtentBegin:[* TO 2010-04-27T16:40:00]))", query.toString());*/
    }
	/**
	 * 'category' parameter with a multiple values.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(+_cat:\"zat op de stoep\" +_cat:hoeperdepoep) +_isTemplate:n", query.toString());
	}
	
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
        Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
        // verify query
        assertEquals("+(_cat:hoeperdepoep _cat:\"zat op de stoep\") +_isTemplate:n", query.toString());
    }

	/**
	 * 'groupOwner' parameter with a single value (it should be ignored and not go into the query).
	 */
	public void testSingleGroupOwner() {
		// create request object
		JDOMFactory factory = new DefaultJDOMFactory();
		Element request = factory.element("request");
		Element any = factory.element("_groupOwner");
		any.addContent("JanMetDeKorteNaam");
		request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
		// build lucene query
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_isTemplate:n", query.toString());
	}

	/**
	 * 'groupOwner' parameter with multiple values (it should be ignored and not go into the query).
	 */
	public void testMultipleGroupOwner() {
		// create request object
		JDOMFactory factory = new DefaultJDOMFactory();
		Element request = factory.element("request");
		Element any = factory.element("_groupOwner");
		any.addContent("JanMetDeKorteNaam");
		request.addContent(any);
		Element any2 = factory.element("_groupOwner");
		any2.addContent("GregoriusMetDeLangeNaam");
		request.addContent(any2);
        // build lucene query input
        LuceneQueryInput lQI = new LuceneQueryInput(request);
		// build lucene query
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_isTemplate:n", query.toString());
	}

	/**
	 * 'editable' parameter true.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_isTemplate:n", query.toString());
	}

	/**
	 * 'featured' parameter true.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(+_op6:1 +_op0:1) +_isTemplate:n", query.toString());
	}

	/**
	 * 'featured' parameter not true.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_isTemplate:n", query.toString());
	}


	/**
	 * 'groups' parameter single.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(_op0:hatsjekidee _op2:hatsjekidee) +_isTemplate:n", query.toString());
	}

	/**
	 * 'groups' parameter multi.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(_op0:nou moe _op2:nou moe _op0:hatsjekidee _op2:hatsjekidee) +_isTemplate:n", query.toString());
	}

	/**
	 * 'groups' parameter multi with reviewer.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(_op0:nou moe _op2:nou moe _op0:hatsjekidee _op2:hatsjekidee) +_isTemplate:n", query.toString());
	}

	/**
	 * 'groups' parameter multi with owner.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(_op0:nou moe _op2:nou moe _op0:hatsjekidee _op2:hatsjekidee _owner:yeah!) +_isTemplate:n", query.toString());
	}

	/**
	 * 'groups' parameter multi with admin.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(_op0:nou moe _op2:nou moe _op0:hatsjekidee _op2:hatsjekidee _dummy:0) +_isTemplate:n", query.toString());
	}

	/**
	 * 'bounding box' parameter equals.
	 * TODO verify with Jose why he put the float values here.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+eastBL:[55.0 TO 55.0] +westBL:[43.0 TO 43.0] +northBL:[12.0 TO 12.0] +southBL:[9.0 TO 9.0] +_isTemplate:n", query.toString());
	}

	/**
	 * 'bounding box' parameter overlaps.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+eastBL:[43.0 TO 180.0] +westBL:[-180.0 TO 55.0] +northBL:[9.0 TO 90.0] +southBL:[-90.0 TO 12.0] +_isTemplate:n", query.toString());
	}

	/**
	 * 'bounding box' parameter encloses.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+eastBL:[55.0 TO 180.0] +westBL:[-180.0 TO 43.0] +northBL:[12.0 TO 90.0] +southBL:[-90.0 TO 9.0] +_isTemplate:n", query.toString());
	}

	/**
	 * 'bounding box' parameter fullyEnclosedWithin.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+eastBL:[43.0 TO 55.0] +westBL:[43.0 TO 55.0] +northBL:[9.0 TO 12.0] +southBL:[9.0 TO 12.0] +_isTemplate:n", query.toString());
	}

	/**
	 * 'bounding box' parameter fullyOutsideOf.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("eastBL:[-180.0 TO 43.0] westBL:[55.0 TO 180.0] northBL:[-90.0 TO 0.0] southBL:[30.0 TO 90.0] +_isTemplate:n", query.toString());
	}

	/**
	 * 'bounding box' parameter overlaps - standard values from search page.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+eastBL:[-180.0 TO 180.0] +westBL:[-180.0 TO 180.0] +northBL:[-90.0 TO 90.0] +southBL:[-90.0 TO 90.0] +_isTemplate:n", query.toString());
	}

	/**
	<request>
		<eastBL>180</eastBL>
		<title>hoi</title>
		<sortBy>popularity</sortBy>
		<southBL>-90</southBL>
		<northBL>90</northBL>
		<any />
		<similarity>1</similarity>
		<relation>overlaps</relation>
		<westBL>-180</westBL>
		<hitsPerPage>10</hitsPerPage>
		<attrset>geo</attrset>
		<group>1</group>
		<group>0</group>
		</request>
	*/
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+(_op0:1 _op2:1 _op0:0 _op2:0) +eastBL:[-180.0 TO 180.0] +westBL:[-180.0 TO 180.0] +northBL:[-90.0 TO 90.0] +southBL:[-90.0 TO 90.0] +title:hoi +_isTemplate:n", query.toString());
	}

	/**
	 * All parameters as sent by the Popular Get service when it searches for metadata. This test
	 * includes many parameters that are present, but empty.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+_isTemplate:n +eastBL:[-180.0 TO 180.0] +westBL:[-180.0 TO 180.0] +northBL:[-90.0 TO 90.0] +southBL:[-90.0 TO 90.0]", query.toString());
	}


    /**
	 * 'dynamic' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+dynamic:true +_isTemplate:n", query.toString());
    }

    /**
	 * 'download' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+download:true +_isTemplate:n", query.toString());

    }


   /**
	 * 'download' parameter.
	 */
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
		Query query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQI);
		// verify query
		assertEquals("+digital:true +_isTemplate:n", query.toString());


        // create request object with with digital=off, paper=on
		request = factory.element("request");
		any = factory.element("paper");
		any.addContent("true");
		request.addContent(any);
        // build lucene query input
        LuceneQueryInput lQIa = new LuceneQueryInput(request);
		// build lucene query
		 query = new LuceneQueryBuilder(_tokenizedFieldSet, _numericFieldSet, _analyzer).build(lQIa);
		// verify query
		assertEquals("+paper:true +_isTemplate:n", query.toString());
    }
}

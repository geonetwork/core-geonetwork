package org.fao.geonet.kernel.search;

import junit.framework.TestCase;
import org.apache.lucene.search.Query;
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

	public LuceneQueryTest(String name) {
		super(name);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+any:hoeperdepoep +_isTemplate:n", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(topicCat:biota*) +_isTemplate:n", query.toString());
	}

	/**
	 * 'topic-category' parameter with multiple values.
	 */
	public void testMultipleTopicCategories() {
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(topicCat:biota* topicCat:boundaries* topicCat:environment*) +_isTemplate:n", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +changeDate:[12-05-1989 TO *]", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +changeDate:[* TO 12-05-1989T23:59:59]", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +changeDate:[11-05-1989 TO 12-05-1989T23:59:59]", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +source:f74e4ccf-755a-48ef-bedf-990f9872298b", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +title:humph", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +type:dataset", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +(keyword:hoeperdepoep)", query.toString());
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
		any2.addContent("zat op de stoep");
		request.addContent(any2);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +(keyword:hoeperdepoep) +(keyword:zat op de stoep)", query.toString());
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
        // build lucene query
        Query query = new LuceneQueryBuilder().build(request);
        // verify query
        assertEquals("+_isTemplate:n +(keyword:hoeperdepoep keyword:zat op de stoep)", query.toString());

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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(+(_cat:hoeperdepoep)) +_isTemplate:n", query.toString());
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
		any2.addContent("zat op de stoep");
		request.addContent(any2);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(+(_cat:hoeperdepoep) +(_cat:zat _cat:op _cat:de _cat:stoep)) +_isTemplate:n", query.toString());
	}

	/**
	 * 'groupOwner' parameter with a single value.
	 */
	public void testSingleGroupOwner() {
		// create request object
		JDOMFactory factory = new DefaultJDOMFactory();
		Element request = factory.element("request");
		Element any = factory.element("groupOwner");
		any.addContent("JanMetDeKorteNaam");
		request.addContent(any);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_groupOwner:JanMetDeKorteNaam +_isTemplate:n", query.toString());
	}

	/**
	 * 'groupOwner' parameter with multiple values.
	 */
	public void testMultipleGroupOwner() {
		// create request object
		JDOMFactory factory = new DefaultJDOMFactory();
		Element request = factory.element("request");
		Element any = factory.element("groupOwner");
		any.addContent("JanMetDeKorteNaam");
		request.addContent(any);
		Element any2 = factory.element("groupOwner");
		any2.addContent("GregoriusMetDeLangeNaam");
		request.addContent(any2);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_groupOwner:JanMetDeKorteNaam +_groupOwner:GregoriusMetDeLangeNaam +_isTemplate:n", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_op6:1 +_op0:1 +_isTemplate:n", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(_op0:hatsjekidee) +_isTemplate:n", query.toString());
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
		request.addContent(any2);		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(_op0:hatsjekidee _op0:nou moe) +_isTemplate:n", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(_op0:hatsjekidee _op0:nou moe _groupOwner:hatsjekidee _groupOwner:nou moe) +_isTemplate:n", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(_op0:hatsjekidee _op0:nou moe _owner:yeah!) +_isTemplate:n", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(_op0:hatsjekidee _op0:nou moe _dummy:0) +_isTemplate:n", query.toString());
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
		anyN.addContent("120");
		request.addContent(anyN);
		Element anyS = factory.element("southBL");
		anyS.addContent("90");
		request.addContent(anyS);
		Element anyR = factory.element("relation");
		anyR.addContent("equal");
		request.addContent(anyR);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +eastBL:[414.0 TO 416.0] +westBL:[402.0 TO 404.0] +northBL:[479.0 TO 481.0] +southBL:[449.0 TO 451.0]", query.toString());
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
		anyN.addContent("120");
		request.addContent(anyN);
		Element anyS = factory.element("southBL");
		anyS.addContent("90");
		request.addContent(anyS);
		Element anyR = factory.element("relation");
		anyR.addContent("overlaps");
		request.addContent(anyR);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +eastBL:[404.0 TO 540] +westBL:[180 TO 414.0] +northBL:[451.0 TO 450] +southBL:[270 TO 479.0]", query.toString());
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
		anyN.addContent("120");
		request.addContent(anyN);
		Element anyS = factory.element("southBL");
		anyS.addContent("90");
		request.addContent(anyS);
		Element anyR = factory.element("relation");
		anyR.addContent("encloses");
		request.addContent(anyR);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +eastBL:[414.0 TO 540] +westBL:[180 TO 404.0] +northBL:[479.0 TO 450] +southBL:[270 TO 451.0]", query.toString());
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
		anyN.addContent("120");
		request.addContent(anyN);
		Element anyS = factory.element("southBL");
		anyS.addContent("90");
		request.addContent(anyS);
		Element anyR = factory.element("relation");
		anyR.addContent("fullyEnclosedWithin");
		request.addContent(anyR);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +eastBL:[402.0 TO 416.0] +westBL:[402.0 TO 416.0] +northBL:[449.0 TO 481.0] +southBL:[449.0 TO 481.0]", query.toString());
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
		anyN.addContent("120");
		request.addContent(anyN);
		Element anyS = factory.element("southBL");
		anyS.addContent("90");
		request.addContent(anyS);
		Element anyR = factory.element("relation");
		anyR.addContent("fullyOutsideOf");
		request.addContent(anyR);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n eastBL:[180 TO 404.0] +westBL:[414.0 TO 540] +northBL:[270 TO 451.0] +southBL:[479.0 TO 540]", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +eastBL:[181.0 TO 540] +westBL:[180 TO 539.0] +northBL:[271.0 TO 450] +southBL:[270 TO 449.0]", query.toString());
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
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(_op0:0 _op0:1) +_isTemplate:n +title:hoi +eastBL:[181.0 TO 540] +westBL:[180 TO 539.0] +northBL:[271.0 TO 450] +southBL:[270 TO 449.0]", query.toString());
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
		Element download = factory.element("download");
		download.addContent("off");
		request.addContent(download);
		Element dynamic = factory.element("dynamic");
		dynamic.addContent("off");
		request.addContent(dynamic);
		Element digital = factory.element("digital");
		digital.addContent("off");
		request.addContent(digital);
		Element paper = factory.element("paper");
		paper.addContent("off");
		request.addContent(paper);
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
		Element sortBySelect = factory.element("sortBySelect");
		sortBySelect.addContent("date");
		request.addContent(sortBySelect);

		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +eastBL:[181.0 TO 540] +westBL:[180 TO 539.0] +northBL:[271.0 TO 450] +southBL:[270 TO 449.0]", query.toString());
	}


    /**
	 * 'dynamic' parameter.
	 */
	public void testDynamic() {
		// create request object with dynamic=on
		JDOMFactory factory = new DefaultJDOMFactory();
		Element request = factory.element("request");
		Element any = factory.element("dynamic");
		any.addContent("on");
		request.addContent(any);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(protocol:OGC:WMS-*-get-map protocol:OGC:WMS-*-get-capabilities protocol:ESRI:AIMS-*-get-image) +_isTemplate:n", query.toString());


        // create request object with dynamic=off
		request = factory.element("request");
		any = factory.element("dynamic");
		any.addContent("off");
        request.addContent(any);
        Element template = factory.element("template");
		template.addContent("y");
		request.addContent(template);
		// build lucene query
		 query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:y", query.toString());
    }

    /**
	 * 'download' parameter.
	 */
	public void testDownload() {
		// create request object with dynamic=on
		JDOMFactory factory = new DefaultJDOMFactory();
		Element request = factory.element("request");
		Element any = factory.element("download");
		any.addContent("on");
		request.addContent(any);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+(protocol:WWW:DOWNLOAD-*--download) +_isTemplate:n", query.toString());


        // create request object with dynamic=off
		request = factory.element("request");
		any = factory.element("download");
		any.addContent("off");
        request.addContent(any);
        Element template = factory.element("template");
		template.addContent("y");
		request.addContent(template);
		// build lucene query
		 query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:y", query.toString());
    }


   /**
	 * 'download' parameter.
	 */
	public void testDigitalAndPaper() {
		// create request object with digital=on, paper=off
		JDOMFactory factory = new DefaultJDOMFactory();
		Element request = factory.element("request");
		Element any = factory.element("digital");
		any.addContent("on");
		request.addContent(any);
        any = factory.element("paper");
		any.addContent("off");
		request.addContent(any);
		// build lucene query
		Query query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +digital:true", query.toString());


        // create request object with with digital=off, paper=on
		request = factory.element("request");
		any = factory.element("digital");
		any.addContent("off");
		request.addContent(any);
        any = factory.element("paper");
		any.addContent("on");
		request.addContent(any);
		// build lucene query
		 query = new LuceneQueryBuilder().build(request);
		// verify query
		assertEquals("+_isTemplate:n +paper:true", query.toString());

        // create request object with with digital=off, paper=off
        request = factory.element("request");
        any = factory.element("digital");
        any.addContent("off");
        request.addContent(any);
        any = factory.element("paper");
        any.addContent("off");
        request.addContent(any);
        // build lucene query
        query = new LuceneQueryBuilder().build(request);
        // verify query
        assertEquals("+_isTemplate:n", query.toString());

        // create request object with with digital=on, paper=on
        request = factory.element("request");
        any = factory.element("digital");
        any.addContent("on");
        request.addContent(any);
        any = factory.element("paper");
        any.addContent("on");
        request.addContent(any);
        // build lucene query
        query = new LuceneQueryBuilder().build(request);
        // verify query
        assertEquals("+_isTemplate:n", query.toString());

    }
}
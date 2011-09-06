//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.csw.services.getrecords;

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.LuceneUtils;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.jdom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

//=============================================================================

public class CatalogSearcher {
	private Element _summaryConfig;
	private LuceneConfig	_luceneConfig;
	private HashSet<String> _tokenizedFieldSet;
	private HashMap<String, LuceneConfigNumericField> _numericFieldSet;
	private FieldSelector _selector;
	private Query         _query;
	private volatile IndexReader   _reader;
	private CachingWrapperFilter _filter;
	private Sort          _sort;
	private String        _lang;
	
	public CatalogSearcher(File summaryConfig, LuceneConfig luceneConfig) {
		try {
			if (summaryConfig != null)
				_summaryConfig = Xml.loadStream(new FileInputStream(
						summaryConfig));
		} catch (Exception e) {
			throw new RuntimeException(
					"Error reading summary configuration file", e);
		}

		_luceneConfig = luceneConfig;
		_tokenizedFieldSet = luceneConfig.getTokenizedField();
		_numericFieldSet = luceneConfig.getNumericFields();
		
		_selector = new FieldSelector() {
			public final FieldSelectorResult accept(String name) {
				if (name.equals("_id")) return FieldSelectorResult.LOAD;
				else return FieldSelectorResult.NO_LOAD;
			}
		};
	}

	public void reloadLuceneConfiguration(LuceneConfig lc) {
		_tokenizedFieldSet = lc.getTokenizedField();
		_numericFieldSet = lc.getNumericFields();
	}
	
	// ---------------------------------------------------------------------------
	// ---
	// --- Main search method
	// ---
	// ---------------------------------------------------------------------------

	/**
	 * Convert a filter to a lucene search and run the search.
	 * 
	 * @return a list of id that match the given filter, ordered by sortFields
	 */
	public Pair<Element, List<ResultItem>> search(ServiceContext context,
                                                  Element filterExpr, String filterVersion,
                                                  Sort sort, ResultType resultType, int startPosition, int maxRecords,
                                                  int maxHitsInSummary)
			throws CatalogException {
		Element luceneExpr = filterToLucene(context, filterExpr);

		Log.debug(Geonet.CSW_SEARCH, "CatS: after filter2lucene:\n"+ Xml.getString(luceneExpr));

		if (luceneExpr != null) {
			checkForErrors(luceneExpr);
			remapFields(luceneExpr);
		}

		Log.debug(Geonet.CSW_SEARCH, "CatS: after remapfields:\n"+ Xml.getString(luceneExpr));


		try {
			if (luceneExpr != null) {
				convertPhrases(luceneExpr);
				Log.debug(Geonet.CSW_SEARCH, "CatS: after convertphrases:\n"+ Xml.getString(luceneExpr));
			}

            return performSearch(context,
                    luceneExpr, filterExpr, filterVersion, sort, resultType,
                    startPosition, maxRecords, maxHitsInSummary);
		} catch (Exception e) {
			Log.error(Geonet.CSW_SEARCH, "Error while searching metadata ");
			Log.error(Geonet.CSW_SEARCH, "  (C) StackTrace:\n"
					+ Util.getStackTrace(e));

			throw new NoApplicableCodeEx(
					"Raised exception while searching metadata : " + e);
		}
	}

	// ---------------------------------------------------------------------------
	/**
	 * <p>
	 * Gets results in current searcher
	 * </p>
	 * 
	 * @return current searcher result in "fast" mode
	 * 
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public List<String> getAllUuids(int maxHits) throws Exception {

		FieldSelector uuidselector = new FieldSelector() {
			public final FieldSelectorResult accept(String name) {
				if (name.equals("_uuid")) return FieldSelectorResult.LOAD;
				else return FieldSelectorResult.NO_LOAD;
			}
		};

        Pair<TopDocs, Element> searchResults =
			LuceneSearcher.doSearchAndMakeSummary( 
					maxHits, 0, maxHits, Integer.MAX_VALUE, 
					_lang, ResultType.RESULTS.toString(), _summaryConfig, 
					_reader, _query, _filter, _sort, false,
					_luceneConfig.isTrackDocScores(), _luceneConfig.isTrackMaxScore(), _luceneConfig.isDocsScoredInOrder()
			);
		TopDocs tdocs = searchResults.one();
		Element summary = searchResults.two();

		int numHits = Integer.parseInt(summary.getAttributeValue("count"));

		Log.debug(Geonet.CSW_SEARCH, "Records matched : " + numHits);

		// --- retrieve results
		List<String> response = new ArrayList<String>();
		
		for ( ScoreDoc sdoc : tdocs.scoreDocs ) {
			Document doc = _reader.document(sdoc.doc, uuidselector);
			String uuid = doc.get("_uuid");
			if (uuid != null) response.add(uuid);
		}
		return response;
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- Private methods
	// ---
	// ---------------------------------------------------------------------------

	/**
	 * Use filter-to-lucene stylesheet to create a Lucene search query to be
	 * used by LuceneSearcher.
	 * 
	 * @return XML representation of Lucene query
	 */
	private Element filterToLucene(ServiceContext context, Element filterExpr)
			throws NoApplicableCodeEx {
		if (filterExpr == null)
			return null;

		String styleSheet = context.getAppPath() + Geonet.Path.CSW
				+ Geonet.File.FILTER_TO_LUCENE;

		try {
			Element result = Xml.transform(filterExpr, styleSheet);
            Log.info(Geonet.CSW_SEARCH,"filterToLucene result:\n" + Xml.getString(result));
            return result;

		}
        catch (Exception e) {
			context.error("Error during Filter to Lucene conversion : " + e);
			context.error("  (C) StackTrace\n" + Util.getStackTrace(e));

			throw new NoApplicableCodeEx(
					"Error during Filter to Lucene conversion : " + e);
		}
	}

	// ---------------------------------------------------------------------------

	private void checkForErrors(Element elem) throws InvalidParameterValueEx {
		List children = elem.getChildren();

		if (elem.getName().equals("error")) {
			String type = elem.getAttributeValue("type");
			String oper = Xml.getString((Element) children.get(0));

			throw new InvalidParameterValueEx(type, oper);
		}

        for (Object aChildren : children) {
            checkForErrors((Element) aChildren);
        }
	}


	// ---------------------------------------------------------------------------

	private void convertPhrases(Element elem)
			throws InterruptedException, IOException {
		if (elem.getName().equals("TermQuery")) {
			String field = elem.getAttributeValue("fld");
			String text = elem.getAttributeValue("txt");

			if (_tokenizedFieldSet.contains(field) && text.indexOf(' ') != -1) {
				elem.setName("PhraseQuery");

				StringTokenizer st = new StringTokenizer(text, " ");

				while (st.hasMoreTokens()) {
					Element term = new Element("TermQuery");
					term.setAttribute("fld", field);
					term.setAttribute("txt", st.nextToken());

					elem.addContent(term);
				}
			}
		}

		else {
			List children = elem.getChildren();

            for (Object aChildren : children) {
                convertPhrases((Element) aChildren);
            }
		}
	}

	// ---------------------------------------------------------------------------
	/**
	 * Map OGC CSW search field names to Lucene field names using
	 * {@link FieldMapper}. If a field name is not defined then the any (ie.
	 * full text) criteria is used.
	 * 
	 */
	private void remapFields(Element elem) {
		String field = elem.getAttributeValue("fld");

		if (field != null) {
			if (field.equals(""))
				field = "any";

			String mapped = FieldMapper.map(field);

			if (mapped != null)
				elem.setAttribute("fld", mapped);
			else
				Log.info(Geonet.CSW_SEARCH, "Unknown queryable field : "
						+ field); // FIXME log doesn't work
		}

		List children = elem.getChildren();

        for (Object aChildren : children) {
            remapFields((Element) aChildren);
        }
	}

	// ---------------------------------------------------------------------------

	private Pair<Element, List<ResultItem>> performSearch(
			ServiceContext context, Element luceneExpr, Element filterExpr,
			String filterVersion, Sort sort, ResultType resultType, 
			int startPosition, int maxRecords, int maxHitsInSummary) throws Exception {

        if (filterExpr != null) {
            Log.debug(Geonet.CSW_SEARCH, "CatS performsearch: filterXpr:\n"+ Xml.getString(filterExpr));
        }


		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getSearchmanager();
		UserSession session = context.getUserSession();

		// if this is a new search or request has changed them release indexreader
		// and get a (reopened) indexreader
		String requestId = Util.scramble(Xml.getString(filterExpr));
		String sessionRequestId = (String) session.getProperty(Geonet.Session.SEARCH_REQUEST_ID);
		if ((sessionRequestId != null && !sessionRequestId.equals(requestId)) || sessionRequestId == null) {
			Log.debug(Geonet.CSW_SEARCH, "Query changed, reopening IndexReader");
			synchronized(this) {
				if (_reader != null) sm.releaseIndexReader(_reader);
					_reader = sm.getIndexReader();
			}
		}

		if (luceneExpr != null) {
			Log.debug(Geonet.CSW_SEARCH, "Search criteria:\n" + Xml.getString(luceneExpr));
        }
		Query data = (luceneExpr == null) ? null : LuceneSearcher.makeQuery(luceneExpr, SearchManager.getAnalyzer(), _tokenizedFieldSet, _numericFieldSet);
        Log.info(Geonet.CSW_SEARCH,"LuceneSearcher made query:\n" + data.toString());


		Query groups = getGroupsQuery(context);

		if (sort == null) {
			sort = LuceneSearcher.makeSort(Collections.singletonList(Pair.read(Geonet.SearchResult.SortBy.RELEVANCE, true)));
		}

		// --- put query on groups in AND with lucene query

		BooleanQuery query = new BooleanQuery();

		// FIXME : DO I need to fix that here ???
		// BooleanQuery.setMaxClauseCount(1024); // FIXME : using MAX_VALUE
		// solve
		// // partly the org.apache.lucene.search.BooleanQuery$TooManyClauses
		// // problem.
		// // Improve index content.

		BooleanClause.Occur occur = LuceneUtils
				.convertRequiredAndProhibitedToOccur(true, false);
		if (data != null)
			query.add(data, occur);

		query.add(groups, occur);

		// --- proper search
		Log.debug(Geonet.CSW_SEARCH, "Lucene query: " + query.toString());

		// TODO Handle NPE creating spatial filter (due to constraint
		// language version).
		Filter spatialfilter = sm.getSpatial().filter(query, filterExpr, filterVersion);
		CachingWrapperFilter cFilter = null;
		if (spatialfilter != null) cFilter = new CachingWrapperFilter(spatialfilter);
		boolean buildSummary = resultType == ResultType.RESULTS_WITH_SUMMARY;
		int numHits = startPosition + maxRecords;

			// get as many results as instructed or enough for search summary
		if (buildSummary) {
			numHits = Math.max(maxHitsInSummary, numHits);
		}

		// record globals for reuse
		_query = query;
		_filter = cFilter;
		_sort = sort;
		_lang = context.getLanguage();
	
		Pair<TopDocs,Element> searchResults = LuceneSearcher.doSearchAndMakeSummary(
				numHits, startPosition - 1, maxRecords, Integer.MAX_VALUE, 
				_lang, resultType.toString(), _summaryConfig, 
				_reader, query, cFilter, sort, buildSummary,
				
				_luceneConfig.isTrackDocScores(), _luceneConfig.isTrackMaxScore(), _luceneConfig.isDocsScoredInOrder()		
		);
		TopDocs hits = searchResults.one();
		Element summary = searchResults.two();

		numHits = Integer.parseInt(summary.getAttributeValue("count"));
	
		Log.debug(Geonet.CSW_SEARCH, "Records matched : " + numHits);
		
		// --- retrieve results

		List<ResultItem> results = new ArrayList<ResultItem>();

		// Get hits according to request (when using summary topdocs returned by
		// LuceneSearcher already contains all docs)
		int i = 0;
		int iMax = hits.scoreDocs.length;
		if (buildSummary) {
			i = startPosition -1;
			iMax = Math.min(hits.scoreDocs.length, i + maxRecords); 
		}
		
		for (;i < iMax; i++) {
			Document doc = _reader.document(hits.scoreDocs[i].doc, _selector);
			String id = doc.get("_id");
	
			ResultItem ri = new ResultItem(id);
			results.add(ri);
	
			for (String field : FieldMapper.getMappedFields()) {
				String value = doc.get(field);

				if (value != null)
					ri.add(field, value);
			}
		}

		summary.setName("Summary");
		summary.setNamespace(Csw.NAMESPACE_GEONET);
		return Pair.read(summary, results);
	}

	// ---------------------------------------------------------------------------

	/**
	 * Allow search on current user's groups only adding a BooleanClause to the
	 * search.
	 */
	public static Query getGroupsQuery(ServiceContext context) throws Exception {
		Dbms dbms = (Dbms) context.getResourceManager()
				.open(Geonet.Res.MAIN_DB);

		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		AccessManager am = gc.getAccessManager();
		Set<String> hs = am.getUserGroups(dbms, context.getUserSession(),
				context.getIpAddress());

		BooleanQuery query = new BooleanQuery();

		String operView = "_op0";

		BooleanClause.Occur occur = LuceneUtils
				.convertRequiredAndProhibitedToOccur(false, false);
		for (Object group : hs) {
			TermQuery tq = new TermQuery(new Term(operView, group.toString()));
			query.add(tq, occur);
		}

		// If user is authenticated, add the current user to the query because
		// if an editor unchecked all
		// visible options in privileges panel for all groups, then the metadata
		// records could not be found anymore, even by its editor.
		if (context.getUserSession().getUserId() != null) {
			TermQuery tq = new TermQuery(new Term("_owner", context
					.getUserSession().getUserId()));
			query.add(tq, occur);
		}

		return query;
	}

}

// =============================================================================

/**
 * Class containing result items with information retrieved from Lucene index.
 */
class ResultItem {
	/**
	 * Metadata identifier
	 */
	private String id;

	/**
	 * Other Lucene index information declared in {@link FieldMapper}
	 */
	private HashMap<String, String> hmFields = new HashMap<String, String>();

	// ---------------------------------------------------------------------------
	// ---
	// --- Constructor
	// ---
	// ---------------------------------------------------------------------------

	public ResultItem(String id) {
		this.id = id;
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- API methods
	// ---
	// ---------------------------------------------------------------------------

	public String getID() {
		return id;
	}

	// ---------------------------------------------------------------------------

	public void add(String field, String value) {
		hmFields.put(field, value);
	}

	// ---------------------------------------------------------------------------

	public String getValue(String field) {
		return hmFields.get(field);
	}
}

// =============================================================================

// =============================================================================

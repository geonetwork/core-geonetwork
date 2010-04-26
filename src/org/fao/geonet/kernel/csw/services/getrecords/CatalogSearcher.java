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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
import org.apache.lucene.search.TopFieldCollector;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.TypeName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.LuceneUtils;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.Pair;

import org.fao.geonet.services.util.MainUtil;
import org.jdom.Element;

//=============================================================================

public class CatalogSearcher {
	private Element _summaryConfig;
	private Map<String, Boolean> _isTokenizedField = new HashMap<String, Boolean>();
	private FieldSelector _selector;
	private Query         _query;
	private CachingWrapperFilter _filter;
	private Sort          _sort;
	private String        _lang;

	public CatalogSearcher(File summaryConfig) {
		try {
			if (summaryConfig != null)
				_summaryConfig = Xml.loadStream(new FileInputStream(
						summaryConfig));
		} catch (Exception e) {
			throw new RuntimeException(
					"Error reading summary configuration file", e);
		}

		_selector = new FieldSelector() {
			public final FieldSelectorResult accept(String name) {
				if (name.equals("_id")) return FieldSelectorResult.LOAD;
				else return FieldSelectorResult.NO_LOAD;
			}
		};
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
			Element filterExpr, String filterVersion, Set<TypeName> typeNames,
			Sort sort, ResultType resultType, int startPosition, int maxRecords,
			int maxHitsInSummary)
			throws CatalogException {
		Element luceneExpr = filterToLucene(context, filterExpr);

		if (luceneExpr != null) {
			checkForErrors(luceneExpr);
			remapFields(luceneExpr);
            processFieldsAnalyzer(luceneExpr);
		}
		
		try {
			if (luceneExpr != null) {
				convertPhrases(luceneExpr, context);
			}

			Pair<Element, List<ResultItem>> results = performSearch(context,
					luceneExpr, filterExpr, filterVersion, sort, resultType,
					startPosition, maxRecords, maxHitsInSummary);
			return results;
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
	public List<String> getAllUuids(ServiceContext context, int maxHits) throws Exception {

		FieldSelector uuidselector = new FieldSelector() {
			public final FieldSelectorResult accept(String name) {
				if (name.equals("_uuid")) return FieldSelectorResult.LOAD;
				else return FieldSelectorResult.NO_LOAD;
			}
		};

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getSearchmanager();
		IndexReader reader = sm.getIndexReader();
		try {
			Pair<TopFieldCollector,Element> searchResults = LuceneSearcher.doSearchAndMakeSummary( maxHits, Integer.MAX_VALUE, _lang, ResultType.RESULTS.toString(), _summaryConfig, reader, _query, _filter, _sort, false);
			TopFieldCollector tfc = searchResults.one();
			Element summary = searchResults.two();

			int numHits = Integer.parseInt(summary.getAttributeValue("count"));

			Log.debug(Geonet.CSW_SEARCH, "Records matched : " + numHits);

			// --- retrieve results

			List<String> response = new ArrayList<String>();
			TopDocs tdocs = tfc.topDocs(0, maxHits);

			for ( ScoreDoc sdoc : tdocs.scoreDocs ) {
				Document doc = reader.document(sdoc.doc, uuidselector);
				String uuid = doc.get("_uuid");
				if (uuid != null) response.add(uuid);
			}
			return response;
		} finally {
			sm.releaseIndexReader(reader);
		}
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
			return Xml.transform(filterExpr, styleSheet);
		} catch (Exception e) {
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

		for (int i = 0; i < children.size(); i++)
			checkForErrors((Element) children.get(i));
	}

	// ---------------------------------------------------------------------------

	// Only tokenize field must be converted
	// TODO add token parameter in CSW conf for each field, may be useful for
	// GetDomain operation too.
	private void convertPhrases(Element elem, ServiceContext context)
			throws InterruptedException, CorruptIndexException, IOException {
		if (elem.getName().equals("TermQuery")) {
			String field = elem.getAttributeValue("fld");
			String text = elem.getAttributeValue("txt");

			boolean isTokenized = isTokenized(field, context);
			if (isTokenized && text.indexOf(" ") != -1) {
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

			for (int i = 0; i < children.size(); i++)
				convertPhrases((Element) children.get(i), context);
		}
	}

	// ---------------------------------------------------------------------------
	private boolean isTokenized(String field, ServiceContext context) throws CorruptIndexException, IOException, InterruptedException {
		Boolean tokenized = _isTokenizedField.get(field);
		if (tokenized != null) {
			return tokenized.booleanValue();
		}
		GeonetContext gc = (GeonetContext) context
				.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getSearchmanager();
		IndexReader reader = sm.getIndexReader();
		try {
			int i = 0;
			while (i < reader.numDocs() && tokenized == null) {
				if (reader.isDeleted(i)) {
					i++;
					continue; // FIXME: strange lucene hack: sometimes it tries
					// to load a deleted document
				}
				Document doc = reader.document(i);
				Field tmp = doc.getField(field);
				if (tmp != null) {
					tokenized = tmp.isTokenized();
				}
				i++;
			}
	
			if (tokenized != null) {
				_isTokenizedField.put(field, tokenized.booleanValue());
				return tokenized.booleanValue();
			}
			_isTokenizedField.put(field, false);
			return false;
		} finally {
			sm.releaseIndexReader(reader);
		}
	}

    /**
     * Process the fields title, abstract and any in same way as done
     * in org.fao.geonet.services.main.Search service
     *
     * @param elem
     */
    private void processFieldsAnalyzer(Element elem) {
        String field = elem.getAttributeValue("fld");
        if (field != null) {
            if ((field.equals(Geonet.SearchResult.TITLE)) ||
                    (field.equals(Geonet.SearchResult.ABSTRACT)) ||
                    (field.equals(Geonet.SearchResult.ANY))) {
                 elem.setAttribute("txt", MainUtil.splitWord( elem.getAttributeValue("txt")));
            }
        }

        List children = elem.getChildren();

		for (int i = 0; i < children.size(); i++)
			processFieldsAnalyzer((Element) children.get(i));
    }

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

		for (int i = 0; i < children.size(); i++)
			remapFields((Element) children.get(i));
	}

	// ---------------------------------------------------------------------------

	private Pair<Element, List<ResultItem>> performSearch(
			ServiceContext context, Element luceneExpr, Element filterExpr,
			String filterVersion, Sort sort, ResultType resultType, 
			int startPosition, int maxRecords, int maxHitsInSummary) throws Exception {

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getSearchmanager();

		if (luceneExpr != null)
			Log.debug(Geonet.CSW_SEARCH, "Search criteria:\n"
					+ Xml.getString(luceneExpr));

		Query data = (luceneExpr == null) ? null : LuceneSearcher
				.makeQuery(luceneExpr);
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

		IndexReader reader = sm.getIndexReader();
		try {

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
	
			Pair<TopFieldCollector,Element> searchResults = LuceneSearcher.doSearchAndMakeSummary(numHits, Integer.MAX_VALUE, context.getLanguage(), resultType.toString(), _summaryConfig, reader, query, cFilter, sort, buildSummary);
			TopFieldCollector tfc = searchResults.one();
			Element summary = searchResults.two();

			numHits = Integer.parseInt(summary.getAttributeValue("count"));
	
			Log.debug(Geonet.CSW_SEARCH, "Records matched : " + numHits);

			// --- retrieve results

			List<ResultItem> results = new ArrayList<ResultItem>();
			// FIXME : topDocs could have been used already once in MakeSummary
			// so the second call return null docs. resultType=results return
			// record, but results_with_summary does not.
			TopDocs hits = tfc.topDocs(startPosition - 1, maxRecords);
	
			for (int i = 0; i < hits.scoreDocs.length; i++) {
				Document doc = reader.document(hits.scoreDocs[i].doc, _selector);
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
		} finally {
			sm.releaseIndexReader(reader);
		}
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

/**
 * Used to sort search results
 * 
 * comment francois : could we use {@link Sort} instead ?
 */
class ItemComparator implements Comparator<ResultItem> {
	private List<SortField> sortFields;

	// ---------------------------------------------------------------------------
	// ---
	// --- Constructor
	// ---
	// ---------------------------------------------------------------------------

	public ItemComparator(List<SortField> sf) {
		sortFields = sf;
	}

	// ---------------------------------------------------------------------------
	// ---
	// --- Comparator interface
	// ---
	// ---------------------------------------------------------------------------

	public int compare(ResultItem ri1, ResultItem ri2) {
		for (SortField sf : sortFields) {
			String value1 = ri1.getValue(sf.field);
			String value2 = ri2.getValue(sf.field);

			// --- some metadata may have null values for some fields
			// --- in this case we push null values at the bottom

			if (value1 == null && value2 != null)
				return 1;

			if (value1 != null && value2 == null)
				return -1;

			if (value1 == null || value2 == null)
				return 0;

			// --- values are ok, do a proper comparison

			int comp = value1.compareTo(value2);

			if (comp == 0)
				continue;

			return (!sf.descend) ? comp : -comp;
		}

		return 0;
	}
}

// =============================================================================

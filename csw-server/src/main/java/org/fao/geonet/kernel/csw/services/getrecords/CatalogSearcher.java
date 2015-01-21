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

import com.vividsolutions.jts.geom.Geometry;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldType.NumericType;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.queryparser.flexible.standard.config.NumericConfig;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.SearchExpiredEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.region.Region;
import org.fao.geonet.kernel.region.RegionsDAO;
import org.fao.geonet.kernel.search.DuplicateDocFilter;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.search.LuceneIndexField;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.LuceneUtils;
import org.fao.geonet.kernel.search.MetadataRecordSelector;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.xml.Encoder;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.springframework.context.ApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.annotation.Nonnull;

//=============================================================================

public class CatalogSearcher implements MetadataRecordSelector {
	private static final class FindRegionFilterElements implements org.jdom.filter.Filter {
        private static final Namespace namespace = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        private static final long serialVersionUID = 1L;

        public boolean matches(Object obj)
        {
            if (obj instanceof Element) {
                Element element = (Element) obj;
                Attribute attribute = element.getAttribute("id", namespace);
                return attribute != null && attribute.getValue() != null;
            }
            return false;
        }
    }

//	private final Set<String> _tokenizedFieldSet;
	private final Set<String> _selector;
	private final Set<String> _uuidselector;
	private Query         _query;
	private Filter _filter;
	private Sort          _sort;
	private LuceneSearcher.LanguageSelection _lang;
	private long          _searchToken;
    private final GMLConfiguration   _configuration;
    private ApplicationContext _applicationContext;
	
	public CatalogSearcher(GMLConfiguration  configuration, 
			Set<String> selector, Set<String> uuidselector, ApplicationContext applicationContext) {
//		_tokenizedFieldSet = luceneConfig.getTokenizedField();
		_selector = selector;
		_configuration = configuration;
		this._applicationContext = applicationContext;
		_uuidselector = uuidselector;
		_searchToken = -1L;  // means we will get a new IndexSearcher when we
		                     // ask for it first time
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
                                                  Element filterExpr, String filterVersion, String typeName,
                                                  Sort sort, ResultType resultType, int startPosition, int maxRecords,
                                                  int maxHitsInSummary, String cswServiceSpecificContraint) throws CatalogException {
        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
            Log.debug(Geonet.CSW_SEARCH, "CatalogSearch search");
		Element luceneExpr = filterToLucene(context, filterExpr);
        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
            Log.debug(Geonet.CSW_SEARCH, "after filter2lucene:\n"+ Xml.getString(luceneExpr));

        // OGC 07-045:
        // If typeName equals to “csw:Record” no ISO metadata profile specific queryables must be used. The handling of
        // the queryables is as defined as in chapter 10.8.4.11 of [OGC 07-006].

        // If the typeNames attribute of a query equals to ‘gmd:MD_Metadata’ (‘gmd’ representing the
        // ‘http://www.isotc211.org/2005/gmd’ namespace) any queryable that is part of the associated filter must be
        // represented by a qualified name with a prefix (e.g. ‘apiso’), representing the
        // ‘http://www.opengis.net/cat/csw/apiso/1.0’ namespace. This is true for both application profile queryables as
        // well as for the OGC common core queryables (which are mapped to the gmd metadata schema then).

        // TODO typeName is not enforced: restrict query to requested typeName (in remapFields)

		if (luceneExpr != null) {
			checkForErrors(luceneExpr);
			remapFields(luceneExpr);
		}

        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
            Log.debug(Geonet.CSW_SEARCH, "after remapfields:\n"+ Xml.getString(luceneExpr));
        
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SearchManager sm = gc.getBean(SearchManager.class);
        IndexAndTaxonomy indexAndTaxonomy = null;
        try {
            if (luceneExpr != null) {
                convertPhrases(luceneExpr);
                if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                    Log.debug(Geonet.CSW_SEARCH, "after convertphrases:\n" + Xml.getString(luceneExpr));
            }
            _lang = LuceneSearcher.determineLanguage(context, filterExpr, sm.getSettingInfo());

            indexAndTaxonomy = sm.getIndexReader(_lang.presentationLanguage, _searchToken);
            Log.debug(Geonet.CSW_SEARCH, "Found searcher with " + indexAndTaxonomy.version + " comparing with " + _searchToken);
            if (_searchToken != -1L && indexAndTaxonomy.version != _searchToken) {
                throw new SearchExpiredEx("Search has expired/timed out - start a new search");
            }
            _searchToken = indexAndTaxonomy.version;
            GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;
            return performSearch(context, luceneExpr, filterExpr, filterVersion, sort, resultType, startPosition, maxRecords,
                    maxHitsInSummary, cswServiceSpecificContraint, reader, indexAndTaxonomy.taxonomyReader);
        } catch (Exception e) {
			Log.error(Geonet.CSW_SEARCH, "Error while searching metadata ");
			Log.error(Geonet.CSW_SEARCH, "  (C) StackTrace:\n" + Util.getStackTrace(e));
			throw new NoApplicableCodeEx("Raised exception while searching metadata : " + e);
        } finally {
            try {
                if (indexAndTaxonomy != null) {
                    sm.releaseIndexReader(indexAndTaxonomy);
                }
            } catch (Exception ex) {
                // eat it as it probably doesn't matter,
                // but say what happened anyway
                Log.error(Geonet.CSW_SEARCH, "Error while releasing index searcher ", ex);
            }
        }
	}

	// ---------------------------------------------------------------------------
	private LuceneConfig getLuceneConfig() {
	    return _applicationContext.getBean(LuceneConfig.class);
	}
	/**
	 * <p>
	 * Gets results in current searcher
	 * </p>
	 * @param context 
	 * 
	 * @return current searcher result in "fast" mode
	 * 
	 * @throws IOException
	 * @throws CorruptIndexException
	 */
	public List<String> getAllUuids(int maxHits, ServiceContext context) throws Exception {

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getBean(SearchManager.class);
		LuceneConfig luceneConfig = getLuceneConfig();
        IndexAndTaxonomy indexAndTaxonomy = sm.getIndexReader(null, _searchToken);

        try {
            Log.debug(Geonet.CSW_SEARCH, "Found searcher with " + indexAndTaxonomy.version + " comparing with " + _searchToken);
            if (indexAndTaxonomy.version != _searchToken && !(!luceneConfig.useNRTManagerReopenThread() || Boolean.parseBoolean(System.getProperty(LuceneConfig.USE_NRT_MANAGER_REOPEN_THREAD)))) {
                throw new SearchExpiredEx("Search has expired/timed out - start a new search");
            }
            GeonetworkMultiReader _reader = indexAndTaxonomy.indexReader;
            Pair<TopDocs, Element> searchResults = LuceneSearcher.doSearchAndMakeSummary(maxHits, 0, maxHits, _lang.presentationLanguage,
                    luceneConfig.getSummaryTypes().get(ResultType.RESULTS.toString()), luceneConfig.getTaxonomyConfiguration(),
                    _reader, _query, wrapSpatialFilter(), _sort, null, false,
                    luceneConfig.isTrackDocScores(), luceneConfig.isTrackMaxScore(), luceneConfig.isDocsScoredInOrder());
            TopDocs tdocs = searchResults.one();
            Element summary = searchResults.two();

            int numHits = Integer.parseInt(summary.getAttributeValue("count"));

            if (Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "Records matched : " + numHits);

            // --- retrieve results
            List<String> response = new ArrayList<String>();

            for (ScoreDoc sdoc : tdocs.scoreDocs) {
                Document doc = _reader.document(sdoc.doc, _uuidselector);
                String uuid = doc.get("_uuid");
                if (uuid != null)
                    response.add(uuid);
            }
            return response;
        } finally {
			sm.releaseIndexReader(indexAndTaxonomy);
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

		Path styleSheet = context.getAppPath().resolve(Geonet.Path.CSW).resolve(Geonet.File.FILTER_TO_LUCENE);

		try {
			Element result = Xml.transform(filterExpr, styleSheet);
			removeEmptyBranches(result);
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
	@SuppressWarnings("unchecked")
    private void removeEmptyBranches( Element element ) {
        List<Element> children = new ArrayList<Element>(element.getChildren());
        for( Element e : children ) {
            removeEmptyBranches(e);
        }

        if (element.getChildren().isEmpty() && element.getTextTrim().isEmpty()
                && element.getAttribute("fld") == null) {
            element.detach();
        }
	}
	// ---------------------------------------------------------------------------

	private void checkForErrors(Element elem) throws InvalidParameterValueEx {
		@SuppressWarnings("unchecked")
        List<Element> children = elem.getChildren();

		if (elem.getName().equals("error")) {
			String type = elem.getAttributeValue("type");
			String oper = "unknown";
			if (!children.isEmpty()) {
			    oper = Xml.getString((Element) children.get(0));
			}

			throw new InvalidParameterValueEx(type, oper);
		}

        for (Element aChildren : children) {
            checkForErrors(aChildren);
        }
	}


	// ---------------------------------------------------------------------------

	private void convertPhrases(Element elem)
			throws InterruptedException, IOException {
		if (elem.getName().equals("TermQuery")) {
			String field = elem.getAttributeValue("fld");
			String text = elem.getAttributeValue("txt");

			
			Set<String> tokenizedFieldSet = getLuceneConfig().getTokenizedField();
            if (tokenizedFieldSet.contains(field) && text.indexOf(' ') != -1) {
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
			@SuppressWarnings("unchecked")
            List<Element> children = elem.getChildren();

            for (Element aChildren : children) {
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
			if (field.equals("")) {
                field = "any";
            }

			String mapped = getFieldMapper().map(field);

			if (mapped != null) {
                elem.setAttribute("fld", mapped);
            } else {
                Log.info(Geonet.CSW_SEARCH, "Unknown queryable field : "
                                            + field); // FIXME log doesn't work
            }
		}

		@SuppressWarnings("unchecked")
        List<Element> children = elem.getChildren();

        for (Element aChildren : children) {
            remapFields(aChildren);
        }
	}

    private FieldMapper getFieldMapper() {
        return _applicationContext.getBean(FieldMapper.class);
    }

    // ---------------------------------------------------------------------------

    /**
     * Executes a CSW search using a filter query.
     *
     * If it is provided a service specific constraint in CswDispatcher service, the constraint is added
     * to the final search query to restrict the search.
     *
     * @param context
     * @param luceneExpr
     * @param filterExpr                    CSW Filter
     * @param filterVersion
     * @param sort
     * @param resultType
     * @param startPosition
     * @param maxRecords
     * @param maxHitsInSummary
     * @param cswServiceSpecificContraint   Service specific constraint
     * @param taxonomyReader 
     * @return
     * @throws Exception
     */
	private Pair<Element, List<ResultItem>> performSearch(ServiceContext context, Element luceneExpr,
                                                          @Nonnull Element filterExpr, String filterVersion, Sort sort,
                                                          ResultType resultType, int startPosition, int maxRecords,
                                                          int maxHitsInSummary, String cswServiceSpecificContraint,
                                                          GeonetworkMultiReader reader, TaxonomyReader taxonomyReader)
            throws Exception {

        if(Log.isDebugEnabled(Geonet.CSW_SEARCH)) {
            Log.debug(Geonet.CSW_SEARCH, "CatalogSearcher performSearch()");
        }
        if(Log.isDebugEnabled(Geonet.CSW_SEARCH)) {
            Log.debug(Geonet.CSW_SEARCH, "CatS performsearch: filterXpr:\n"+ Xml.getString(filterExpr));
        }

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getBean(SearchManager.class);

		if (luceneExpr != null) {
            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "Search criteria:\n" + Xml.getString(luceneExpr));
        }
        else {
            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "## Search criteria: null");
        }


		boolean requestedLanguageOnTop = sm.getSettingInfo().getRequestedLanguageOnTop();
		
        Query data;
        LuceneConfig luceneConfig = getLuceneConfig();
        if (luceneExpr == null) {
            data = null;
            Log.info(Geonet.CSW_SEARCH, "LuceneSearcher made null query");
        } else {
            PerFieldAnalyzerWrapper analyzer = SearchManager.getAnalyzer(_lang.analyzerLanguage, true);
            SettingInfo.SearchRequestLanguage requestedLanguageOnly = sm.getSettingInfo().getRequestedLanguageOnly();
            data = LuceneSearcher.makeLocalisedQuery(luceneExpr,
                analyzer, luceneConfig, _lang.presentationLanguage, requestedLanguageOnly);
            Log.info(Geonet.CSW_SEARCH, "LuceneSearcher made query:\n" + data.toString());
        }

        Query cswCustomFilterQuery = null;
        Log.info(Geonet.CSW_SEARCH,"LuceneSearcher cswCustomFilter:\n" + cswServiceSpecificContraint);
        if (StringUtils.isNotEmpty(cswServiceSpecificContraint)) {
            cswCustomFilterQuery = getCswServiceSpecificConstraintQuery(cswServiceSpecificContraint, luceneConfig);
            Log.info(Geonet.CSW_SEARCH,"LuceneSearcher cswCustomFilterQuery:\n" + cswCustomFilterQuery);
        }

		Query groups = getGroupsQuery(context);
		if (sort == null) {
			List<Pair<String, Boolean>> fields = Collections.singletonList(Pair.read(Geonet.SearchResult.SortBy.RELEVANCE, true));
            sort = LuceneSearcher.makeSort(fields, _lang.presentationLanguage, requestedLanguageOnTop);
		}

		// --- put query on groups in AND with lucene query

		BooleanQuery query = new BooleanQuery();

		// FIXME : DO I need to fix that here ???
		// BooleanQuery.setMaxClauseCount(1024); // FIXME : using MAX_VALUE
		// solve
		// // partly the org.apache.lucene.search.BooleanQuery$TooManyClauses
		// // problem.
		// // Improve index content.

		BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);
		if (data != null) {
			query.add(data, occur);
        }
		query.add(groups, occur);

        if (cswCustomFilterQuery != null) {
            query.add(cswCustomFilterQuery, occur);
        }

		// --- proper search
        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
            Log.debug(Geonet.CSW_SEARCH, "Lucene query: " + query.toString());

        int numHits = startPosition + maxRecords;

        updateRegionsInSpatialFilter(context, filterExpr);
		// TODO Handle NPE creating spatial filter (due to constraint)
        _filter = sm.getSpatial().filter(query, Integer.MAX_VALUE, filterExpr, filterVersion);
        
        boolean buildSummary = resultType == ResultType.RESULTS_WITH_SUMMARY;
        // get as many results as instructed or enough for search summary
        if (buildSummary) {
            numHits = Math.max(maxHitsInSummary, numHits);
        }
		// record globals for reuse
		_query = query;
		_sort = sort;
		
	    ServiceConfig config = new ServiceConfig();
	    String geomWkt = null;
	    LuceneSearcher.logSearch(context, config, _query, numHits, _sort, geomWkt, sm);

		Pair<TopDocs,Element> searchResults = LuceneSearcher.doSearchAndMakeSummary(numHits, startPosition - 1,
                maxRecords, _lang.presentationLanguage,
                luceneConfig.getSummaryTypes().get(resultType.toString()), luceneConfig.getTaxonomyConfiguration(),
                reader, _query, wrapSpatialFilter(),
                _sort, taxonomyReader, buildSummary, luceneConfig.isTrackDocScores(), luceneConfig.isTrackMaxScore(),
                luceneConfig.isDocsScoredInOrder()
		);
		TopDocs hits = searchResults.one();
		Element summary = searchResults.two();

		numHits = Integer.parseInt(summary.getAttributeValue("count"));
        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
            Log.debug(Geonet.CSW_SEARCH, "Records matched : " + numHits);
		
		// --- retrieve results

		List<ResultItem> results = new ArrayList<ResultItem>();

		// Get hits according to request (when using summary topdocs returned by
		// LuceneSearcher already contains all docs)
		int i = 0;
		int iMax = hits.scoreDocs.length;
		for (;i < iMax; i++) {
			Document doc = reader.document(hits.scoreDocs[i].doc, _selector);
			String id = doc.get("_id");
			ResultItem ri = new ResultItem(id);
			results.add(ri);
			for (String field : getFieldMapper().getMappedFields()) {
				String value = doc.get(field);
				if (value != null) {
					ri.add(field, value);
                }
			}
		}
		summary.setName("Summary");
		summary.setNamespace(Csw.NAMESPACE_GEONET);
		return Pair.read(summary, results);
	}
    // ---------------------------------------------------------------------------

    private Filter wrapSpatialFilter() {
        Filter duplicateRemovingFilter = new DuplicateDocFilter(_query);
        Filter cFilter = null;
        if (_filter == null) {
            cFilter = duplicateRemovingFilter;
        }
        else {
            Filter[] filters = new Filter[]{duplicateRemovingFilter, _filter };
            cFilter = new ChainedFilter(filters, ChainedFilter.AND);
        }
        cFilter = new CachingWrapperFilter(cFilter);
        return cFilter;
    }

	/**
	 * Process all spatial filters by replacing the region placeholders (filters with gml:id starting with 'region:')
	 * with the gml of the actual region geometry.
	 * 
	 * @param context
	 * @param filterExpr
	 * @throws Exception
	 */
	private void updateRegionsInSpatialFilter(ServiceContext context, Element filterExpr) throws Exception {
	    Collection<RegionsDAO> regionDAOs = context.getApplicationContext().getBeansOfType(RegionsDAO.class).values();
        for( Element regionEl: (List<Element>) lookupGeoms(filterExpr)) {
            Attribute attribute = regionEl.getAttribute("id", FindRegionFilterElements.namespace);
            Geometry unionedGeom = null;
            List<Geometry> geoms = new ArrayList<Geometry>();
            String[] regionIds = attribute.getValue().substring("region:".length()).split("\\s*,\\s*");

            for (String regionId : regionIds) {
                for (RegionsDAO regionDAO: regionDAOs) {
                    Geometry geometry = regionDAO.getGeom(context, regionId, false, Region.WGS84);
                    if(geometry!=null) {
                        geoms.add(geometry);
                        if (unionedGeom == null) {
                            unionedGeom = geometry;
                        } else {
                            unionedGeom = unionedGeom.union(geometry);
                        }
                        break; // break out of looking through all RegionDAOs
                    }
                }
            }
            
            updateWithinFilter(regionEl, geoms);

            setGeom(regionEl, unionedGeom);
        }
        
    }
	/**
	 * If the filter is a within filter then we want to break the within into a few parts.  
	 * 
	 * <ul>
	 * <li>A within filter where the geometry is the union of all the geometries that was in the gml:id</li>
	 * <li>A within filter for each of the individual geometries</li>
	 * </ul> 
	 * 
	 * The reason is the case of within 2 adjacent regions.  Suppose you have a within switzerland and france, if
	 * a metadata crosses the border then within will fail in the normal case, the fixes that case.
	 * 
	 * The reason that the union and each individual geometry are or-d together is for the situation where a metadata's 
	 * polygon extent is exactly the same as one of the individual geometries.  The within filter is a little dumb in
	 * that it is true if the geometry is fully within or is exactly equals.  So if only the union is used then
	 * the geometry is neither fully within (IE doesn't share boundary) nor exactly the same.  So
	 * I use the work around of having several filters or'd together.  This is not a common case in practice
	 * but must be handled. 
	 */
    private void updateWithinFilter(Element regionEl, List<Geometry> geoms) throws IOException, JDOMException {
        if(geoms.size() < 2) {
            return;
        }
        Element withinFilter = findWithinFilter(regionEl);

        if(withinFilter!=null ){
            Element parentElement = withinFilter.getParentElement();
            int index = parentElement.indexOf(withinFilter);

            ArrayList<Element> ors = new ArrayList<Element>();
            ors.add(withinFilter);
            for (Geometry geometry : geoms) {
                Element newEl = (Element) withinFilter.clone();
                org.jdom.filter.Filter filter = new FindRegionFilterElements();
                Iterator<?> children = regionEl.getDescendants(filter);
                if (children.hasNext()) {
                    setGeom((Element) children.next(), geometry);
                }
                ors.add(newEl);
            }

            Element or = new Element("Or","ogc", "http://www.opengis.net/ogc");
            parentElement.setContent(index, or);

            or.addContent(ors);
        }
    }

    /**
     * Check to see if there is a within filter in the element.
     */
    private Element findWithinFilter(Element element)
    {
        if(element == null ){
            return null;
        }
        if(element.getName().equalsIgnoreCase("WITHIN")){
            return element;
        }
        return findWithinFilter(element.getParentElement());
    }

    /**
     * Update the element with the gml encoded geometry
     */
    private void setGeom(Element element, Geometry fullGeom) throws IOException, JDOMException
    {
        Element parentElement = element.getParentElement();
        int index = parentElement.indexOf(element);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = new Encoder(_configuration);
        encoder.setOmitXMLDeclaration(true);
        encoder.setNamespaceAware(true);

        encoder.encode(SpatialIndexWriter.toMultiPolygon(fullGeom), org.geotools.gml3.GML.MultiPolygon, out);
        Element geomElem = org.fao.geonet.csw.common.util.Xml.loadString(out.toString(Constants.ENCODING), false);
        parentElement.setContent(index, geomElem);
    }

    private ArrayList<Element> lookupGeoms(Element filterExpr)
    {
        org.jdom.filter.Filter filter = new FindRegionFilterElements();
        Iterator<?> children = filterExpr.getDescendants(filter);
        ArrayList<Element> elements = new ArrayList<Element>();
        while (children.hasNext()) {
            elements.add((Element) children.next());
        }
        return elements;
    }

    // ---------------------------------------------------------------------------

	/**
	 * Allow search on current user's groups only adding a BooleanClause to the
	 * search.
	 */
	public static Query getGroupsQuery(ServiceContext context) throws Exception {
		AccessManager am = context.getBean(AccessManager.class);
		Set<Integer> hs = am.getUserGroups(context.getUserSession(), context.getIpAddress(), false);

		BooleanQuery query = new BooleanQuery();


		BooleanClause.Occur occur = LuceneUtils
				.convertRequiredAndProhibitedToOccur(false, false);
		for (Integer groupId : hs) {
			TermQuery tq = new TermQuery(new Term(ReservedOperation.view.getLuceneIndexCode(), groupId.toString()));
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

    /**
     * Creates a lucene Query object from a lucene query string using Lucene query syntax.
     *
     * @param cswServiceSpecificConstraint
     * @return
     * @throws ParseException
     * @throws QueryNodeException 
     */
    public static Query getCswServiceSpecificConstraintQuery(String cswServiceSpecificConstraint, LuceneConfig _luceneConfig) throws ParseException, QueryNodeException {
//        MultiFieldQueryParser parser = new MultiFieldQueryParser(Geonet.LUCENE_VERSION, fields , SearchManager.getAnalyzer());
        StandardQueryParser parser = new StandardQueryParser(SearchManager.getAnalyzer());
        Map<String, NumericConfig> numericMap = new HashMap<String, NumericConfig>();
        for (LuceneConfigNumericField field : _luceneConfig.getNumericFields().values()) {
            String name = field.getName();
            int precisionStep = field.getPrecisionStep();
            NumberFormat format = NumberFormat.getNumberInstance();
            NumericType type = NumericType.valueOf(field.getType().toUpperCase());
            NumericConfig config = new NumericConfig(precisionStep, format, type);
            numericMap.put(name, config);
        }
        parser.setNumericConfigMap(numericMap);
        Query q = parser.parse(cswServiceSpecificConstraint, "title");

        // List of lucene fields which MUST not be control by user, to be removed from the CSW service specific constraint
        List<String> SECURITY_FIELDS = Arrays.asList(
             LuceneIndexField.OWNER,
             LuceneIndexField.GROUP_OWNER);
        
        BooleanQuery bq;
        if (q instanceof BooleanQuery) {
            bq = (BooleanQuery) q;
            List<BooleanClause> clauses = bq.clauses();
    
            Iterator<BooleanClause> it = clauses.iterator();
            while (it.hasNext()) {
                BooleanClause bc = it.next();
    
                for (String fieldName : SECURITY_FIELDS){
                    if (bc.getQuery().toString().contains(fieldName + ":")) {
                        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                            Log.debug(Geonet.CSW_SEARCH,"LuceneSearcher getCswServiceSpecificConstraintQuery removed security field: " + fieldName);
                        it.remove();
    
                        break;
                    }
                }
            }
        }
        return q;
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
	private Map<String, String> hmFields = new HashMap<String, String>();

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

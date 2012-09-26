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

import java.io.ByteArrayOutputStream;


import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.misc.ChainedFilter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Version;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.search.DuplicateDocFilter;
import org.fao.geonet.kernel.search.LuceneConfig;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.search.LuceneIndexField;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.LuceneUtils;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.kernel.search.spatial.SpatialIndexWriter;
import org.geotools.data.DataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.gml2.GMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Encoder;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.Or;
import org.opengis.filter.identity.Identifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

//=============================================================================

public class CatalogSearcher {
	private static final CoordinateReferenceSystem CRS_4326;
    static {
        CoordinateReferenceSystem temp;
        try {
            temp = CRS.decode("EPSG:4326");
        }catch (Exception e) {
            temp = DefaultGeographicCRS.WGS84;
        }
        CRS_4326 = temp;
    }
    private final GMLConfiguration   _configuration;
    private final DataStore 		 _datastore;
    /**
     * Filter geometry object WKT, used in the logger ugly way to store this
     * object, as ChainedFilter API is a little bit cryptic for me...
     * will be set to null (so not logged) if the conif.xml statLogSpatialObjects is false
     */
    private String                         _geomWKT          = null;
    private boolean                        _logSpatialObjects = false;
	private final Element _summaryConfig;
	private final LuceneConfig	_luceneConfig;
	private final Set<String> _tokenizedFieldSet;
	private final Map<String, LuceneConfigNumericField> _numericFieldSet;
	private final FieldSelector _selector;
	private final FieldSelector _uuidselector;
	private Query         _query;
	private Filter _filter;
	private Sort          _sort;
	private String        _lang;
	
	public CatalogSearcher(DataStore datastore, GMLConfiguration  configuration, Element summaryConfig,
			LuceneConfig luceneConfig, FieldSelector selector, FieldSelector uuidselector) {
		_datastore = datastore;
		this._configuration = configuration;
		_luceneConfig = luceneConfig;
		_tokenizedFieldSet = luceneConfig.getTokenizedField();
		_numericFieldSet = luceneConfig.getNumericFields();
		_selector = selector;
		_uuidselector = uuidselector;
		_summaryConfig = summaryConfig;
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

		try {
			if (luceneExpr != null) {
				convertPhrases(luceneExpr);
                if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                    Log.debug(Geonet.CSW_SEARCH, "after convertphrases:\n"+ Xml.getString(luceneExpr));
			}

            return performSearch(context,
                    luceneExpr, filterExpr, filterVersion, sort, resultType,
                    startPosition, maxRecords, maxHitsInSummary, cswServiceSpecificContraint);
		}
        catch (Exception e) {
			Log.error(Geonet.CSW_SEARCH, "Error while searching metadata ");
			Log.error(Geonet.CSW_SEARCH, "  (C) StackTrace:\n" + Util.getStackTrace(e));
			throw new NoApplicableCodeEx("Raised exception while searching metadata : " + e);
		}
	}

	// ---------------------------------------------------------------------------
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
		SearchManager sm = gc.getSearchmanager();

		IndexReader _reader = sm.getIndexReader(context.getLanguage());
		try {
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

        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
            Log.debug(Geonet.CSW_SEARCH, "Records matched : " + numHits);

		// --- retrieve results
		List<String> response = new ArrayList<String>();
		
		for ( ScoreDoc sdoc : tdocs.scoreDocs ) {
			Document doc = _reader.document(sdoc.doc, _uuidselector);
			String uuid = doc.get("_uuid");
			if (uuid != null) response.add(uuid);
		}
		return response;
		} finally {
			sm.releaseIndexReader(_reader);
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
     * @return
     * @throws Exception
     */
	private Pair<Element, List<ResultItem>> performSearch(ServiceContext context, Element luceneExpr,
                                                          Element filterExpr, String filterVersion, Sort sort,
                                                          ResultType resultType, int startPosition, int maxRecords,
                                                          int maxHitsInSummary, String cswServiceSpecificContraint)
            throws Exception {

        if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
            Log.debug(Geonet.CSW_SEARCH, "CatalogSearcher performSearch()");
        if (filterExpr != null) {
            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "CatS performsearch: filterXpr:\n"+ Xml.getString(filterExpr));
        }

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getSearchmanager();
		UserSession session = context.getUserSession();

         IndexReader indexReader = sm.getIndexReader(context.getLanguage());
         try {
		if (luceneExpr != null) {
            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "Search criteria:\n" + Xml.getString(luceneExpr));
        }
        else {
            if(Log.isDebugEnabled(Geonet.CSW_SEARCH))
                Log.debug(Geonet.CSW_SEARCH, "## Search criteria: null");
        }
        // TODO do not just use context getlanguage ?

		Query data = (luceneExpr == null) ? null : LuceneSearcher.makeLocalisedQuery(luceneExpr,
                SearchManager.getAnalyzer(context.getLanguage(), false), _tokenizedFieldSet, _numericFieldSet,
                context.getLanguage(), false);
        Log.info(Geonet.CSW_SEARCH,"LuceneSearcher made query:\n" + data.toString());

        Query cswCustomFilterQuery = null;
        Log.info(Geonet.CSW_SEARCH,"LuceneSearcher cswCustomFilter:\n" + cswServiceSpecificContraint);
        if (StringUtils.isNotEmpty(cswServiceSpecificContraint)) {
            cswCustomFilterQuery = getCswServiceSpecificConstraintQuery(cswServiceSpecificContraint);
            Log.info(Geonet.CSW_SEARCH,"LuceneSearcher cswCustomFilterQuery:\n" + cswCustomFilterQuery);
        }

		Query groups = getGroupsQuery(context);
		if (sort == null) {
			List<Pair<String, Boolean>> fields = Collections.singletonList(Pair.read(Geonet.SearchResult.SortBy.RELEVANCE, true));
            sort = LuceneSearcher.makeSort(fields, context.getLanguage(), true);
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

        Element replaceElements = replaceElements(filterExpr);
		// TODO Handle NPE creating spatial filter (due to constraint
        Filter spatialfilter = sm.getSpatial().filter(query, replaceElements, filterVersion);
        Filter duplicateRemovingFilter = new DuplicateDocFilter(query, 1000000);
        Filter cFilter = null;
        if (spatialfilter == null) {
            cFilter = duplicateRemovingFilter;
        }
        else {
            Filter[] filters = new Filter[]{duplicateRemovingFilter, spatialfilter};
            cFilter = new ChainedFilter(filters, ChainedFilter.AND);
        }

        boolean buildSummary = resultType == ResultType.RESULTS_WITH_SUMMARY;
        int numHits = startPosition + maxRecords;
        // get as many results as instructed or enough for search summary
        if (buildSummary) {
            numHits = Math.max(maxHitsInSummary, numHits);
        }
        if (sort == null) {
			sort = LuceneSearcher.makeSort(Collections.singletonList(Pair.read(Geonet.SearchResult.SortBy.RELEVANCE, true)), context.getLanguage(), true);
		}
		// record globals for reuse
		_query = query;
		_filter = new CachingWrapperFilter(cFilter);
		_sort = sort;
		_lang = context.getLanguage();
	
		Pair<TopDocs,Element> searchResults = LuceneSearcher.doSearchAndMakeSummary(numHits, startPosition - 1,
                maxRecords, Integer.MAX_VALUE, _lang, resultType.toString(), _summaryConfig, indexReader, query, _filter,
                sort, buildSummary, _luceneConfig.isTrackDocScores(), _luceneConfig.isTrackMaxScore(),
                _luceneConfig.isDocsScoredInOrder()
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
		if (buildSummary) {
			i = startPosition -1;
			iMax = Math.min(hits.scoreDocs.length, i + maxRecords); 
		}
		for (;i < iMax; i++) {
			Document doc = indexReader.document(hits.scoreDocs[i].doc, _selector);
			String id = doc.get("_id");
			ResultItem ri = new ResultItem(id);
			results.add(ri);
			for (String field : FieldMapper.getMappedFields()) {
				String value = doc.get(field);
				if (value != null) {
					ri.add(field, value);
                }
			}
		}
		summary.setName("Summary");
		summary.setNamespace(Csw.NAMESPACE_GEONET);
		return Pair.read(summary, results);
         } finally {
        	 sm.releaseIndexReader(indexReader);
         }
	}

	// ---------------------------------------------------------------------------

    private Element replaceElements(Element filterExpr) throws IOException, Exception
    {

        final Namespace namespace = Namespace.getNamespace("gml", "http://www.opengis.net/gml");
        ArrayList<Element> elements = lookupGeoms(filterExpr, namespace);

        FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());

        for (Element element : elements) {
            if (_datastore == null ) {
                throw new RuntimeException("datastore is not configured correctly.  Problem discovered when trying to replace:"
                        + Xml.getString(element));
            }

            Attribute attribute = element.getAttribute("id", namespace);
            Geometry fullGeom = null;
            List<Geometry> geoms = new ArrayList<Geometry>();
            String[] gmlIds = attribute.getValue().split(",");

            for (String gmlId : gmlIds) {

                FeatureIterator<SimpleFeature> iter = null;
                if (gmlId.startsWith("kantone:")) {
                    iter = getIdentifiedFeature(gmlId, filterFactory, "KANTONSNR", "kantone_search");
                } else if (gmlId.startsWith("gemeinden:")) {
                    iter = getIdentifiedFeature(gmlId, filterFactory, "OBJECTVAL", "gemeinden_search");
                } else if (gmlId.startsWith("country:")) {
                    iter = getIdentifiedFeature(gmlId, filterFactory, "LAND", "countries_search");
                }

                if (iter != null) {
                    try {
                        final SimpleFeature simpleFeature = iter.next();
                        Geometry geom = (Geometry) simpleFeature.getDefaultGeometry();
                        while (iter.hasNext()) {
                            geom = geom.union((Geometry) iter.next().getDefaultGeometry());
                        }

                        geom.setUserData(CRS_4326);
                        geoms.add(geom);

                        if (fullGeom == null) {
                            fullGeom = geom;
                        } else {
                            fullGeom = fullGeom.union(geom);
                        }

                    } finally {
                        iter.close();
                    }
                } else {
                    Log.warning(Geonet.CSW_SEARCH, "Cannot find : " + gmlId);
                }

            }
            if (this._logSpatialObjects) {
                this._geomWKT = fullGeom.toText();
            }

            Element withinFilter = findWithinFilter(element);

            if(withinFilter!=null && geoms.size() > 1){
                Element parentElement = withinFilter.getParentElement();
                int index = parentElement.indexOf(withinFilter);

                ArrayList<Element> ors = new ArrayList<Element>();
                ors.add(withinFilter);
                for (Geometry geometry : geoms) {
                    Element newEl = (Element) withinFilter.clone();
                    ArrayList<Element> geomEl = lookupGeoms(newEl, namespace);
                    setGeom(geomEl.get(0), geometry);
                    ors.add(newEl);
                }

                Element or = new Element("Or","ogc", "http://www.opengis.net/ogc");
                parentElement.setContent(index, or);

                or.addContent(ors);
            }

            setGeom(element, fullGeom);
        }
        return filterExpr;
    }

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

    private void setGeom(Element element, Geometry fullGeom) throws IOException, JDOMException
    {
        Element parentElement = element.getParentElement();
        int index = parentElement.indexOf(element);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Encoder encoder = new Encoder(_configuration);
        encoder.setOmitXMLDeclaration(true);
        encoder.setNamespaceAware(true);

        encoder.encode(SpatialIndexWriter.toMultiPolygon(fullGeom), org.geotools.gml3.GML.MultiPolygon, out);
        Element geomElem = org.fao.geonet.csw.common.util.Xml.loadString(out.toString(), false);
        parentElement.setContent(index, geomElem);
    }

    private ArrayList<Element> lookupGeoms(Element filterExpr, final Namespace namespace)
    {
        org.jdom.filter.Filter filter = new org.jdom.filter.Filter()
        {

            private static final long serialVersionUID = 1L;

            public boolean matches(Object obj)
            {
                if (obj instanceof Element) {
                    Element element = (Element) obj;
                    Attribute attribute = element.getAttribute("id", namespace);
                    if (attribute != null) {
                        String id = attribute.getValue();
                        if (id.startsWith("kantone:") || id.startsWith("gemeinden:") || id.startsWith("country:")) {
                            return true;
                        }
                    }
                }
                return false;
            }

        };
        Iterator children = filterExpr.getDescendants(filter);
        ArrayList<Element> elements = new ArrayList<Element>();
        while (children.hasNext()) {
            elements.add((Element) children.next());
        }
        return elements;
    }

    private FeatureIterator<SimpleFeature> getIdentifiedFeature(String gmlId, FilterFactory2 filterFactory,
            String idField, String typeName) throws IOException
    {
        FeatureIterator<SimpleFeature> iter;
        String[] ids = gmlId.split(":", 2)[1].split(",");
        Set<Identifier> identifiers = new HashSet<Identifier>();
        for (String id : ids) {
        	identifiers.add(filterFactory.featureId(typeName+"."+id));
        }
        org.geotools.data.Query query = new org.geotools.data.Query(typeName, filterFactory.id(identifiers), new String[]{"the_geom"});
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = _datastore.getFeatureSource(typeName).getFeatures(
        		query);
        iter = features.features();
        return iter;
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

    /**
     * Creates a lucene Query object from a lucene query string using Lucene query syntax.
     *
     * @param cswServiceSpecificConstraint
     * @return
     * @throws ParseException
     */
    public static Query getCswServiceSpecificConstraintQuery(String cswServiceSpecificConstraint) throws ParseException {

        Query q = new QueryParser(Version.LUCENE_30, "title", SearchManager.getAnalyzer()).parse(cswServiceSpecificConstraint);

        // List of lucene fields which MUST not be control by user, to be removed from the CSW service specific constraint
        List<String> SECURITY_FIELDS = Arrays.asList(
             LuceneIndexField.OWNER,
             LuceneIndexField.GROUP_OWNER);

        BooleanQuery bq = (BooleanQuery) q;

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
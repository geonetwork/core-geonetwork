//==============================================================================
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

package org.fao.geonet.kernel.search;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import jeeves.constants.Jeeves;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.FSDirectory;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.kernel.search.LuceneConfig.LuceneConfigNumericField;
import org.fao.geonet.kernel.search.SummaryComparator.SortOption;
import org.fao.geonet.kernel.search.SummaryComparator.Type;
import org.fao.geonet.kernel.search.log.SearcherLogger;
import org.fao.geonet.kernel.search.lucenequeries.DateRangeQuery;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.util.JODAISODate;
import org.jdom.Element;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

//==============================================================================
// search metadata locally using lucene
//--------------------------------------------------------------------------------

public class LuceneSearcher extends MetaSearcher
{
	private SearchManager _sm;
	private String        _styleSheetName;
	private Element       _summaryConfig;

	private Query         _query;
	private Filter        _filter;
	private Sort          _sort;
	private Element       _elSummary;
	private FieldSelector _selector;
	private IndexReader		_reader;

	private int           _maxSummaryKeys;
	private int           _maxHitsInSummary;
	private int           _numHits;
	private String        _resultType;
  private String        _language;

	private HashSet<String>	_tokenizedFieldSet;
	private LuceneConfig _luceneConfig;
	private String _boostQueryClass;
	
	/** Filter geometry object WKT, used in the logger
	  * ugly way to store this object, as ChainedFilter API is a little bit cryptic to me...
	  */
	private String _geomWKT = null;

	//--------------------------------------------------------------------------------
	// constructor
	public LuceneSearcher (SearchManager sm, String styleSheetName, Element summaryConfig, LuceneConfig luceneConfig)
	{
		_sm             = sm;
		_styleSheetName = styleSheetName;
		_summaryConfig  = summaryConfig;
		_selector = new FieldSelector() {
			public final FieldSelectorResult accept(String name) {
				if (name.equals("_id")) return FieldSelectorResult.LOAD;
				else return FieldSelectorResult.NO_LOAD;
			}
		};

		// build _tokenizedFieldSet
		_luceneConfig = luceneConfig;
		_boostQueryClass = _luceneConfig.getBoostQueryClass();
		_tokenizedFieldSet = luceneConfig.getTokenizedField();
	}

	//--------------------------------------------------------------------------------
	// MetaSearcher API

	public void search(ServiceContext srvContext, Element request, ServiceConfig config)
		throws Exception
	{
		SearcherLogger searchLogger = new SearcherLogger(srvContext, _sm.getLogSpatialObject(), _sm.getLuceneTermsToExclude());

		_reader = _sm.getIndexReader();
		computeQuery(srvContext, request, config);
		initSearchRange(srvContext);
		performQuery(getFrom()-1, getTo());
		searchLogger.logSearch(_query, _numHits, _sort, _geomWKT, config.getValue(Jeeves.Text.GUI_SERVICE,"n"));
	}

	//--------------------------------------------------------------------------------

	public List<org.jdom.Document> presentDocuments(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception {
		throw new UnsupportedOperationException("Not supported by Lucene searcher");
	}

	//--------------------------------------------------------------------------------


	/**
	 * @return		An empty response if no result or a list of results. Return
	 * only geonet:info element in fast mode. 
	 */
	public Element present(ServiceContext srvContext, Element request, ServiceConfig config)
		throws Exception
	{
		updateSearchRange(request);

		GeonetContext gc = null;
		if (srvContext != null)
			gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);

		String sFast = request.getChildText("fast");
		boolean fast = sFast != null && sFast.equals("true");

		// build response
		Element response =  new Element("response");
		response.setAttribute("from",  getFrom()+"");
		response.setAttribute("to",    getTo()+"");
		Log.debug(Geonet.SEARCH_ENGINE, Xml.getString(response));

		response.addContent((Element)_elSummary.clone());

		if (getTo() > 0) {
			TopDocs tdocs = performQuery(getFrom()-1, getTo()); // get enough hits to show a page	

			int nrHits = getTo() - (getFrom()-1);
			if (tdocs.scoreDocs.length >= nrHits) {
				for (int i = 0; i < nrHits; i++) {
					Document doc;
					if (fast) {
						doc = _reader.document(tdocs.scoreDocs[i].doc); // no selector
					} else {
						doc = _reader.document(tdocs.scoreDocs[i].doc, _selector);
					}
					String id = doc.get("_id");
					Element md = null;
	
					if (fast) {
						md = getMetadataFromIndex(doc, id);
					}
                    else if (srvContext != null) {
						md = gc.getDataManager().getMetadata(srvContext, id, false);
					}
	
					//--- a metadata could have been deleted just before showing 
					//--- search results
	
					if (md != null) {
						// Calculate score and add it to info elem
						if (_luceneConfig.isTrackDocScores()) {
							Float score = tdocs.scoreDocs[i].score;
							Element info = md.getChild (Edit.RootChild.INFO, Edit.NAMESPACE);
							addElement(info, Edit.Info.Elem.SCORE, score.toString());
						}
						response.addContent(md);
					}
				}
			} else {
				throw new Exception("Failed: Not enough search results ("+tdocs.scoreDocs.length+") available to meet request for "+nrHits+".");
			}
		}
		
		return response;
	}
	
	//--------------------------------------------------------------------------------

	public int getSize()
	{
		return _numHits;
	}

	//--------------------------------------------------------------------------------

	public Element getSummary() throws Exception
	{
		Element response =  new Element("response");
		response.addContent((Element)_elSummary.clone());
		return response;
	}

	//--------------------------------------------------------------------------------

	public void close() {
		try {
			_sm.releaseIndexReader(_reader);
		} catch (IOException e) {
			Log.error(Geonet.SEARCH_ENGINE,"Failed to close Index Reader: "+e.getMessage());
			e.printStackTrace();
		}
	}

	//--------------------------------------------------------------------------------
	// private setup, index, delete and search functions

	private void computeQuery(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception
	{

		String sMaxRecordsInKeywordSummary = request.getChildText("maxHitsInSummary");
		if (sMaxRecordsInKeywordSummary == null) sMaxRecordsInKeywordSummary = config.getValue("maxHitsInSummary", "1000");
		_maxHitsInSummary = Integer.parseInt(sMaxRecordsInKeywordSummary);

		String sMaxSummaryKeys = request.getChildText("maxSummaryKeys");
		if (sMaxSummaryKeys == null) sMaxSummaryKeys = config.getValue("maxSummaryKeys", "10");
		_maxSummaryKeys = Integer.parseInt(sMaxSummaryKeys);

		if (srvContext != null) {
			GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
	
			Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);


            List<Element> requestedGroups = request.getChildren("group");
            Set<String> userGroups = gc.getAccessManager().getUserGroups(dbms, srvContext.getUserSession(), srvContext.getIpAddress());
            UserSession userSession = srvContext.getUserSession();
            if (userSession == null
                    || userSession.getProfile() == null
                    || ! (userSession.getProfile().equals(Geonet.Profile.ADMINISTRATOR) && userSession.isAuthenticated())) {
                if(requestedGroups != null && requestedGroups.size() > 0) {
                    for(Element group : requestedGroups) {
                        if(! userGroups.contains(group.getText())) {
                            throw new UnAuthorizedException("You are not authorized to do this.", null);
                        }
                    }
                }
            }

			// if 'restrict to' is set then don't add any other user/group info
			if (request.getChild("group") == null) {
				for (String group : userGroups) {
					request.addContent(new Element("group").addContent(group));
                }
                String owner = null;
                if (userSession != null) {
                    owner = userSession.getUserId();
                }
                if (owner != null) {
					request.addContent(new Element("owner").addContent(owner));
                }
			    //--- in case of an admin we have to show all results
                if (userSession != null) {
                    if (userSession.isAuthenticated()) {
                        if (userSession.getProfile().equals(Geonet.Profile.ADMINISTRATOR)) {
                            request.addContent(new Element("isAdmin").addContent("true"));
}
                        else if (userSession.getProfile().equals(Geonet.Profile.REVIEWER)) {
                            request.addContent(new Element("isReviewer").addContent("true"));
}
                    }
                }
            }

			//--- handle the time elements

			processTimeRange(request.getChild("dateFrom"), "0000-01-01",request.getChild("dateTo"), "9999-01-01");

			//--- some other stuff

			Log.debug(Geonet.SEARCH_ENGINE, "CRITERIA:\n"+ Xml.getString(request));

			if (_styleSheetName.equals(Geonet.File.SEARCH_Z3950_SERVER)) {
				// Construct Lucene query by XSLT, not Java, for Z3950 anyway :-)
				Element xmlQuery = _sm.transform(_styleSheetName, request);
				Log.debug(Geonet.SEARCH_ENGINE, "XML QUERY:\n"+ Xml.getString(xmlQuery));
				_query = makeQuery(xmlQuery, SearchManager.getAnalyzer(), _tokenizedFieldSet, _luceneConfig.getNumericFields());
			} else {
		        // Construct Lucene query by Java, not XSLT
				_query = new LuceneQueryBuilder(_tokenizedFieldSet, _luceneConfig.getNumericFields(), _sm.getAnalyzer()).build(request);
			}
		    
			// Boosting query
			if (_boostQueryClass != null) {
				try {
					Log.debug(Geonet.SEARCH_ENGINE, "Create boosting query:" + _boostQueryClass);
					Class boostClass = Class.forName(_boostQueryClass);
					Class[] clTypesArray = _luceneConfig.getBoostQueryParameterClass();				
					Object[] inParamsArray = _luceneConfig.getBoostQueryParameter(); 

					Class[] clTypesArrayAll = new Class[clTypesArray.length + 1];
					clTypesArrayAll[0] = Class.forName("org.apache.lucene.search.Query");

					for (int j=0; j<clTypesArray.length; j++) {
						clTypesArrayAll[j+1] = clTypesArray[j];
					}
					Object[] inParamsArrayAll = new Object[inParamsArray.length + 1];
					inParamsArrayAll[0] = _query;
					for (int j=0; j<inParamsArray.length; j++) {
						inParamsArrayAll[j+1] = inParamsArray[j];
					}
					try {
						Log.debug(Geonet.SEARCH_ENGINE, "Creating boost query with parameters:" + inParamsArrayAll.toString());
						Constructor c = boostClass.getConstructor(clTypesArrayAll);
						_query = (Query) c.newInstance(inParamsArrayAll);
					} catch (Exception e) {
						Log.warning(Geonet.SEARCH_ENGINE, " Failed to create boosting query: " + e.getMessage() 
								+ ". Check Lucene configuration");
						e.printStackTrace();
					}	
				} catch (Exception e1) {
					Log.warning(Geonet.SEARCH_ENGINE, " Error on boosting query initialization: " + e1.getMessage()
							+ ". Check Lucene configuration");
				}
			}
			
		    // Use RegionsData rather than fetching from the DB everytime
		    //
		    //request.addContent(Lib.db.select(dbms, "Regions", "region"));
		    Element regions = RegionsData.getRegions(dbms);
		    request.addContent(regions);
		}

		if (srvContext != null)
        	_language = srvContext.getLanguage();
        else
        	_language = "eng"; // TODO : set default not language in config

		
		Geometry geometry = getGeometry(request);
        if (geometry != null) {
						if (_sm.getLogSpatialObject()) {
							_geomWKT = geometry.toText();
						}
            _filter = new CachingWrapperFilter(_sm.getSpatial().filter(_query, geometry, request));
        }
        
        String sortBy = Util.getParam(request, Geonet.SearchResult.SORT_BY,
				Geonet.SearchResult.SortBy.RELEVANCE);
		boolean sortOrder = (Util.getParam(request, Geonet.SearchResult.SORT_ORDER, "").equals(""));
		Log.debug(Geonet.SEARCH_ENGINE, "Sorting by : " + sortBy);

		_sort = makeSort(Collections.singletonList(Pair.read(sortBy, sortOrder)));
		
		_resultType = config.getValue(Geonet.SearchResult.RESULT_TYPE, Geonet.SearchResult.ResultType.HITS);
		/* resultType is not specified in search params - it's in config?
		Content child = request.getChild(Geonet.SearchResult.RESULT_TYPE);
        if (child == null) {
            _resultType = Geonet.SearchResult.ResultType.HITS;
        }
        else {
            _resultType = child.getValue();
        }
			*/
	}

	//---------------------------------------------------------------------------
	
	private void processTimeRange(Element fromTime, String defaultFromTime, Element toTime, String defaultToTime) {
		if (fromTime != null && toTime != null) { 
			if (fromTime.getTextTrim().equals("") && 
								 toTime.getTextTrim().equals("")) {
				fromTime.detach(); toTime.detach();
			} else {
				if (fromTime.getTextTrim().equals("")) {
					fromTime.setText(defaultFromTime);
				} else if (toTime.getTextTrim().equals("")) {
					toTime.setText(defaultToTime);
				}
				String newFromTime = JODAISODate.parseISODateTime(fromTime.getText());
				fromTime.setText(newFromTime);	
				String newToTime = JODAISODate.parseISODateTime(toTime.getText());
				toTime.setText(newToTime);	
			}
		}
	}

	//--------------------------------------------------------------------------------
	/**
	 * Execute Lucene query with sorting option. 
	 * 
	 * Default sort by option is RELEVANCE.
	 * Default sort order option is not reverse order. Reverse order is active 
	 * if sort order option is set and not null
     * @param startHit
     * @param endHit
     * @return
     * @throws Exception
     */
	private TopDocs performQuery(int startHit, int endHit) throws Exception
	{
		CachingWrapperFilter cFilter = null;
		if (_filter != null) cFilter = new CachingWrapperFilter(_filter);

		int numHits;
		boolean buildSummary = _elSummary == null;
		if (buildSummary) {
			// get as many results as instructed or enough for search summary
			numHits = Math.max(_maxHitsInSummary,endHit);
		} else {
			numHits = endHit;
		}

		Pair<TopDocs,Element> results = doSearchAndMakeSummary( numHits, startHit, endHit, 
				_maxSummaryKeys, _language, _resultType, _summaryConfig, 
				_reader, _query, cFilter, _sort, buildSummary,
				_luceneConfig.isTrackDocScores(), _luceneConfig.isTrackMaxScore(), _luceneConfig.isDocsScoredInOrder()
		);
		TopDocs hits = results.one();
		_elSummary = results.two();
		_numHits = Integer.parseInt(_elSummary.getAttributeValue("count"));
	
		Log.debug(Geonet.SEARCH_ENGINE, "Hits found : "+_numHits+"");
		
		return hits;
	}

	//--------------------------------------------------------------------------------

	private Geometry getGeometry(Element request) throws Exception
    {
        String geomWKT = Util.getParam(request, Geonet.SearchResult.GEOMETRY, null);
        if (geomWKT != null) {
            WKTReader reader = new WKTReader();
            return reader.read(geomWKT);
        }
        return null;
    }
	
	//--------------------------------------------------------------------------------
	// Create the Sort to use in the search
    public static Sort makeSort(List<Pair<String, Boolean>> fields)
    {

        List<SortField> sortFields = new ArrayList<SortField>();

        for (Pair<String, Boolean> sortBy : fields) {
            Log.debug(Geonet.SEARCH_ENGINE, "Sorting by : " + sortBy);
            SortField sortField = makeSortField(sortBy.one(), sortBy.two());
            if( sortField!=null ) sortFields.add(sortField);
        }
        sortFields.add(SortField.FIELD_SCORE);

        return new Sort(sortFields.toArray(new SortField[sortFields.size()]));
    }
    
	//--------------------------------------------------------------------------------
    /**
     * Define sort field. By default, the field is assumed to be a string.
     * Only popularity and rating are sorted based on integer type.
     * In order to works well sort field needs to be not tokenized in Lucene index.
     * 
     * Relevance is the default Lucene sorting mechanism.
     * 
     * @param sortBy
     * @param sortOrder
     * @return
     */
    private static SortField makeSortField(String sortBy, boolean sortOrder)
    {
        int sortType = SortField.STRING;

        if( sortBy.equals(Geonet.SearchResult.SortBy.RELEVANCE) ){
            return null;
        }
        
        // FIXME : here we should be able to define field type ?
        // Add "_" prefix for internal fields. Maybe we should
        // update that in DataManager indexMetadata to have the list of
        // internal Lucene fields (ie. not defined in index-fields.xsl).
        if (sortBy.equals(Geonet.SearchResult.SortBy.POPULARITY)
        		|| sortBy.equals(Geonet.SearchResult.SortBy.RATING)) {
            sortType = SortField.INT;
            sortBy = "_" + sortBy;
        } else if (sortBy.equals(Geonet.SearchResult.SortBy.DATE) 
        		|| sortBy.equals(Geonet.SearchResult.SortBy.TITLE)) {
            sortBy = "_" + sortBy;
        }

        return new SortField(sortBy, sortType, sortOrder);
    }
    
	//--------------------------------------------------------------------------------
	/**
	 *  Makes a new lucene query.
	 *  
	 *  If the field to be queried is tokenized then this method applies 
	 *  the appropriate analyzer (see SearchManager) to the field.
	 * @param numericFieldSet TODO
	 *   
	 */
	@SuppressWarnings({"deprecation"})
    public static Query makeQuery(Element xmlQuery, PerFieldAnalyzerWrapper analyzer, HashSet<String> tokenizedFieldSet, HashMap<String, LuceneConfigNumericField> numericFieldSet) throws Exception
	{
        System.out.println("\n\n\n* * makeQuery input XML:\n" + Xml.getString(xmlQuery));
		String name = xmlQuery.getName();
		Query returnValue;
		
		if (name.equals("TermQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
			String txt = analyzeQueryText(fld, xmlQuery.getAttributeValue("txt"), analyzer, tokenizedFieldSet);
			returnValue = new TermQuery(new Term(fld, txt));
		}
		else if (name.equals("FuzzyQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
			Float sim = Float.valueOf(xmlQuery.getAttributeValue("sim"));
			String txt = analyzeQueryText(fld, xmlQuery.getAttributeValue("txt"), analyzer, tokenizedFieldSet);
			returnValue = new FuzzyQuery(new Term(fld, txt), sim);
		}
		else if (name.equals("PrefixQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
			String txt = analyzeQueryText(fld, xmlQuery.getAttributeValue("txt"), analyzer, tokenizedFieldSet);
			returnValue = new PrefixQuery(new Term(fld, txt));
		}
		else if (name.equals("MatchAllDocsQuery"))
		{
            return new MatchAllDocsQuery();
		}
		else if (name.equals("WildcardQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
			String txt = analyzeQueryText(fld, xmlQuery.getAttributeValue("txt"), analyzer, tokenizedFieldSet);
			returnValue = new WildcardQuery(new Term(fld, txt));
		}
		else if (name.equals("PhraseQuery"))
		{
			PhraseQuery query = new PhraseQuery();
            for (Object o : xmlQuery.getChildren()) {
                Element xmlTerm = (Element) o;
                String fld = xmlTerm.getAttributeValue("fld");
                String txt = analyzeQueryText(fld, xmlTerm.getAttributeValue("txt"), analyzer, tokenizedFieldSet);
                query.add(new Term(fld, txt));
            }
			returnValue = query;
		}
		else if (name.equals("RangeQuery"))
		{
			String  fld        = xmlQuery.getAttributeValue("fld");
			String  lowerTxt   = xmlQuery.getAttributeValue("lowerTxt");
			String  upperTxt   = xmlQuery.getAttributeValue("upperTxt");
			String  sInclusive = xmlQuery.getAttributeValue("inclusive");
			boolean inclusive  = "true".equals(sInclusive);

			LuceneConfigNumericField fieldConfig = numericFieldSet.get(fld);
			if (fieldConfig != null) {
				returnValue = LuceneQueryBuilder.buildNumericRangeQueryForType(null, lowerTxt, upperTxt, inclusive, inclusive, fieldConfig.getType());
			} else {
				lowerTxt = (lowerTxt == null ? null : analyzeQueryText(fld, lowerTxt, analyzer, tokenizedFieldSet));
				upperTxt = (upperTxt == null ? null : analyzeQueryText(fld, upperTxt, analyzer, tokenizedFieldSet));

				returnValue = new TermRangeQuery(fld, lowerTxt, upperTxt, inclusive, inclusive);
			}
		}
		else if (name.equals("DateRangeQuery"))
		{
			String  fld        = xmlQuery.getAttributeValue("fld");
			String  lowerTxt   = xmlQuery.getAttributeValue("lowerTxt");
			String  upperTxt   = xmlQuery.getAttributeValue("upperTxt");
			String  sInclusive = xmlQuery.getAttributeValue("inclusive");
			returnValue = new DateRangeQuery(fld, lowerTxt, upperTxt, sInclusive);
		}
		else if (name.equals("BooleanQuery"))
		{
			BooleanQuery query = new BooleanQuery();
            for (Object o : xmlQuery.getChildren()) {
                Element xmlBooleanClause = (Element) o;
                String sRequired = xmlBooleanClause.getAttributeValue("required");
                String sProhibited = xmlBooleanClause.getAttributeValue("prohibited");
                boolean required = sRequired != null && sRequired.equals("true");
                boolean prohibited = sProhibited != null && sProhibited.equals("true");
                BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(required, prohibited);
                List<Element> subQueries = xmlBooleanClause.getChildren();
                Element xmlSubQuery;
                if (subQueries != null && subQueries.size() != 0) {
                    xmlSubQuery = subQueries.get(0);
                    query.add(makeQuery(xmlSubQuery, analyzer, tokenizedFieldSet, numericFieldSet), occur);
                }
            }
			BooleanQuery.setMaxClauseCount(16384); // FIXME: quick fix; using Filters should be better
			
			returnValue = query;
		}
		else throw new Exception("unknown lucene query type: " + name);

        Log.debug(Geonet.SEARCH_ENGINE, "Lucene Query: " + returnValue.toString());
        System.out.println("\n* * makeQuery Lucene Query: " + returnValue.toString() + "\n\n\n");
		return returnValue;
	}

	//--------------------------------------------------------------------------------

	private static HashMap<String,HashMap<String,Object>> getSummaryConfig(Element summaryConfig, String resultType, int maxSummaryKeys) throws Exception {

		HashMap<String,HashMap<String,Object>> results = new HashMap<String,HashMap<String,Object>>();

		Element resultTypeConfig = summaryConfig.getChild("def").getChild(resultType);
		List<Element> elements = resultTypeConfig.getChildren();

		for (Element summaryElement : elements) {
			String name = summaryElement.getAttributeValue("name");
			String plural = summaryElement.getAttributeValue("plural");
			String key = summaryElement.getAttributeValue("indexKey");
			String order = summaryElement.getAttributeValue("order");
			String maxString = summaryElement.getAttributeValue("max");
			String type = summaryElement.getAttributeValue("type");
			if (order == null) {
				order = "frequency";
			}
			int max;
			if (maxString == null) {
				max = 10;
			} else {
				max = Integer.parseInt(maxString);
			}
			max = Math.min(maxSummaryKeys, max);
			if( type==null ){
				type = "string";
			}

			HashMap<String,Object> values = new HashMap<String,Object>();
			values.put("name", name);
			values.put("plural", plural);
			values.put("max", max);
			values.put("order", order);
			values.put("type", type);
			values.put("typeConfig", summaryConfig.getChild("typeConfig"));
			results.put(key,values);
		}

		return results;
	}

	//--------------------------------------------------------------------------------

	private static SummaryComparator getSummaryComparator(String langCode, HashMap<String, Object> summaryConfigValuesForKey) throws Exception
	{
		
			SortOption sortOption = SortOption.parse((String)summaryConfigValuesForKey.get("order"));
			return new SummaryComparator(sortOption, Type.parse((String)summaryConfigValuesForKey.get("type")), langCode, (Element)summaryConfigValuesForKey.get("typeConfig"));
	}

	//--------------------------------------------------------------------------------

	private static HashMap<String,HashMap<String,Integer>> prepareSummaryMaps(Set<String> indexKeys) throws Exception
	{
		HashMap<String,HashMap<String,Integer>> summaryMaps = new HashMap<String,HashMap<String,Integer>>();
		for (String key : indexKeys) {
			summaryMaps.put(key, new HashMap<String, Integer>());
		}
		return summaryMaps;
	}

	//--------------------------------------------------------------------------------

	private static HashMap<String,HashMap<String,Integer>> buildSummaryMaps(Element elSummary, IndexReader reader, ScoreDoc[] sdocs, final HashMap<String,HashMap<String,Integer>> summaryMaps) {
		elSummary.setAttribute("hitsusedforsummary", sdocs.length+"");

		FieldSelector keySelector = new FieldSelector() {
			public final FieldSelectorResult accept(String name) {
				if (summaryMaps.get(name) != null) return FieldSelectorResult.LOAD;
				else return FieldSelectorResult.NO_LOAD;
			}
		};

        for (ScoreDoc sdoc : sdocs) {
            Document doc = null;
            try {
                doc = reader.document(sdoc.doc, keySelector);
            }
            catch (Exception e) {
                Log.error(Geonet.SEARCH_ENGINE, e.getMessage() + " Caused Failure to get document " + sdoc.doc);
                e.printStackTrace();
            }

            for (String key : summaryMaps.keySet()) {
                HashMap<String, Integer> summary = summaryMaps.get(key);
                String hits[] = doc.getValues(key);
                if (hits != null) {
                    for (String info : hits) {
                        Integer catCount = summary.get(info);
                        if (catCount == null) {
                            catCount = 1;
                        }
                        else {
                            catCount = catCount + 1;
                        }
                        summary.put(info, catCount);
                    }
                }
            }
        }

		return summaryMaps;
	}

	//--------------------------------------------------------------------------------

	private static Element addSortedSummaryKeys(Element elSummary, String langCode, HashMap<String,HashMap<String,Integer>> summaryMaps, HashMap<String,HashMap<String,Object>> summaryConfigValues) throws Exception {
	
		for ( String indexKey : summaryMaps.keySet() ) {
			HashMap <String,Object> summaryConfigValuesForKey = summaryConfigValues.get(indexKey);
       Element rootElem = new Element((String)summaryConfigValuesForKey.get("plural"));
      // sort according to frequency
			SummaryComparator summaryComparator = getSummaryComparator(langCode, summaryConfigValuesForKey);
			HashMap<String,Integer> summary = summaryMaps.get(indexKey);
		  Log.debug(Geonet.SEARCH_ENGINE, "Sorting "+summary.size()+" according to frequency of " + indexKey);

			TreeSet<Map.Entry<String, Integer>> sortedSummary = new TreeSet<Map.Entry<String, Integer>>(summaryComparator);
			sortedSummary.addAll(summary.entrySet());

			Integer max = (Integer)summaryConfigValuesForKey.get("max");

			int nKeys = 0;
            for (Object aSortedSummary : sortedSummary) {
                if (++nKeys > max) {
                    break;
                }

                Map.Entry me = (Map.Entry) aSortedSummary;
                String keyword = (String) me.getKey();
                Integer keyCount = (Integer) me.getValue();

                Element childElem = new Element((String) summaryConfigValuesForKey.get("name"));
                childElem.setAttribute("count", keyCount.toString());
                childElem.setAttribute("name", keyword);
                rootElem.addContent(childElem);
            }
			elSummary.addContent(rootElem);
		}

		return elSummary;
	}

	//--------------------------------------------------------------------------------

	
	/**
	 * Do Lucene search and optionnaly build a summary for the search.
	 * 
	 * @param numHits	the maximum number of hits to collect
	 * @param startHit	the start hit to return in the TopDocs if not building summary
	 * @param endHit	the end hit to return in the TopDocs if not building summary
	 * @param maxSummaryKeys	the max number of keys to process in a summary
	 * @param langCode	the language code used by SummaryComparator
	 * @param resultType	the resultType is used to define the type of summary to build according to summary configuration in config-summary.xml.
	 * @param summaryConfig	the summary configuration
	 * @param reader
	 * @param query
	 * @param cFilter
	 * @param sort	the sort criteria
	 * @param buildSummary	true to build query summary element. Summary is stored in the second element of the returned Pair.
	 * @param trackDocScores	specifies whether document scores should be tracked and set on the results. 
	 * @param trackMaxScore	specifies whether the query's maxScore should be tracked and set on the resulting TopDocs.
	 * @param docsScoredInOrder	specifies whether documents are scored in doc Id order or not by the given Scorer
	 *
	 * @return	the topDocs for the search. When building summary, topDocs will contains all search hits
	 * and need to be filtered to return only required hits according to search parameters.
	 * 
	 * @throws Exception
	 */
	public static Pair<TopDocs, Element> doSearchAndMakeSummary(int numHits, int startHit, int endHit, int maxSummaryKeys, 
			String langCode, String resultType, Element summaryConfig, 
			IndexReader reader, Query query, CachingWrapperFilter cFilter, Sort sort, boolean buildSummary,
			boolean trackDocScores, boolean trackMaxScore, boolean docsScoredInOrder) throws Exception
	{

		Log.debug(Geonet.SEARCH_ENGINE, "Setting up the TFC with numHits "+numHits);
		TopFieldCollector tfc = TopFieldCollector.create(sort, numHits, true, trackDocScores, trackMaxScore, docsScoredInOrder);

        if(query != null && reader != null ){
            // too dangerous to do this only for logging, as it may throw NPE if Query was not constructed correctly
            // However if you need to see what Lucene queries are really used, print the rewritten query instead
            // Query rw = query.rewrite(reader);
            // System.out.println("Lucene query: " + rw.toString());
            Log.debug(Geonet.SEARCH_ENGINE, "Lucene query: " + query.toString());
        }
		new IndexSearcher(reader).search(query, cFilter, tfc); 

		Element elSummary= new Element("summary");
		elSummary.setAttribute("count", tfc.getTotalHits()+"");
		elSummary.setAttribute("type", "local");

		// topDocs could only be called once.
		// A second call will return null docs.
		TopDocs tdocs;
			
		if (buildSummary) {	
			Log.debug(Geonet.SEARCH_ENGINE, "Building summary");

			// -- prepare
			HashMap<String,HashMap<String,Object>> summaryConfigValues = getSummaryConfig(summaryConfig, resultType, maxSummaryKeys);
			Log.debug(Geonet.SEARCH_ENGINE, "ResultType is "+resultType+", SummaryKeys are "+summaryConfigValues);
			HashMap<String,HashMap<String,Integer>> summaryMaps = prepareSummaryMaps(summaryConfigValues.keySet());

			// -- get all hits from search to build the summary
			tdocs = tfc.topDocs(0, numHits);

			// -- add summary keys to summary element
			summaryMaps = buildSummaryMaps(elSummary, reader, tdocs.scoreDocs, summaryMaps);
			elSummary = addSortedSummaryKeys(elSummary, langCode, summaryMaps, summaryConfigValues);
		} else {
			tdocs = tfc.topDocs(startHit, endHit);
		}

		return Pair.read(tdocs,elSummary);
	}

	//--------------------------------------------------------------------------------

	public static Element getMetadataFromIndex(Document doc, String id)
	{
		String root       = doc.get("_root");
		String schema     = doc.get("_schema");

        String createDate = doc.get("_createDate");
        if (createDate != null) createDate = createDate.toUpperCase();

        String changeDate = doc.get("_changeDate");
        if (changeDate != null) changeDate = changeDate.toUpperCase();
        
		String source     = doc.get("_source");
		String uuid       = doc.get("_uuid");

		Element md = new Element(root);

		Element info = new Element(Edit.RootChild.INFO, Edit.NAMESPACE);

		addElement(info, Edit.Info.Elem.ID,          id);
		addElement(info, Edit.Info.Elem.UUID,        uuid);
		addElement(info, Edit.Info.Elem.SCHEMA,      schema);
		addElement(info, Edit.Info.Elem.CREATE_DATE, createDate);
		addElement(info, Edit.Info.Elem.CHANGE_DATE, changeDate);
		addElement(info, Edit.Info.Elem.SOURCE,      source);
	
		List<Fieldable> fields = doc.getFields();
    for (Iterator<Fieldable> i = fields.iterator(); i.hasNext(); ) {
      Fieldable field = i.next();
      String name  = field.name();
      String value = field.stringValue();
      if (name.equals("_cat")) addElement(info, Edit.Info.Elem.CATEGORY, value);
    }	
		md.addContent(info);
		return md;
	}

    //--------------------------------------------------------------------------------
	/**
	 * <p>
	 * Gets all metadata uuids in current searcher
	 * </p>
	 * 
	 * @return current searcher result in "fast" mode
	 * 
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
    public List<String>  getAllUuids(int maxHits) throws Exception {

				FieldSelector uuidselector = new FieldSelector() {
					public final FieldSelectorResult accept(String name) {
						if (name.equals("_uuid")) return FieldSelectorResult.LOAD;
						else return FieldSelectorResult.NO_LOAD;
					}
				};

        List<String> response = new ArrayList<String>();
				TopDocs tdocs = performQuery(0, maxHits);

        for ( ScoreDoc sdoc : tdocs.scoreDocs ) {
          Document doc = _reader.document(sdoc.doc, uuidselector);
          String uuid = doc.get("_uuid");
					if (uuid != null) response.add(uuid);
        }
        return response;
    }

	//--------------------------------------------------------------------------------
	/**
     * Search in Lucene index and return Lucene index
     * field value. Metadata records is retrieved 
     * based on its uuid.
     * 
     * @param id metadata uuid
     * @param fieldname lucene field value
     * @return
     */
    public static String getMetadataFromIndex(String indexPath, String id, String fieldname) throws Exception
    {
			List<String> fieldnames = new ArrayList<String>();
			fieldnames.add(fieldname);
			return getMetadataFromIndex(indexPath, id, fieldnames).get(fieldname);
		}

    public static Map<String,String> getMetadataFromIndex(String indexPath, String id, List<String> fieldnames) throws Exception
    {

			MapFieldSelector selector = new MapFieldSelector(fieldnames); 

			File luceneDir = new File(indexPath);
			IndexReader reader = IndexReader.open(FSDirectory.open(luceneDir), true);
      Searcher searcher = new IndexSearcher(reader);

			Map<String,String> values = new HashMap<String,String>();
        
    	try {	
				TermQuery query = new TermQuery(new Term("_uuid", id));
		    TopDocs tdocs = searcher.search(query,1);
	        
	       for ( ScoreDoc sdoc : tdocs.scoreDocs ) {
        		Document doc = reader.document(sdoc.doc, selector);

               for ( String fieldname :  fieldnames ) {
							values.put(fieldname, doc.get(fieldname));
	        	}
	        }
	        
	        searcher.close();
	        reader.close();
    	} catch (CorruptIndexException e) {
			// TODO: handle exception
    		System.out.println (e.getMessage());
		} catch (IOException e) {
			// TODO: handle exception
			System.out.println (e.getMessage());
		} finally {
			searcher.close();
			reader.close();
		}
	
    return values;
  }

	public static String analyzeQueryText(String field, String aText, PerFieldAnalyzerWrapper analyzer, HashSet<String> tokenizedFieldSet) {
		Log.debug(Geonet.SEARCH_ENGINE, "Analyze field "+field+" : "+aText);
		if (tokenizedFieldSet.contains(field)) {
			String analyzedText = analyzeText(field, aText, analyzer);	
			Log.debug(Geonet.SEARCH_ENGINE, "Analyzed text is "+analyzedText);
			return analyzedText;
		} else return aText;
	}


	/**
	 * Splits text into tokens using the Analyzer that is matched to the field.
	 * 
	 * @param requestStr
	 * @return
	 */
	public static String analyzeText(String field, String requestStr, PerFieldAnalyzerWrapper a) {

		boolean phrase = false;
		if ((requestStr.startsWith("\"") && requestStr.endsWith("\""))) phrase = true;
		
		TokenStream ts = a.tokenStream(field, new StringReader(requestStr));
		TermAttribute termAtt = ts.addAttribute(TermAttribute.class);

		List<String> tokenList = new ArrayList<String>();
		try {
			while (ts.incrementToken()) {
				tokenList.add(termAtt.term());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		StringBuffer result = new StringBuffer();

		for (int i = 0;i < tokenList.size();i++) {
			if (i > 0) {
				result.append(" ");
				result.append(tokenList.get(i));
			} else {
				result.append(tokenList.get(i));
			}
		}
		String outStr = result.toString();
		if (phrase) outStr = "\"" + outStr + "\"";
		return outStr;
	}

	// Unused at the moment - but might be useful later 
    public static String escapeLuceneChars(String aText, String excludes) {
     final StringBuilder result = new StringBuilder();
     final StringCharacterIterator iterator = new StringCharacterIterator(aText);
     char character =  iterator.current();
     while (character != CharacterIterator.DONE ) {
       if (character == '\\' && !excludes.contains("\\")) {
         result.append("\\");
       } else if (character == '!' && !excludes.contains("!")) {
         result.append("\\");
       } else if (character == '(' && !excludes.contains("(")) {
         result.append("\\");
       } else if (character == ')' && !excludes.contains(")")) {
         result.append("\\");
       } else if (character == '*' && !excludes.contains("*")) {
         result.append("\\");
       } else if (character == '+' && !excludes.contains("+")) {
         result.append("\\");
       } else if (character == '-' && !excludes.contains("-")) {
         result.append("\\");
       } else if (character == ':' && !excludes.contains(":")) {
         result.append("\\");
       } else if (character == '?' && !excludes.contains("?")) {
         result.append("\\");
       } else if (character == '[' && !excludes.contains("[")) {
         result.append("\\");
       } else if (character == ']' && !excludes.contains("]")) {
         result.append("\\");
       } else if (character == '^' && !excludes.contains("^")) {
         result.append("\\");
       } else if (character == '{' && !excludes.contains("{")) {
         result.append("\\");
       } else if (character == '}' && !excludes.contains("}")) {
         result.append("\\");
       } 
       result.append(character);
       character = iterator.next();
     }
		 Log.debug(Geonet.SEARCH_ENGINE, "Escaped: "+result.toString());
     return result.toString();
  }

}

//==============================================================================


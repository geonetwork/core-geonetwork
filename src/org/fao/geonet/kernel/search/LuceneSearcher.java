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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.SummaryComparator.SortOption;
import org.fao.geonet.kernel.search.SummaryComparator.Type;
import org.fao.geonet.kernel.search.spatial.Pair;
import org.fao.geonet.lib.Lib;
import org.jdom.Content;
import org.jdom.Element;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

//==============================================================================
// search metadata locally using lucene
//--------------------------------------------------------------------------------

public class LuceneSearcher extends MetaSearcher
{
	private SearchManager _sm;
	private String        _styleSheetName;
	private Element       _summaryConfig;

	private IndexReader   _reader;
	private Searcher      _searcher;
	private Query         _query;
	private Filter        _filter;
	private Sort          _sort;
	private Hits          _hits;
	private Hits          _lastHits;
	private Element       _elSummary;

	private int           _maxSummaryKeys;
	private int           _numHits;
	private String        _resultType;
    private String        _language;

	//--------------------------------------------------------------------------------
	// constructor
	public LuceneSearcher (SearchManager sm, String styleSheetName, Element summaryConfig)
	{
		_sm             = sm;
		_styleSheetName = styleSheetName;
		_summaryConfig  = summaryConfig;
	}

	//--------------------------------------------------------------------------------
	// MetaSearcher API

	public void search(ServiceContext srvContext, Element request, ServiceConfig config)
		throws Exception
	{
		try {
			computeQuery(srvContext, request, config);
			performQuery(request, srvContext!=null?true:false);
			initSearchRange(srvContext);
		}
		finally {
			_hits = null;
			_searcher.close();
			_searcher = null;
			setValid(false);
		}
	}

	//--------------------------------------------------------------------------------

	/**
	 * @return		An empty response if no result or a list of results. Return
	 * only geonet:info element in fast mode. 
	 */
	public Element present(ServiceContext srvContext, Element request, ServiceConfig config)
		throws Exception
	{
		if (!isValid())
			performQuery(request, srvContext!=null?true:false);

		updateSearchRange(request);

		GeonetContext gc = null;
		if (srvContext != null)
			gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);

		String sFast = request.getChildText("fast");
		boolean fast = sFast != null && sFast.equals("true");

		// srvContext.log("METASEARCHER " + _styleSheetName + " FROM: " + from +
		// "(" + sFrom + ")"); // DEBUG
		// srvContext.log("METASEARCHER " + _styleSheetName + " TO:   " + to +
		// "(" + sTo + ")"); // DEBUG

		// build response
		Element response =  new Element("response");
		response.setAttribute("from",  getFrom()+"");
		response.setAttribute("to",    getTo()+"");

		response.addContent((Element)_elSummary.clone());

		if (getTo() > 0)
		{
			for(int i = getFrom() - 1; i < getTo(); i++)
			{
				Document doc = _hits.doc(i);
				String id = doc.get("_id");
				Element md = new Element ("md");

				if (fast)
				{
					md = getMetadataFromIndex(doc, id);
				}
				else if (srvContext != null)
				{
					md = gc.getDataManager().getMetadata(srvContext, id, false);
				} else {
					md = null;
				}

				//--- the search result is buffered so a metadata could have been deleted
				//--- just before showing search results

				if (md != null)
				{
					// Calculate score and add it to info elem
					Float score = _hits.score(i);
					Element info = md.getChild (Edit.RootChild.INFO, Edit.NAMESPACE);
					addElement(info, Edit.Info.Elem.SCORE, score.toString());

					response.addContent(md);
				}
			}
		}
		
		_hits = null;
		_searcher.close();
        _searcher = null;
        setValid(false);
        
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
	// RGFIX: check this

	public void close()
	{
		try
		{
			if (_searcher != null) {
                _searcher.close();
                _searcher = null;
            }
            setValid(false);
		} catch (IOException e) {
			e.printStackTrace(); 
		} // DEBUG
	}

	//--------------------------------------------------------------------------------
	// private setup, index, delete and search functions

	private void computeQuery(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception
	{
		String sMaxSummaryKeys = request.getChildText("maxSummaryKeys");
		if (sMaxSummaryKeys == null) sMaxSummaryKeys = config.getValue("maxSummaryKeys", "10");
		_maxSummaryKeys = Integer.parseInt(sMaxSummaryKeys);

		if (srvContext != null) {
			GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
	
			Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);
	
			// if 'restrict to' is set then don't add any other user/group info
			if (request.getChild("group") == null) {
				Set<String> hs = gc.getAccessManager().getUserGroups(dbms, srvContext.getUserSession(), srvContext.getIpAddress());
	
				for (String group : hs)
					request.addContent(new Element("group").addContent(group));
	
				UserSession us = srvContext.getUserSession();
	
				String owner = us.getUserId();
	
				if (owner != null)
					request.addContent(new Element("owner").addContent(owner));
	
			//--- in case of an admin we have to show all results
	
				if (us.isAuthenticated())
				{
					if (us.getProfile().equals(Geonet.Profile.ADMINISTRATOR))
						request.addContent(new Element("isAdmin").addContent("true"));
		
					else if (us.getProfile().equals(Geonet.Profile.REVIEWER))
						request.addContent(new Element("isReviewer").addContent("true"));
				}
			}
			//--- some other stuff

			Log.debug(Geonet.SEARCH_ENGINE, "CRITERIA:\n"+ Xml.getString(request));
			request.addContent(Lib.db.select(dbms, "Regions", "region"));
		}


		Element xmlQuery = _sm.transform(_styleSheetName, request);
		Log.debug(Geonet.SEARCH_ENGINE, "XML QUERY:\n"+ Xml.getString(xmlQuery));

		if (srvContext != null)
        	_language = srvContext.getLanguage();
        else
        	_language = "eng"; // TODO : set default not language in config
        
		_query = makeQuery(xmlQuery);
		
		Geometry geometry = getGeometry(request);
        if (geometry != null) {
            _filter = new CachingWrapperFilter(_sm.getSpatial().filter(_query, geometry, request));
        }
        
        String sortBy = Util.getParam(request, Geonet.SearchResult.SORT_BY,
				Geonet.SearchResult.SortBy.RELEVANCE);
		boolean sortOrder = (Util.getParam(request,
				Geonet.SearchResult.SORT_ORDER, "").equals("") ? true : false);
		Log.debug(Geonet.SEARCH_ENGINE, "Sorting by : " + sortBy);

		_sort = makeSort(Collections
				.singletonList(Pair.read(sortBy, sortOrder)));
		
		Content child = request.getChild(Geonet.SearchResult.RESULT_TYPE);
        if (child == null) {
            _resultType = Geonet.SearchResult.ResultType.HITS;
        } else {
            _resultType = child.getValue();
        }
	}

	/**
	 * Execute Lucene query with sorting option. 
	 * 
	 * Default sort by option is RELEVANCE.
	 * Default sort order option is not reverse order. Reverse order is active 
	 * if sort order option is set and not null
	 */
	private void performQuery(Element request, boolean keepSearch) throws Exception
	{
		_reader = _sm.getIndexReader();
		_searcher = new IndexSearcher(_reader);
		
		if (_filter == null) {
			_hits = _searcher.search(_query, _sort);
		} else {
			_hits = _searcher.search(_query, new CachingWrapperFilter(_filter), _sort);
		}

		_numHits = _hits.length();
		
		if (keepSearch)
			_lastHits = _hits;

		Log.debug(Geonet.SEARCH_ENGINE, "Hits found : "+ _hits.length());

		if (_elSummary == null) {
            _elSummary = makeSummary(_hits, getSize(), _summaryConfig, _resultType, _maxSummaryKeys, _language);
        }

		setValid(true);
	}
	
	private Geometry getGeometry(Element request) throws Exception
    {
        String geomWKT = Util.getParam(request, Geonet.SearchResult.GEOMETRY, null);
        if (geomWKT != null) {
            WKTReader reader = new WKTReader();
            return reader.read(geomWKT);
        }
        return null;
    }
	
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
        Sort sort = new Sort(sortFields.toArray(new SortField[sortFields.size()]));

        return sort;
    }
    
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
        
        SortField sortField = new SortField(sortBy, sortType, sortOrder);
        
        return sortField;
    }
    
	//--------------------------------------------------------------------------------
	/**
	 *  Makes a new lucene query
	 *  
	 *  Converts to lowercase if needed as the StandardAnalyzer.
	 *  Therefore, some fields (eg. uuid, subject) are indexed 
	 *  using a different analyzer @see SearchManager.
	 *   
	 */
	public static Query makeQuery(Element xmlQuery) throws Exception
	{
		String name = xmlQuery.getName();
		Query returnValue = null;
		
		if (name.equals("TermQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
			String txt = xmlQuery.getAttributeValue("txt");//.toLowerCase();
			returnValue = new TermQuery(new Term(fld, txt));
		}
		else if (name.equals("FuzzyQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
			Float sim = Float.valueOf(xmlQuery.getAttributeValue("sim"));
			String txt = xmlQuery.getAttributeValue("txt").toLowerCase();
			returnValue = new FuzzyQuery(new Term(fld, txt), sim.floatValue());
		}
		else if (name.equals("PrefixQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
			String txt = xmlQuery.getAttributeValue("txt").toLowerCase();
			returnValue = new PrefixQuery(new Term(fld, txt));
		}
		else if (name.equals("MatchAllDocsQuery"))
		{
			MatchAllDocsQuery query = new MatchAllDocsQuery();
			return query;
		}
		else if (name.equals("WildcardQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
			String txt = xmlQuery.getAttributeValue("txt").toLowerCase();
			returnValue = new WildcardQuery(new Term(fld, txt));
		}
		else if (name.equals("PhraseQuery"))
		{
			PhraseQuery query = new PhraseQuery();
			for(Iterator i = xmlQuery.getChildren().iterator(); i.hasNext();) {
				Element xmlTerm = (Element) i.next();
				String fld = xmlTerm.getAttributeValue("fld");
				String txt = xmlTerm.getAttributeValue("txt").toLowerCase();
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

			Term lowerTerm = (lowerTxt == null ? null : new Term(fld, lowerTxt.toLowerCase()));
			Term upperTerm = (upperTxt == null ? null : new Term(fld, upperTxt.toLowerCase()));

			returnValue = new RangeQuery(lowerTerm, upperTerm, inclusive);
		}
		else if (name.equals("BooleanQuery"))
		{
			BooleanQuery query = new BooleanQuery();
			for (Iterator iter = xmlQuery.getChildren().iterator(); iter.hasNext(); )
			{
				Element xmlBooleanClause = (Element)iter.next();
				String  sRequired   = xmlBooleanClause.getAttributeValue("required");
				String  sProhibited = xmlBooleanClause.getAttributeValue("prohibited");
				boolean required    = sRequired   != null && sRequired.equals("true");
				boolean prohibited  = sProhibited != null && sProhibited.equals("true");
				BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(required, prohibited);
				List<Element> subQueries = xmlBooleanClause.getChildren();
				Element xmlSubQuery;
				if (subQueries!=null && subQueries.size()!=0) {
					xmlSubQuery = subQueries.get(0);
					query.add(makeQuery(xmlSubQuery), occur);
				}
			}
			BooleanQuery.setMaxClauseCount(16384); // FIXME: quick fix; using Filters should be better
			
			returnValue = query;
		}
		else throw new Exception("unknown lucene query type: " + name);
		
		Log.debug(Geonet.SEARCH_ENGINE, "Lucene Query: " + returnValue.toString());
		return returnValue;
	}

	//--------------------------------------------------------------------------------

	public static Element makeSummary(Hits hits, int count, Element summaryConfig, String resultType, int maxSummaryKeys, String langCode) throws Exception
	{
		Element elSummary  = new Element("summary");

		elSummary.setAttribute("count", count+"");
		elSummary.setAttribute("type", "local");
		
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

            SortOption sortOption = SortOption.parse(order);
            SummaryComparator summaryComparator = new SummaryComparator(sortOption, Type.parse(type), langCode, summaryConfig.getChild("typeConfig"));
            summarize(elSummary, hits, key, count, plural, name, summaryComparator, max);
        }
        return elSummary;
	}
	
	private static void summarize(Element elSummary, Hits phits, String indexKey, int count, String rootElemName, String elemName,
            SummaryComparator summaryComparator, int max) throws CorruptIndexException, IOException
    {

        HashMap<String, Integer> summary = new HashMap<String, Integer>();
        for (int i = 0; i < count; i++) {
            Document doc = phits.doc(i);
            String hits[] = doc.getValues(indexKey);
            if (hits != null) // if there are no categories lucene returns null
                // instead of an empty array
                for (int j = 0; j < hits.length; j++) {
                    String info = hits[j];
                    Integer catCount = (Integer) summary.get(info);
                    if (catCount == null)
                        catCount = new Integer(1);
                    else
                        catCount = new Integer(catCount.intValue() + 1);
                    summary.put(info, catCount);
                }
        }

        Element rootElem = new Element(rootElemName);
        // sort according to frequency
        TreeSet<Map.Entry<String, Integer>> sortedSummary = new TreeSet<Map.Entry<String, Integer>>(summaryComparator);
        sortedSummary.addAll(summary.entrySet());

        int nKeys = 0;
        for (Iterator iter = sortedSummary.iterator(); iter.hasNext();) {
            if (++nKeys > max)
                break;

            Map.Entry me = (Map.Entry) iter.next();
            String keyword = (String) me.getKey();
            Integer keyCount = (Integer) me.getValue();

            Element childElem = new Element(elemName);
            childElem.setAttribute("count", keyCount.toString());
            childElem.setAttribute("name", keyword);
            rootElem.addContent(childElem);
        }
        elSummary.addContent(rootElem);
    }

	//--------------------------------------------------------------------------------

	public static Element getMetadataFromIndex(Document doc, String id)
	{
		String root       = doc.get("_root");
		String schema     = doc.get("_schema");
		String createDate = doc.get("_createDate").toUpperCase();
		String changeDate = doc.get("_changeDate").toUpperCase();
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
		
		List<Field> fields = doc.getFields();
		for (Iterator<Field> i = fields.iterator(); i.hasNext(); ) {
			Field field = i.next();
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
	 * Gets results in current searcher
	 * </p>
	 * 
	 * @return current searcher result in "fast" mode
	 * 
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
    public Element getAll() throws CorruptIndexException, IOException {
        Element response = new Element("response");
        if (_lastHits == null || _lastHits.length() == 0) {
            response.setAttribute("from", 0 + "");
            response.setAttribute("to", 0 + "");
            return response;
        }

        response.setAttribute("from", 1 + "");
        response.setAttribute("to", _lastHits.length() + "");

        for (int i = 0; i < _lastHits.length(); i++) {
            Document doc = _lastHits.doc(i);
            String id = doc.get("_id");

            // FAST mode
            Element md = getMetadataFromIndex(doc, id);
            response.addContent(md);
        }
        return response;
    }

	/**
     * Search in Lucene index and return Lucene index
     * field value. Metadata records is retrieved 
     * based on its uuid.
     * 
     * @param id metadata uuid
     * @param fieldname lucene field value
     * @param languageCode (NOT USED : only one language index here).
     * @return
     */
    public static String getMetadataFromIndex(String appPath, String id, String fieldname, String languageCode) throws Exception
    {
    	String value = "";
    	File luceneDir = new File(appPath + "WEB-INF/lucene/nonspatial");
        IndexReader reader = IndexReader.open(luceneDir);
        Searcher searcher = new IndexSearcher(reader);
        
    	try {	
			TermQuery query = new TermQuery(new Term("_uuid", id));
	    	Hits hits = searcher.search(query);
	        
	        for (int j=0; j<hits.length(); j++) {
        		Document doc = hits.doc(j);
	    	
		        Element record = new Element("record");
		        List<Field> fields = doc.getFields();
		        
		        for (Iterator<Field> i = fields.iterator(); i.hasNext();) {
		            Field field = i.next();
		            String name = field.name();
		            
		            if (name.equals(fieldname)) {
		            	value = field.stringValue();
		            	break;
		            	// TODO : handle multiple fields ?
		            }
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
	
        return value;
    }
}

//==============================================================================


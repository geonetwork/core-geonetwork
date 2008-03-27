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

import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.search.LuceneUtils;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

//==============================================================================
// search metadata locally using lucene
//--------------------------------------------------------------------------------

public class LuceneSearcher extends MetaSearcher
{
	private SearchManager _sm;
	private String        _styleSheetName;

	private IndexReader   _reader;
	private IndexSearcher _searcher;
	private Query         _query;
	private Hits          _hits;
	private Element       _elSummary;

	private int           _maxSummaryKeys;

	//--------------------------------------------------------------------------------
	// constructor
	public LuceneSearcher(SearchManager sm, String styleSheetName)
	{
		_sm             = sm;
		_styleSheetName = styleSheetName;
	}

	//--------------------------------------------------------------------------------
	// MetaSearcher API

	public void search(ServiceContext srvContext, Element request, ServiceConfig config)
		throws Exception
	{
		computeQuery(srvContext, request, config);
		performQuery(request);
		initSearchRange(srvContext);
	}

	//--------------------------------------------------------------------------------

	public Element present(ServiceContext srvContext, Element request, ServiceConfig config)
		throws Exception
	{
		if (!isValid()) performQuery(request);

		updateSearchRange(request);

		GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);

		String sFast = request.getChildText("fast");
		boolean fast = sFast != null && sFast.equals("true");

		// srvContext.log("METASEARCHER " + _styleSheetName + " FROM: " + from + "(" + sFrom + ")"); // DEBUG
		// srvContext.log("METASEARCHER " + _styleSheetName + " TO:   " + to   + "(" + sTo   + ")"); // DEBUG

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
				else
				{
					md = gc.getDataManager().getMetadata(srvContext, id, false);
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
		return response;
	}

	//--------------------------------------------------------------------------------

	public int getSize()
	{
		return _hits.length();
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
			_reader.close();
		}
		catch (IOException e) { e.printStackTrace(); } // DEBUG
	}

	//--------------------------------------------------------------------------------
	// private setup, index, delete and search functions

	private void computeQuery(ServiceContext srvContext, Element request, ServiceConfig config) throws Exception
	{
		String sMaxSummaryKeys = request.getChildText("maxSummaryKeys");
		if (sMaxSummaryKeys == null) sMaxSummaryKeys = config.getValue("maxSummaryKeys", "10");
		_maxSummaryKeys = Integer.parseInt(sMaxSummaryKeys);

		GeonetContext gc = (GeonetContext) srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
		AccessManager am = gc.getAccessManager();

		Dbms dbms = (Dbms) srvContext.getResourceManager().open(Geonet.Res.MAIN_DB);

		// if 'restrict to' is set then don't add any other user/group info
		if (request.getChild("group") == null) {
			Set<String> hs = gc.getAccessManager().getUserGroups(dbms, srvContext.getUserSession(), srvContext.getIpAddress());

			for (String group : hs)
				request.addContent(new Element("group").addContent(group));

			String owner = srvContext.getUserSession().getUserId();

			if (owner != null)
				request.addContent(new Element("owner").addContent(owner));

		//--- in case of an admin we have to show all results

			UserSession us = srvContext.getUserSession();

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

		Element xmlQuery = _sm.transform(_styleSheetName, request);
		Log.debug(Geonet.SEARCH_ENGINE, "XML QUERY:\n"+ Xml.getString(xmlQuery));

		_query = makeQuery(xmlQuery);
	}

	//--------------------------------------------------------------------------------
	// perform the query

	private void performQuery(Element request) throws Exception
	{
		String sortBy = Util.getParam(request, Geonet.SearchResult.SORT_BY, Geonet.SearchResult.SortBy.RELEVANCE);

		Log.debug(Geonet.SEARCH_ENGINE, "Sorting by : "+ sortBy);

		Sort sort = null;

		if (sortBy.equals(Geonet.SearchResult.SortBy.DATE))
			sort = new Sort(new SortField[]
								{
									new SortField("_changeDate", SortField.STRING, true),
									SortField.FIELD_SCORE
								});

		else if (sortBy.equals(Geonet.SearchResult.SortBy.POPULARITY))
			sort = new Sort(new SortField[]
								{
									new SortField("_popularity", SortField.INT, true),
									SortField.FIELD_SCORE
								});

		else if (sortBy.equals(Geonet.SearchResult.SortBy.RATING))
			sort = new Sort(new SortField[]
								{
									new SortField("_rating", SortField.INT, true),
									SortField.FIELD_SCORE
								});

		_reader = IndexReader.open(_sm.getLuceneDir());
		_searcher = new IndexSearcher(_reader);
		_hits = _searcher.search(_query, sort);

		Log.debug(Geonet.SEARCH_ENGINE, "Hits found : "+ _hits.length());

		makeSummary();

		setValid(true);
	}

	//--------------------------------------------------------------------------------
	// makes a new lucene query
	// converts to lowercase if needed as the StandardAnalyzer

	public static Query makeQuery(Element xmlQuery) throws Exception
	{
		String name = xmlQuery.getName();
		Query returnValue = null;
		
		if (name.equals("TermQuery"))
		{
			String fld = xmlQuery.getAttributeValue("fld");
			String txt = xmlQuery.getAttributeValue("txt").toLowerCase();
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
				Element xmlSubQuery = (Element)xmlBooleanClause.getChildren().get(0);
				query.add(makeQuery(xmlSubQuery), occur);
			}
			query.setMaxClauseCount(16384); // FIXME: quick fix; using Filters should be better
			
			returnValue = query;
		}
		else throw new Exception("unknown lucene query type: " + name);
		
		Log.debug(Geonet.SEARCH_ENGINE, "Lucene Query: " + returnValue.toString());
		return returnValue;
	}

	//--------------------------------------------------------------------------------

	private void makeSummary() throws Exception
	{
		_elSummary = new Element("summary");

		int count = getSize();

		_elSummary.setAttribute("count", count+"");
		_elSummary.setAttribute("type", "local");

		// count keyword frequencies
		Element elKeywords = new Element("keywords");
		Hashtable htKeywords = new Hashtable();
		for(int i = 0; i < count; i++)
		{
			Document doc = _hits.doc(i);
			String keywords[] = doc.getValues("keyword");
			if (keywords != null) // if there are no keywords lucene returns null instead of an empty array
				for (int j = 0; j < keywords.length; j++)
				{
					String keyword = keywords[j];
					Integer keyCount = (Integer)htKeywords.get(keyword);
					if (keyCount == null) keyCount = new Integer(1);
					else                  keyCount = new Integer(keyCount.intValue() + 1);
					htKeywords.put(keyword, keyCount);
				}
		}
		// sort keywords according to frequency
		TreeSet setKeywords = new TreeSet(new Comparator()
			  {
					public int compare(Object p1, Object p2)
					{
						Map.Entry me1 = (Map.Entry)p1;
						Map.Entry me2 = (Map.Entry)p2;
						String  key1   = (String)me1.getKey();
						String  key2   = (String)me2.getKey();
						Integer count1 = (Integer)me1.getValue();
						Integer count2 = (Integer)me2.getValue();
						int cmp = count2.compareTo(count1);
						if (cmp != 0) return cmp;
						else          return key1.compareTo(key2);
					}
				});
		setKeywords.addAll(htKeywords.entrySet());

		int nKeys = 0;
		for (Iterator iter = setKeywords.iterator(); iter.hasNext(); )
		{
			if (++nKeys > _maxSummaryKeys) break;

			Map.Entry me = (Map.Entry)iter.next();
			String keyword   = (String)me.getKey();
			Integer keyCount = (Integer)me.getValue();

			Element elKeyword = new Element("keyword");
			elKeyword.setAttribute("count", keyCount.toString());
			elKeyword.setAttribute("name", keyword);
			elKeywords.addContent(elKeyword);
		}
		_elSummary.addContent(elKeywords);

		// count categories frequencies
		Element elCategories = new Element("categories");
		Hashtable htCategories = new Hashtable();
		for(int i = 0; i < count; i++)
		{
			Document doc = _hits.doc(i);
			String categories[] = doc.getValues("_cat");
			if (categories != null) // if there are no categories lucene returns null instead of an empty array
				for (int j = 0; j < categories.length; j++)
				{
					String category = categories[j];
					Integer catCount = (Integer)htCategories.get(category);
					if (catCount == null) catCount = new Integer(1);
					else                  catCount = new Integer(catCount.intValue() + 1);
					htCategories.put(category, catCount);
				}
		}
		// sort categories according to name
		TreeSet setCategories = new TreeSet(new Comparator()
			  {
					public int compare(Object p1, Object p2)
					{
						Map.Entry me1 = (Map.Entry)p1;
						Map.Entry me2 = (Map.Entry)p2;
						String  cat1   = (String)me1.getKey();
						String  cat2   = (String)me2.getKey();
						return cat1.compareTo(cat2);
					}
				});
		setCategories.addAll(htCategories.entrySet());

		for (Iterator iter = setCategories.iterator(); iter.hasNext(); )
		{
			Map.Entry me = (Map.Entry)iter.next();
			String category  = (String)me.getKey();
			Integer catCount = (Integer)me.getValue();

			Element elCategory = new Element("category");
			elCategory.setAttribute("count", catCount.toString());
			elCategory.setAttribute("name", category);
			elCategories.addContent(elCategory);
		}
		_elSummary.addContent(elCategories);

		// count sources frequencies
		Element elSources = new Element("sources");
		Hashtable htSources = new Hashtable();
		for(int i = 0; i < count; i++)
		{
			Document doc = _hits.doc(i);
			String source = doc.get("_source");
			Integer sourceCount = (Integer)htSources.get(source);
			if (sourceCount == null) sourceCount = new Integer(1);
			else                     sourceCount = new Integer(sourceCount.intValue() + 1);
			htSources.put(source, sourceCount);
		}
		// sort sources according to frequency
		TreeSet setSources = new TreeSet(new Comparator()
			  {
					public int compare(Object p1, Object p2)
					{
						Map.Entry me1    = (Map.Entry)p1;
						Map.Entry me2    = (Map.Entry)p2;
						String    key1   = (String)me1.getKey();
						String    key2   = (String)me2.getKey();
						Integer   count1 = (Integer)me1.getValue();
						Integer   count2 = (Integer)me2.getValue();
						int cmp = count2.compareTo(count1);
						if (cmp != 0) return cmp;
						else          return key1.compareTo(key2);
					}
				});
		setSources.addAll(htSources.entrySet());

		for (Iterator iter = setSources.iterator(); iter.hasNext(); )
		{
			Map.Entry me = (Map.Entry)iter.next();
			String  source   = (String)me.getKey();
			Integer keyCount = (Integer)me.getValue();

			Element elSource = new Element("source");
			elSource.setAttribute("count", keyCount.toString());
			elSource.setAttribute("name",  source);
			elSources.addContent(elSource);
		}
		_elSummary.addContent(elSources);
	}

	//--------------------------------------------------------------------------------

	private static Element getMetadataFromIndex(Document doc, String id)
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

		for (Enumeration enu = doc.fields(); enu.hasMoreElements(); )
		{
			Field field = (Field) enu.nextElement();
			String name  = field.name();
			String value = field.stringValue();

			if (name.equals("_cat")) addElement(info, Edit.Info.Elem.CATEGORY, value);
		}
		md.addContent(info);
		return md;
	}
}

//==============================================================================


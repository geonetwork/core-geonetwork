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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import jeeves.resources.dbms.Dbms;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw.TypeName;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.LuceneUtils;
import org.jdom.Element;

//=============================================================================

class CatalogSearcher
{
	//---------------------------------------------------------------------------
	//---
	//--- Main search method
	//---
	//---------------------------------------------------------------------------

	//--- should return a list of id that match the given filter, ordered by sortFields

	public List<ResultItem> search(ServiceContext context, Element filterExpr, Set<TypeName> typeNames,
										List<SortField> sortFields) throws CatalogException
	{
		Element luceneExpr = filterToLucene(context, filterExpr);

		if (luceneExpr != null)
		{
			checkForErrors(luceneExpr);
			remapFields(luceneExpr);
			convertPhrases(luceneExpr);
		}
//System.out.println("============================================");
//System.out.println("LUCENE:\n"+Xml.getString(luceneExpr));

		try
		{
			List<ResultItem> results = search(context, luceneExpr);

			sort(results, sortFields);

			return results;
		}
		catch (Exception e)
		{
			context.error("Error while searching metadata ");
			context.error("  (C) StackTrace:\n"+ Util.getStackTrace(e));

			throw new NoApplicableCodeEx("Raised exception while searching metadata : "+ e);
		}
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private Element filterToLucene(ServiceContext context, Element filterExpr) throws NoApplicableCodeEx
	{
		if (filterExpr == null)
			return null;

		String styleSheet = context.getAppPath() + Geonet.Path.CSW + Geonet.File.FILTER_TO_LUCENE;

		try
		{
			return Xml.transform(filterExpr, styleSheet);
		}
		catch (Exception e)
		{
			context.error("Error during Filter to Lucene conversion : "+ e);
			context.error("  (C) StackTrace\n"+ Util.getStackTrace(e));

			throw new NoApplicableCodeEx("Error during Filter to Lucene conversion : "+ e);
		}
	}

	//---------------------------------------------------------------------------

	private void checkForErrors(Element elem) throws InvalidParameterValueEx
	{
		List children = elem.getChildren();

		if (elem.getName().equals("error"))
		{
			String type = elem.getAttributeValue("type");
			String oper = Xml.getString((Element) children.get(0));

			throw new InvalidParameterValueEx(type, oper);
		}

		for(int i=0; i<children.size(); i++)
			checkForErrors((Element) children.get(i));
	}

	//---------------------------------------------------------------------------

	private void convertPhrases(Element elem)
	{
		if (elem.getName().equals("TermQuery"))
		{
			String field = elem.getAttributeValue("fld");
			String text  = elem.getAttributeValue("txt");

			if (text.indexOf(" ") != -1)
			{
				elem.setName("PhraseQuery");

				StringTokenizer st = new StringTokenizer(text, " ");

				while (st.hasMoreTokens())
				{
					Element term = new Element("TermQuery");
					term.setAttribute("fld", field);
					term.setAttribute("txt", st.nextToken());

					elem.addContent(term);
				}
			}
		}

		else
		{
			List children = elem.getChildren();

			for(int i=0; i<children.size(); i++)
				convertPhrases((Element) children.get(i));
		}
	}

	//---------------------------------------------------------------------------

	private void remapFields(Element elem)
	{
		String field = elem.getAttributeValue("fld");

		if (field != null)
		{
			if (field.equals(""))
				field = "any";

			String mapped = FieldMapper.map(field);

			if (mapped != null)
				elem.setAttribute("fld", mapped);
			else
				Log.info(Geonet.CSW_SEARCH, "Unknown queryable field : "+ field);
		}

		List children = elem.getChildren();

		for(int i=0; i<children.size(); i++)
			remapFields((Element) children.get(i));
	}

	//---------------------------------------------------------------------------

	private List<ResultItem> search(ServiceContext context, Element luceneExpr) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SearchManager sm = gc.getSearchmanager();

		if (luceneExpr != null)
			Log.debug(Geonet.CSW_SEARCH, "Search criteria:\n"+ Xml.getString(luceneExpr));

		Query data   = (luceneExpr == null) ? null : LuceneSearcher.makeQuery(luceneExpr);
		Query groups = getGroupsQuery(context);
		Query templ  = new TermQuery(new Term("_isTemplate", "n"));

		//--- put query on groups in AND with lucene query

		BooleanQuery query  = new BooleanQuery();

		BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(true, false);					
		if (data != null)
			query.add(data,   occur);

		query.add(groups, occur);
		query.add(templ,  occur);

		//--- proper search

		IndexReader   reader   = IndexReader.open(sm.getLuceneDir());
		IndexSearcher searcher = new IndexSearcher(reader);

		try
		{
			Hits hits = searcher.search(query);

			Log.debug(Geonet.CSW_SEARCH, "Records matched : "+ hits.length());

			//--- retrieve results

			ArrayList<ResultItem> results = new ArrayList<ResultItem>();

			for(int i=0; i<hits.length(); i++)
			{
				Document doc = hits.doc(i);
				String   id  = doc.get("_id");

				ResultItem ri = new ResultItem(id);
				results.add(ri);

				for(String field : FieldMapper.getMappedFields())
				{
					String value = doc.get(field);

					if (value != null)
						ri.add(field, value);
				}
			}

			return results;
		}
		finally
		{
			searcher.close();
			reader  .close();
		}
	}

	//---------------------------------------------------------------------------

	private Query getGroupsQuery(ServiceContext context) throws Exception
	{
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		AccessManager am = gc.getAccessManager();
		Set<String>   hs = am.getUserGroups(dbms, context.getUserSession(), context.getIpAddress());

		BooleanQuery query = new BooleanQuery();

		String operView = "_op0";

		BooleanClause.Occur occur = LuceneUtils.convertRequiredAndProhibitedToOccur(false, false);							
		for(Object group: hs)
		{
			TermQuery tq = new TermQuery(new Term(operView, group.toString()));
			query.add(tq, occur);
		}

		return query;
	}

	//---------------------------------------------------------------------------

	private void sort(List<ResultItem> results, List<SortField> sortFields)
	{
		if (sortFields.isEmpty())
			return;

		ArrayList<SortField> fields = new ArrayList<SortField>();

		for(SortField sf : sortFields)
		{
			String mapped = FieldMapper.map(sf.field);

			if (mapped != null)
			{
				sf.field = mapped;
				fields.add(sf);
			}
			else
				Log.info(Geonet.CSW_SEARCH, "Skipping unknown sortable field : "+ sf.field);
		}

		Collections.sort(results, new ItemComparator(fields));
	}
}

//=============================================================================

class ResultItem
{
	private String id;

	private HashMap<String, String> hmFields = new HashMap<String, String>();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ResultItem(String id)
	{
		this.id = id;
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getID() { return id; }

	//---------------------------------------------------------------------------

	public void add(String field, String value)
	{
		hmFields.put(field, value);
	}

	//---------------------------------------------------------------------------

	public String getValue(String field) { return hmFields.get(field); }
}

//=============================================================================

class ItemComparator implements Comparator<ResultItem>
{
	private List<SortField> sortFields;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public ItemComparator(List<SortField> sf)
	{
		sortFields = sf;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Comparator interface
	//---
	//---------------------------------------------------------------------------

	public int compare(ResultItem ri1, ResultItem ri2)
	{
		for(SortField sf : sortFields)
		{
			String value1 = ri1.getValue(sf.field);
			String value2 = ri2.getValue(sf.field);

			//--- some metadata may have null values for some fields
			//--- in this case we push null values at the bottom

			if (value1 == null && value2 != null)
				return 1;

			if (value1 != null && value2 == null)
				return -1;

			if (value1 == null || value2 == null)
				return 0;

			//--- values are ok, do a proper comparison

			int comp = value1.compareTo(value2);

			if (comp == 0)
				continue;

			return (!sf.descend) ? comp : -comp;
		}

		return 0;
	}
}

//=============================================================================


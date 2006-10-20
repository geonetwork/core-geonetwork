//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.kernel.search;

import com.k_int.IR.*;
import java.util.*;
import jeeves.server.*;
import jeeves.server.context.*;
import jeeves.utils.Xml;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.jdom.input.DOMBuilder;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.services.util.MainUtil;

//--------------------------------------------------------------------------------
// search metadata remotely using Z39.50
//--------------------------------------------------------------------------------

class Z3950Searcher extends MetaSearcher
{
	public final static int EXECUTING = SearchTask.TASK_EXECUTING;
	public final static int FAILURE   = SearchTask.TASK_FAILURE;
	public final static int COMPLETE  = SearchTask.TASK_COMPLETE;
	public final static int IDLE      = SearchTask.TASK_IDLE;

	private SearchManager _sm;
	private String        _styleSheetName;

	private SearchTask    _st;

	//--------------------------------------------------------------------------------
	// constructor
	public Z3950Searcher(SearchManager sm, String styleSheetName)
	{
		_sm             = sm;
		_styleSheetName = styleSheetName;
	}

	//--------------------------------------------------------------------------------
	// MetaSearcher API

	public void search(ServiceContext srvContext, Element request, ServiceConfig config)
		throws Exception
	{
		// System.out.println("CRITERIA:\n" + jeeves.utils.Xml.getString(request)); // DEBUG

		Element xmlQuery = _sm.transform(_styleSheetName, request);

		// System.out.println("XML QUERY:\n" + jeeves.utils.Xml.getString(xmlQuery)); // DEBUG

		String query = newQuery(xmlQuery);

		// System.out.println("QUERY: " + query); // DEBUG

		// get request parameters
		Vector servers = new Vector();
		for (Iterator iter = request.getChildren(Geonet.SearchResult.SERVERS).iterator(); iter.hasNext(); )
		{
			String server = ((Element)iter.next()).getText();
			servers.add(server);
		}
		String sTimeout  = request.getChildText("timeout");
		int timeout;
		if (sTimeout == null) timeout = 10;
		else
		{
			try { timeout = Integer.parseInt(sTimeout); }
			catch (NumberFormatException nfe) { throw new IllegalArgumentException("Bad 'timeout' parameter parameter: " + sTimeout); }
		}
		// perform the search
		// initialize the collection
		Vector collection_ids = new Vector();
		for (int i = 0; i < servers.size(); i++)
		{
			String name = (String)servers.elementAt(i);
			collection_ids.add(name);
		}
		// build the query
		IRQuery e = new IRQuery();
		e.collections = collection_ids;
		e.query = new com.k_int.IR.QueryModels.PrefixString(query);

		// create the search task and perform search
		_st = _sm.getSearchable().createTask(e,null);
		try
		{
			_st.evaluate(timeout * 1000);
		}
		catch ( TimeoutExceededException tee )
		{
			// tee.printStackTrace(); // DEBUG
		}
		initSearchRange(srvContext);
	}

	public Element present(ServiceContext srvContext, Element request, ServiceConfig config)
		throws Exception
	{
		updateSearchRange(request);
		
		// get request parameters
		String syntax = request.getChildText("syntax");
		if (syntax == null)
			syntax = config.getValue("syntax", "b");

		// get results
		Element response =  new Element("response");
		response.setAttribute("from",  getFrom()+"");
		response.setAttribute("to",    getTo()+"");

		Element summary = makeSummary();
		response.addContent(summary);

		if (getTo() > 0)
		{
			RecordFormatSpecification rfs = new RecordFormatSpecification("xml", null, syntax);
			InformationFragment frags[] = _st.getTaskResultSet().getFragment(getFrom(), (getTo() - getFrom() + 1), rfs);
			for (int i = 0; i < frags.length; i++)
			{
				InformationFragment frag = frags[i];
				try
				{
					DOMBuilder builder = new DOMBuilder();

					// System.out.println("ORIGINAL OBJECT IN FRAGMENT: " + frag.getOriginalObject()); // DEBUG

					org.w3c.dom.Document doc = frag.getDocument();
					org.w3c.dom.Element  el  = doc.getDocumentElement();
					Element md = builder.build(el);
					md.detach();

					Element info = new Element(Edit.RootChild.INFO, Edit.NAMESPACE);
					addElement(info, Edit.Info.Elem.ID,     (getFrom() + i)+"");
					addElement(info, Edit.Info.Elem.SERVER, frag.getSourceRepositoryID());
					md.addContent(info);

					response.addContent(md);
				}
				catch (Exception ex)
				{
					// ex.printStackTrace(); // DEBUG
					Element error = new Element("error");;
					error.setAttribute("server",  frag.getSourceRepositoryID());
					error.setAttribute("id",      (getFrom() + i)+"");
					error.setAttribute("message", ex.getClass().getName() + ": " + ex.getMessage());
					response.addContent(error);
				}
			}
		}
		return response;
	}

	public int getSize()
	{
		return _st.getTaskResultSet().getFragmentCount();
	}

	public Element getSummary()
	{
		Element response =  new Element("response");
		response.addContent(makeSummary());
		return response;
	}

	/** closes the connection(s)
	  */
	public void close()
	{
		if (_st != null)
		{
			_st.destroyTask();
			_st = null;
		}
	}

	//--------------------------------------------------------------------------------
	// private methods

	// makes a new query
	private String newQuery(Element xmlQuery)
		throws Exception
	{
		String name = xmlQuery.getName();
		if (name.equals("query"))
		{
			String attrset = xmlQuery.getAttributeValue("attrset");
			List children = xmlQuery.getChildren();
			if (children.size() == 0)
				throw new JeevesException("empty Z59.50 query: " + Xml.getString(xmlQuery), "empty-query");
			
			Element child = (Element)children.get(0);
			return "@attrset " + attrset + " " + newQuery(child);
		}
		else if (name.equals("and") || name.equals("or") || name.equals("not"))
		{
			Element leftChild  = (Element)xmlQuery.getChildren().get(0);
			Element rightChild = (Element)xmlQuery.getChildren().get(1);
			return "@" + name + " " + newQuery(leftChild) + " " + newQuery(rightChild);
		}
		else if (name.equals("term"))
		{
			String  use       = xmlQuery.getAttributeValue("use");
			String  structure = xmlQuery.getAttributeValue("structure");
			String  relation  = xmlQuery.getAttributeValue("relation");
			String  text      = xmlQuery.getText();

			StringBuffer term = new StringBuffer();
			if (use       != null) term.append("@attr 1=" + use       + " ");
			if (structure != null) term.append("@attr 4=" + structure + " ");
			if (relation  != null) term.append("@attr 2=" + relation  + " ");
			boolean toQuote = !isAlpha(text);
			if (toQuote) term.append('"');
			term.append(text);
			if (toQuote) term.append('"');
			return term.toString();
		}
		else throw new Exception("unknown Z39.50 query type: " + name);
	}

	private boolean isAlpha(String text)
	{
		for (int i = 0; i < text.length(); i++)
			if (!Character.isLetter(text.charAt(i))) return false;
		return true;
	}

	private Element makeSummary()
	{
		Element summary = new Element("summary");
		summary.setAttribute("count", getSize()+"");
		summary.setAttribute("status", getStatus());
		summary.setAttribute("type", "remote");

		return summary;
	}

	/** returns the current status
	  */
	private String getStatus()
	{
		switch (_st.getTaskStatusCode())
		{
		case SearchTask.TASK_COMPLETE:  return "complete";
		case SearchTask.TASK_EXECUTING: return "executing";
		case SearchTask.TASK_FAILURE:   return "failure";
		case SearchTask.TASK_IDLE:      return "idle";
		}
		return null;
	}
}


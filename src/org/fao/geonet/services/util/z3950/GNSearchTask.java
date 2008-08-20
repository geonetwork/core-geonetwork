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

package org.fao.geonet.services.util.z3950;

import java.util.Enumeration;
import java.util.List;
import java.util.Observer;
import java.util.Properties;
import java.util.Stack;

import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Xml;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.DOMOutputter;

import com.k_int.IR.AsynchronousEnumeration;
import com.k_int.IR.DefaultSourceEnumeration;
import com.k_int.IR.IFSNotificationTarget;
import com.k_int.IR.IREvent;
import com.k_int.IR.IRQuery;
import com.k_int.IR.IRStatusReport;
import com.k_int.IR.InformationFragment;
import com.k_int.IR.InformationFragmentSource;
import com.k_int.IR.InvalidQueryException;
import com.k_int.IR.QueryModel;
import com.k_int.IR.RecordFormatSpecification;
import com.k_int.IR.SearchTask;
import com.k_int.IR.Syntaxes.DOMTree;
import com.k_int.util.RPNQueryRep.AttrPlusTermNode;
import com.k_int.util.RPNQueryRep.AttrTriple;
import com.k_int.util.RPNQueryRep.ComplexNode;
import com.k_int.util.RPNQueryRep.QueryNode;
import com.k_int.util.RPNQueryRep.QueryNodeVisitor;
import com.k_int.util.RPNQueryRep.RootNode;

//=============================================================================

public class GNSearchTask extends SearchTask implements InformationFragmentSource
{
	private static final String[] PRIVATE_STATUS_TYPES = { "Idle", "Searching", "Search complete", "Requesting records", "All records returned" };

	public int             _demoSearchStatus = 0;

	private int            _fragmentCount = 0;
	private IRQuery        _query = null;
	private GNSearchable   _source = null;
	private ServiceContext _srvContext;
	private Properties     _properties;

	private MetaSearcher   _searcher;

	//--------------------------------------------------------------------------

	public GNSearchTask(IRQuery query, GNSearchable source, Observer[] observers, Properties properties, ServiceContext srvContext)
	{
		super(observers);
		Log.debug(Geonet.Z3950_SERVER, "Creating search task");

		_query      = query;
		_source     = source;
		_srvContext = srvContext;
		_properties = properties;
		setTaskStatusCode(TASK_IDLE);
		Log.debug(Geonet.Z3950_SERVER, "Search task created");
	}

	//--------------------------------------------------------------------------

	public void destroy()
	{
		// TODO: check if empty method is correct
	}

	//--------------------------------------------------------------------------

	/* TODO: not used, remove
	/ * * from SearchTask abstract base class * /
	public void destroyTask()
	{
		super.destroyTask();
	}
	*/

	//--------------------------------------------------------------------------

	public int getPrivateTaskStatusCode()
	{
		return _demoSearchStatus;
	}

	//--------------------------------------------------------------------------

	public void setPrivateTaskStatusCode(int i)
	{
		_demoSearchStatus = i;
	}

	//--------------------------------------------------------------------------

	public String lookupPrivateStatusCode(int code)
	{
		return PRIVATE_STATUS_TYPES[code];
	}

	//--------------------------------------------------------------------------

	public int evaluate(int timeout)
	{
		try
		{
			Log.debug(Geonet.Z3950_SERVER, "INCOMING QUERY:\n" + _query.getQueryModel());

			RemoteQueryDecoder queryDecoder = new RemoteQueryDecoder(_query.getQueryModel());
			Element request = new Element("request");

			Log.debug(Geonet.Z3950_SERVER, "INCOMING XML QUERY:\n" + Xml.getString(queryDecoder.getQuery()));

			request.addContent(queryDecoder.getQuery());
			ServiceConfig config = new ServiceConfig();

			// possibly close old searcher
			if (_searcher != null) _searcher.close();

			// perform the search and save search results
			GeonetContext gc = (GeonetContext) _srvContext.getHandlerContext(Geonet.CONTEXT_NAME);
			SearchManager searchMan = gc.getSearchmanager();

			MetaSearcher s = searchMan.newSearcher(SearchManager.LUCENE, Geonet.File.SEARCH_Z3950_SERVER);
			s.search(_srvContext, request, config);
			_searcher = s;

			// System.out.println("summary:\n" + Xml.getString(s.getSummary())); // DEBUG

			// Random number of records.. Set up the result set
			setFragmentCount(s.getSize());
			setTaskStatusCode(TASK_COMPLETE);

			_srvContext.getResourceManager().close();
		}
		catch (Throwable e)
		{
			e.printStackTrace();

			try {_srvContext.getResourceManager().abort(); }
			catch (Exception e2) { e2.printStackTrace(); }
		}
		return(getTaskStatusCode());
	}

	//--------------------------------------------------------------------------

	public InformationFragment[] getFragment(int startingFragment,
														  int count,
														  RecordFormatSpecification spec)
	{
		Log.debug(Geonet.Z3950_SERVER, "Request for fragment start:"+startingFragment+", count:"+count);

		InformationFragment fragment[] = new InformationFragment[count];
		try
		{
			// build fragment data
			int from = startingFragment;
			int to   = startingFragment + count - 1;

			Element request = new Element("request");
			request.addContent(new Element("from").setText(from+""));
			request.addContent(new Element("to").setText(to+""));
			ServiceConfig config = new ServiceConfig();

			Log.debug(Geonet.Z3950_SERVER, "Search request:\n"+ Xml.getString(request));
			// get result set
			Element result = _searcher.present(_srvContext, request, config);

			Log.debug(Geonet.Z3950_SERVER, "Search result:\n"+ Xml.getString(result));

			// remove summary
			result.removeChildren("summary");
			List list = result.getChildren();

			Log.debug(Geonet.Z3950_SERVER, "Set name asked:"+ spec.getSetname());

			// save other records to fragment
			for(int i = 0; i < count; i++)
			{
				Element md = (Element)list.get(0);
				md.detach();

				Log.debug(Geonet.Z3950_SERVER, "Returning fragment:\n"+ Xml.getString(md));

				// add metadata
				fragment[i] = new DOMTree("geonetwork",
												  "geonetwork",
												  null,
												  getRecord(md),
												  new RecordFormatSpecification("xml", "meta", "f"));
			}
			_srvContext.getResourceManager().close();
			Log.debug(Geonet.Z3950_SERVER, "Fragment returned");
		}
		catch (Throwable e)
		{
			try {_srvContext.getResourceManager().abort(); }
			catch (Exception e2) { e2.printStackTrace(); }

			e.printStackTrace();
		}
		return fragment;
	}

	//--------------------------------------------------------------------------

	private org.w3c.dom.Document getRecord(Element metadata)
		throws Exception
	{
		DOMOutputter outputter = new DOMOutputter();
		return outputter.output(new Document(metadata));
	}

	//--------------------------------------------------------------------------

	public void asyncGetFragment(int startingFragment,
										  int count,
										  RecordFormatSpecification spec,
										  IFSNotificationTarget target)
	{
	}

	//--------------------------------------------------------------------------

	public void store(int id, InformationFragment fragment)
	{
	}

	//--------------------------------------------------------------------------

	public void setFragmentCount(int i)
	{
		_fragmentCount = i;
		IREvent e = new IREvent(IREvent.FRAGMENT_COUNT_CHANGE, new Integer(i));
		setChanged();
		notifyObservers(e);
	}

	//--------------------------------------------------------------------------

	public int getFragmentCount()
	{
		return _fragmentCount;
	}

	//--------------------------------------------------------------------------

	public InformationFragmentSource getTaskResultSet()
	{
		return this;
	}

	//--------------------------------------------------------------------------

	public AsynchronousEnumeration elements()
	{
		return new DefaultSourceEnumeration(this);
	}

	//--------------------------------------------------------------------------

	public IRStatusReport getStatusReport()
	{
		return new IRStatusReport("Demo",
		"Demo",
		"Demo",
		PRIVATE_STATUS_TYPES[_demoSearchStatus],
		getFragmentCount(),
		getFragmentCount(),
		null,
		getLastStatusMessages());
	}
}

//--------------------------------------------------------------------------
// converts an RPN query to xml
class RemoteQueryDecoder
{
	private Stack  stack = new Stack();
	private String attrSet;

	public RemoteQueryDecoder(QueryModel qm)
	{
		try
		{
			RootNode rn = qm.toRPN();
			QueryNodeVisitor qnv = new QueryNodeVisitor()
			{
				public void visit(AttrPlusTermNode aptn)
				{
					super.visit(aptn);
					Element node = new Element("term");
					for (Enumeration enu = aptn.getAttrEnum(); enu.hasMoreElements(); )
					{
						AttrTriple triple = (AttrTriple)enu.nextElement();
						int type = triple.getAttrType().intValue();
						String value = triple.getAttrVal().toString();
						node.setAttribute(getAttrAttribute(type, value));
					}
					node.addContent(aptn.getTermAsString(false));
					stack.push(node);
				}
				public void visit(ComplexNode cn)
				{
					super.visit(cn);
					Element rightChild = (Element)stack.pop();
					Element leftChild = (Element)stack.pop();
					Element node = new Element(getOpString(cn.getOp()));
					node.addContent(leftChild);
					node.addContent(rightChild);
					stack.push(node);
				}
				public void visit(QueryNode qn)
				{
					super.visit(qn);
				}
				public void visit(RootNode rn)
				{
					super.visit(rn);
					Element query = new Element("query");
					query.setAttribute("attrset", rn.getAttrset());
					query.addContent((Element)stack.pop());
					stack.push(query);
				}
			};
			qnv.visit(rn);
		}
		catch (InvalidQueryException iqe)
		{
			iqe.printStackTrace();
		}
	}

	public Element getQuery()
	{
		return (Element)stack.peek();
	}

	public String getAttrSet()
	{
		return attrSet;
	}

	public String toString()
	{
		return Xml.getString(getQuery());
	}

	private String getOpString(int op)
	{
		switch (op)
		{
		case ComplexNode.COMPLEX_AND:    return "and";
		case ComplexNode.COMPLEX_ANDNOT: return "not";
		case ComplexNode.COMPLEX_OR:     return "or";
		case ComplexNode.COMPLEX_PROX:   return "prox";
		default:                         return op+"";
		}
	}

	private Attribute getAttrAttribute(int attrType, String attrValue)
	{
		switch (attrType)
		{
		case 1:  return new Attribute("use",        attrValue);
		case 2:  return new Attribute("relation",   attrValue);
		case 4:  return new Attribute("structure",  attrValue);
		case 5:  return new Attribute("truncation", attrValue);
		default: return new Attribute(attrType+"",  attrValue);
		}
	}
}


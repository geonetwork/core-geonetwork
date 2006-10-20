//=============================================================================
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

package org.fao.geonet.csw.common.requests;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.exceptions.NoApplicableCodeEx;
import org.fao.geonet.csw.common.http.HttpException;
import org.fao.geonet.csw.common.http.HttpGetRequest;
import org.fao.geonet.csw.common.http.HttpPostRequest;
import org.fao.geonet.csw.common.http.HttpRequest;
import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

//=============================================================================

public abstract class CatalogRequest
{
	public enum Method { GET, POST }

	//---------------------------------------------------------------------------

	private String host;
	private int    port;
	private String address;
	private String loginAddr;
	private Method method;

	private HttpRequest request;

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public CatalogRequest()
	{
		this(null);
	}

	//---------------------------------------------------------------------------

	public CatalogRequest(String host)
	{
		this(host, 80);
	}

	//---------------------------------------------------------------------------

	public CatalogRequest(String host, int port)
	{
		this.host    = host;
		this.port    = port;

		setMethod(Method.POST);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getHost()         { return host;    }
	public int    getPort()         { return port;    }
	public String getAddress()      { return address; }
	public Method getMethod()       { return method;  }
	public String getResponseCode() { return request.getResponseCode(); }
	public String getResponseData() { return request.getResponseData(); }
	public String getStatusLine()   { return request.getStatusLine();   }
	public List   getSentData()     { return request.getSentData();     }

	//---------------------------------------------------------------------------

	public void setHost(String host)
	{
		this.host = host;
	}

	//---------------------------------------------------------------------------

	public void setPort(int port)
	{
		this.port = port;
	}

	//---------------------------------------------------------------------------

	public void setAddress(String address)
	{
		this.address = address;
	}

	//---------------------------------------------------------------------------

	public void setMethod(Method m)
	{
		method = m;

		if (method == Method.GET) 	request = new HttpGetRequest();
			else 							request = new HttpPostRequest();
	}

	//---------------------------------------------------------------------------

	public void setLoginAddress(String address)
	{
		loginAddr = address;
	}

	//---------------------------------------------------------------------------

	public void login(String username, String password) throws IOException, CatalogException,
																				  HttpException, Exception
	{
		request.clearParams();
		request.addParam("username", username);
		request.addParam("password", password);

		request.setHost(host);
		request.setPort(port);
		request.setAddress(loginAddr);

		Element response = null;

		try
		{
			response = request.execute();
		}
		catch (JDOMException e)
		{
			throw new NoApplicableCodeEx("Response is not in xml format :\n"+ request.getResponseData());
		}

		if (!response.getName().toLowerCase().equals("ok"))
			throw new NoApplicableCodeEx("Login denied :\n"+ request.getResponseData());
	}

	//---------------------------------------------------------------------------

	public Element execute() throws IOException, CatalogException, HttpException, Exception
	{
		request.clearParams();

		if (method == Method.GET)	setupGetParams (request);
			else 							setupPostParams(request);

		request.setHost(host);
		request.setPort(port);
		request.setAddress(address);

		Element response = null;

		try
		{
			response = request.execute();
		}
		catch (JDOMException e)
		{
			throw new NoApplicableCodeEx("Response is not in xml format :\n"+ request.getResponseData());
		}

		CatalogException.unmarshal(response);

		//------------------------------------------------------------------------
		//--- Start Hack
		//--- we need this hack because Jeeves does not handle the 403 and 404 error
		//--- codes

		if (response.getName().equals("error"))
		{
			String id = response.getAttributeValue("id");

			if (id == null)
				throw new NoApplicableCodeEx("Returned error with no 'id' :\n"+ Xml.getString(response));

			if (id.equals("error"))
				throw new NoApplicableCodeEx("Returned general error :\n"+ Xml.getString(response));

			if (id.equals("privileges"))
			{
				request.forceResponse("403");
				throw new HttpException("Service forbidden", request.getStatusLine(), request.getResponseCode());
			}

			throw new NoApplicableCodeEx("Unknown error 'id' :\n"+ Xml.getString(response));
		}

		//--- End Hack
		//------------------------------------------------------------------------

		return response;
	}

	//---------------------------------------------------------------------------
	//---
	//--- Abstract methods
	//---
	//---------------------------------------------------------------------------

	protected abstract String getRequestName();
	protected abstract void   setupGetParams (HttpRequest request);
	protected abstract void   setupPostParams(HttpRequest request);

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	//--- GET fill methods

	protected void fill(HttpRequest request, String param, Iterable iter)
	{
		fill(request, param, iter, "");
	}

	//---------------------------------------------------------------------------

	protected void fill(HttpRequest request, String param, Iterable iter, String prefix)
	{
		Iterator i = iter.iterator();

		if (!i.hasNext())
			return;

		StringBuffer sb = new StringBuffer();

		while(i.hasNext())
		{
			sb.append(prefix+i.next());

			if (i.hasNext())
				sb.append(",");
		}

		request.addParam(param, sb.toString());
	}

	//---------------------------------------------------------------------------
	//--- POST fill methods
	//---------------------------------------------------------------------------

	protected void fill(Element root, String parentName, String childName,
							  Iterable iter, Namespace ns)
	{
		Iterator i = iter.iterator();

		if (!i.hasNext())
			return;

		Element parent = new Element(parentName, ns);

		while(i.hasNext())
		{
			Element el = new Element(childName, ns);
			el.setText(i.next().toString());

			parent.addContent(el);
		}

		root.addContent(parent);
	}

	//---------------------------------------------------------------------------

	protected void fill(Element root, String childName, Iterable iter)
	{
		Iterator i = iter.iterator();

		if (!i.hasNext())
			return;

		while(i.hasNext())
		{
			Element el = new Element(childName, root.getNamespace());
			el.setText(i.next().toString());

			root.addContent(el);
		}
	}

	//---------------------------------------------------------------------------

	protected void addParam(Element root, String name, Object value)
	{
		addParam(root, name, value, Csw.NAMESPACE_CSW);
	}

	//---------------------------------------------------------------------------

	protected void addParam(Element root, String name, Object value, Namespace ns)
	{
		if (value != null)
			root.addContent(new Element(name, ns).setText(value.toString()));
	}

	//---------------------------------------------------------------------------
	//--- Attribute facilities
	//---------------------------------------------------------------------------

	protected void setAttrib(Element el, String name, Object value)
	{
		setAttrib(el, name, value, "");
	}

	//---------------------------------------------------------------------------

	protected void setAttrib(Element el, String name, Object value, String prefix)
	{
		if (value != null)
			el.setAttribute(name, prefix + value.toString());
	}

	//---------------------------------------------------------------------------

	protected void setAttrib(Element el, String name, Iterable iter, String prefix)
	{
		Iterator i = iter.iterator();

		if (!i.hasNext())
			return;

		StringBuffer sb = new StringBuffer();

		while(i.hasNext())
		{
			sb.append(prefix + i.next().toString());

			if (i.hasNext())
				sb.append(" ");
		}

		el.setAttribute(name, sb.toString());
	}
}

//=============================================================================


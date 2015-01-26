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

package org.fao.geonet.csw.common.requests;

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;
import org.jdom.Namespace;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.fao.geonet.utils.AbstractHttpRequest.Method;

//=============================================================================

public abstract class CatalogRequest {
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public CatalogRequest(ServiceContext context) { this(context, null); }

	//---------------------------------------------------------------------------

	public CatalogRequest(final ServiceContext context, final String host) { this(context, host, 80); }

    //---------------------------------------------------------------------------

    public CatalogRequest(final ServiceContext context, final String host, final int port) {
        this(context, host, port, "http");
    }

	//---------------------------------------------------------------------------

	public CatalogRequest(final ServiceContext context, final String host, final int port, final String protocol) {
        client = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(host, port, protocol);
		setMethod(Method.POST);
        Lib.net.setupProxy(context, client);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getHost()         { return client.getHost();         }
	public int    getPort()         { return client.getPort();         }
    public String getProtocol()     { return client.getProtocol();     }
	public String getAddress()      { return client.getAddress();      }
	public Method getMethod()       { return client.getMethod();       }
	public String getSentData()     { return client.getSentData();     }

	//---------------------------------------------------------------------------

	public String getOutputSchema() {
		return outputSchema;
	}

	public void setOutputSchema(String outputSchema) {
		this.outputSchema = outputSchema;
	}

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

	//---------------------------------------------------------------------------
	/**
	 * Set request URL host, port, address and path.
	 * If URL contains query string parameters, those parameters are
	 * preserved (and {@link CatalogRequest#excludedParameters} are
	 * excluded). A complete GetCapabilities URL may be used for initialization.
	 */
	public void setUrl(ServiceContext context, URL url)
	{
        client.setUrl(url);

        client.setQuery(null);
        client.clearParams();
        String query = url.getQuery();

		if (StringUtils.isNotEmpty(query)) {
			String[] params = query.split("&");
			for (String param : params) {
				String[] kvp = param.split("=");
				if (!excludedParameters.contains(kvp[0].toLowerCase())) {
					client.addParam(kvp[0], kvp[1]);
				}
			}
		}

        // Setup the proxy if applies, checking the url in the proxy ignore list
        Lib.net.setupProxy(context, client);
	}

	//---------------------------------------------------------------------------

	public void setMethod(Method m)
	{
		client.setMethod(m);
	}

    //---------------------------------------------------------------------------

    public Element execute() throws Exception {
        if (getMethod() == Method.GET) {
            client.clearParams();
            setupGetParams();
        } else {
            final Element postParams = getPostParams();
            client.setRequest(postParams);
        }
        Element response = client.execute();
        //--- raises an exception if the case
		CatalogException.unmarshal(response);

		return response;
	}

    //---------------------------------------------------------------------------

	public void setCredentials(String username, String password)
	{
		client.setCredentials(username, password);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Abstract methods
	//---
	//---------------------------------------------------------------------------

	protected abstract String  getRequestName();
	protected abstract void    setupGetParams();
	protected abstract Element getPostParams ();

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	//---------------------------------------------------------------------------
	//--- GET fill methods
	//---------------------------------------------------------------------------

	protected void fill(String param, Iterable<?> iter)
	{
		fill(param, iter, "");
	}

	//---------------------------------------------------------------------------

	protected void fill(String param, Iterable<?> iter, String prefix)
	{
		Iterator<?> i = iter.iterator();

		if (!i.hasNext())
			return;

		StringBuffer sb = new StringBuffer();

		while(i.hasNext())
		{
			sb.append(prefix);
			sb.append(i.next());

			if (i.hasNext())
				sb.append(',');
		}

		addParam(param, sb.toString());
	}

	//---------------------------------------------------------------------------
	//--- POST fill methods
	//---------------------------------------------------------------------------

	protected void fill(Element root, String parentName, String childName,
							  Iterable<?> iter, Namespace ns)
	{
		Iterator<?> i = iter.iterator();

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

	protected void fill(Element root, String childName, Iterable<?> iter)
	{
		Iterator<?> i = iter.iterator();

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

	protected void setAttrib(Element el, String name, Iterable<?> iter, String prefix)
	{
		Iterator<?> i = iter.iterator();

		if (!i.hasNext())
			return;

		StringBuffer sb = new StringBuffer();

		while(i.hasNext())
		{
			sb.append(prefix);
			sb.append(i.next().toString());

			if (i.hasNext())
				sb.append(" ");
		}

		el.setAttribute(name, sb.toString());
	}

    	//---------------------------------------------------------------------------

	protected void setAttribComma(Element el, String name, Iterable<?> iter, String prefix)
	{
		Iterator<?> i = iter.iterator();

		if (!i.hasNext())
			return;

		StringBuffer sb = new StringBuffer();

		while(i.hasNext())
		{
            Object value =  i.next();

            sb.append(prefix);
            sb.append(value.toString());

			if (i.hasNext())
				sb.append(',');
		}

		el.setAttribute(name, sb.toString());
	}
	//--------------------------------------------------------------------------
	//--- Parameters facilities (POST)
	//---------------------------------------------------------------------------

	protected void addParam(Element root, String name, Object value)
	{
		if (value != null)
			root.addContent(new Element(name, Csw.NAMESPACE_CSW).setText(value.toString()));
	}

	//---------------------------------------------------------------------------
	//--- Parameters facilities (GET)
	//--------------------------------------------------------------------------

	protected void addParam(String name, Object value)
	{
		addParam(name, value, "");
	}

	//--------------------------------------------------------------------------

	protected void addParam(String name, Object value, String prefix)
	{
        if (value != null) {
            client.addParam(name, prefix + value.toString());
        }
	}

	protected String outputSchema;

    protected String serverVersion = Csw.CSW_VERSION;  // Sets default value

	private final XmlRequest client;

    // Parameters to not take into account in GetRequest
	private static final List<String> excludedParameters = Arrays.asList("", "request", "version", "service");
}

//=============================================================================


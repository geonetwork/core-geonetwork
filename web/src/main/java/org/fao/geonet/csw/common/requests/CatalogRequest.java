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
import jeeves.utils.Log;
import jeeves.utils.Util;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.Csw;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.csw.common.util.Xml;
import org.fao.geonet.lib.Lib;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

//=============================================================================

public abstract class CatalogRequest
{
	public enum Method { GET, POST }

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

    public CatalogRequest() { this(null, null); }

	public CatalogRequest(ServiceContext context) { this(context, null); }

	//---------------------------------------------------------------------------

	public CatalogRequest(ServiceContext context, String host) { this(context, host, 80); }

    //---------------------------------------------------------------------------

    public CatalogRequest(ServiceContext context, String host, int port)
    {
        this(context, host, port, "http");
    }

	//---------------------------------------------------------------------------

	public CatalogRequest(ServiceContext context, String host, int port, String protocol)
	{
		this.host    = host;
		this.port    = port;
        this.protocol= protocol;

		setMethod(Method.POST);
        Cookie cookie = new Cookie();
        HttpState state = new HttpState();
        state.addCookie(cookie);
		client.setState(state);
		client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

        if (context != null) Lib.net.setupProxy(context, client);
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public String getHost()         { return host;         }
	public int    getPort()         { return port;         }
    public String getProtocol()     { return protocol;     }
	public String getAddress()      { return address;      }
	public Method getMethod()       { return method;       }
	public String getSentData()     { return sentData;     }
	public String getReceivedData() { return receivedData; }

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

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

	//---------------------------------------------------------------------------

	public void setAddress(String address)
	{
		this.address = address;
	}

	//---------------------------------------------------------------------------
	/**
	 * Set request URL host, port, address and path.
	 * If URL contains query string parameters, those parameters are
	 * preserved (and {@link CatalogRequest#excludedParameters} are
	 * excluded). A complete GetCapabilities URL may be used for initialization.
	 */
	public void setUrl(URL url)
	{
		this.host    = url.getHost();
		this.port    = url.getPort();
        this.protocol= url.getProtocol();
		this.address = url.toString();
		this.path = url.getPath();
		
		alSetupGetParams = new ArrayList<NameValuePair>();
		String query = url.getQuery();

		if (StringUtils.isNotEmpty(query)) {
			String[] params = query.split("&");
			for (String param : params) {
				String[] kvp = param.split("=");
				if (!excludedParameters.contains(kvp[0].toLowerCase())) {
					this.alSetupGetParams.add(new NameValuePair(kvp[0], kvp[1]));
				}
			}
		}

		if (this.port == -1) {
			this.port = url.getDefaultPort();
		}
	}

	//---------------------------------------------------------------------------

	public void setMethod(Method m)
	{
		method = m;
	}

	//---------------------------------------------------------------------------

	public void setLoginAddress(String address)
	{
		loginAddr = address;
	}

	//---------------------------------------------------------------------------

	public void setUseSOAP(boolean yesno)
	{
		useSOAP = yesno;
	}

	//---------------------------------------------------------------------------

    //---------------------------------------------------------------------------

    public Element execute() throws Exception
	{
		HttpMethodBase httpMethod = setupHttpMethod();

		Element response = doExecute(httpMethod);

		if (useSOAP)
			response = soapUnembed(response);

		//--- raises an exception if the case
		CatalogException.unmarshal(response);

		return response;
	}

    public boolean login(String username, String password) throws Exception

    {
        Element request = new Element("request")
                .addContent(new Element("username").setText(username))
                .addContent(new Element("password").setText(password));

        PostMethod post = new PostMethod();

        postData = Xml.getString(new Document(request));

        post.setRequestEntity(new StringRequestEntity(postData, "application/xml", "UTF8"));
//		post.setFollowRedirects(true);
        post.setPath(loginAddr);

        Element response = doExecute(post);

        if (Csw.NAMESPACE_ENV.getURI().equals(response.getNamespace().getURI()))
            response = soapUnembed(response);

        return response.getName().equals("ok");
    }

    //---------------------------------------------------------------------------

	public void setCredentials(String username, String password)
	{
		this.useAuthent = true;
		this.username   = username;
		this.password   = password;
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

	protected void fill(String param, Iterable iter)
	{
		fill(param, iter, "");
	}

	//---------------------------------------------------------------------------

	protected void fill(String param, Iterable iter, String prefix)
	{
		Iterator i = iter.iterator();

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
			sb.append(prefix);
			sb.append(i.next().toString());

			if (i.hasNext())
				sb.append(" ");
		}

		el.setAttribute(name, sb.toString());
	}

    	//---------------------------------------------------------------------------

	protected void setAttribComma(Element el, String name, Iterable iter, String prefix)
	{
		Iterator i = iter.iterator();

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
		if (value != null)
			alGetParams.add(new NameValuePair(name, prefix+value.toString()));
	}

	//---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	private Element doExecute(HttpMethodBase httpMethod) throws IOException, JDOMException
	{
		client.getHostConfiguration().setHost(host, port, protocol);

		byte[] data = null;

		try
		{
			client.executeMethod(httpMethod);
			
			///data = httpMethod.getResponseBody();
			// If server return HTTP Error 500 Server error 
			// when retrieving the data return null
			if (httpMethod.getStatusCode() == 500) {
				System.out.println("  Status code: " + httpMethod.getStatusCode());
				return null;
			} else {
				return Xml.loadStream(httpMethod.getResponseBodyAsStream());
			}
		}
		finally
		{
			httpMethod.releaseConnection();
			try {
			setupSentData(httpMethod);
			setupReceivedData(httpMethod, data);
			} catch (Throwable e) {
			    Log.warning(Geonet.HARVESTER, "Exception was raised during cleanup of a CSW request : "+ Util.getStackTrace(e));
			}
		}
	}

	//---------------------------------------------------------------------------

	private HttpMethodBase setupHttpMethod() throws UnsupportedEncodingException
	{
		HttpMethodBase httpMethod;

		if (method == Method.GET)
		{
			alGetParams = new ArrayList<NameValuePair>();
			
			if (alSetupGetParams.size() != 0) {
				alGetParams.addAll(alSetupGetParams);
			}
			
			setupGetParams();
			httpMethod = new GetMethod();
			httpMethod.setPath(path);
			httpMethod.setQueryString(alGetParams.toArray(new NameValuePair[1]));
			System.out.println("GET params:"+httpMethod.getQueryString());
			if (useSOAP)
				httpMethod.addRequestHeader("Accept", "application/soap+xml");
		}
		else
		{
			Element    params = getPostParams();
			PostMethod post   = new PostMethod();
			if (!useSOAP)
			{
				postData = Xml.getString(new Document(params));
				post.setRequestEntity(new StringRequestEntity(postData, "application/xml", "UTF8"));
			}
			else
			{
				postData = Xml.getString(new Document(soapEmbed(params)));
				post.setRequestEntity(new StringRequestEntity(postData, "application/soap+xml", "UTF8"));
			}
			System.out.println("POST params:"+Xml.getString(params));
			httpMethod = post;
			httpMethod.setPath(path);
		}
 
//		httpMethod.setFollowRedirects(true);
		
		if (useAuthent)
		{
			Credentials cred = new UsernamePasswordCredentials(username, password);
			AuthScope   scope= new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);

			client.getState().setCredentials(scope, cred);
			httpMethod.setDoAuthentication(true);
		}

		return httpMethod;
	}

	//---------------------------------------------------------------------------

	private void setupSentData(HttpMethodBase httpMethod)
	{
		sentData = httpMethod.getName() +" "+ httpMethod.getPath();

		if (httpMethod.getQueryString() != null)
			sentData += "?"+ httpMethod.getQueryString();

		sentData += "\r\n";

		for (Header h : httpMethod.getRequestHeaders())
			sentData += h;

		sentData += "\r\n";

		if (httpMethod instanceof PostMethod)
			sentData += postData;
	}

	//---------------------------------------------------------------------------

	private void setupReceivedData(HttpMethodBase httpMethod, byte[] response)
	{
		receivedData = httpMethod.getStatusText() +"\r\r";

		for (Header h : httpMethod.getResponseHeaders())
			receivedData += h;

		receivedData += "\r\n";

		try
		{
			if (response != null)
				receivedData += new String(response, "UTF8");
		}
		catch (UnsupportedEncodingException e) {
            // TODO what's this ?
        }
	}

	//---------------------------------------------------------------------------

	private Element soapEmbed(Element elem)
	{
		Element envl = new Element("Envelope", Csw.NAMESPACE_ENV);
		Element body = new Element("Body",     Csw.NAMESPACE_ENV);

		envl.addContent(body);
		body.addContent(elem);

		return envl;
	}

	//---------------------------------------------------------------------------

	private Element soapUnembed(Element envelope) throws Exception
	{
		Namespace ns   = envelope.getNamespace();
		Element   body = envelope.getChild("Body", ns);

		if (body == null)
			throw new Exception("Bad SOAP response");

		List list = body.getChildren();

		if (list.size() == 0)
			throw new Exception("Bas SOAP response");

		return (Element) list.get(0);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	private String  host;
	private int     port;
    private String  protocol;
	private String  address;
	private String  path;
	private String  loginAddr;
	private Method  method;
	private boolean useSOAP;
	private boolean useAuthent;
	private String  username;
	private String  password;
	protected String outputSchema;

    protected String serverVersion = Csw.CSW_VERSION;  // Sets default value

	private HttpClient client = new HttpClient();

    private ArrayList<NameValuePair> alGetParams;
    private ArrayList<NameValuePair> alSetupGetParams;

    // Parameters to not take into account in GetRequest
	private static final List<String> excludedParameters = Arrays.asList("", "request", "version", "service");
	
	//--- transient vars

	private String sentData;
	private String receivedData;
	private String postData;
}

//=============================================================================


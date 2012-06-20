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

package org.fao.geonet.services.monitoring.services;

import jeeves.exceptions.BadXmlResponseEx;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * JSON request wrapper.
 */
public class JSONRequest
{
	public JSONRequest(String urlStr) throws MalformedURLException
	{
		url = new URL(urlStr);
	}

	/**
	 * Sends a request and obtains an json response.
	 */
	public JSONObject execute() throws IOException, BadXmlResponseEx
	{
		HttpMethodBase httpMethod = createHttpMethod();

		config.setHost(url.getHost(), url.getPort());

		if (useProxy)
		{
			config.setProxy(proxyHost, proxyPort);
		}

		client.setHostConfiguration(config);

		JSONObject responseDoc = null;
		try
		{
			statusCode = client.executeMethod(httpMethod);

		    responseDoc = HTTP.toJSONObject(httpMethod.getResponseBodyAsString());
		}
		catch (JSONException e)
		{
			throw new BadXmlResponseEx("Parse error: " +  e.getMessage());
		}
		finally
		{
			httpMethod.releaseConnection();
		}

		return responseDoc;
	}

	public String getHost()
	{
		return this.url.getHost();
	}

    //---------------------------------------------------------------------------

	public int getStatusCode()
	{
		return statusCode;
	}

	//---------------------------------------------------------------------------

	private HttpMethodBase createHttpMethod() throws UnsupportedEncodingException
	{
		GetMethod getMethod = new GetMethod();

		getMethod.setPath(url.getPath());
		getMethod.setDoAuthentication(false);

		return getMethod;
	}

	//---------------------------------------------------------------------------

	public void setUseProxy(boolean yesno)
	{
		useProxy = yesno;
	}

	//---------------------------------------------------------------------------

	public void setProxyHost(String host)
	{
		proxyHost = host;
	}

	//---------------------------------------------------------------------------

	public void setProxyPort(int port)
	{
		proxyPort = port;
	}

	//---------------------------------------------------------------------------

	public void setProxyCredentials(String username, String password)
	{
		if (username == null || username.trim().length() == 0)
		{
			return;
		}

		Credentials cred = new UsernamePasswordCredentials(username, password);
		AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);

		client.getState().setProxyCredentials(scope, cred);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------
	private HttpClient client = new HttpClient();
	private HostConfiguration config = new HostConfiguration();
	private URL url;
	private boolean useProxy;
	private String proxyHost;
	private int proxyPort;

    private int statusCode;
}

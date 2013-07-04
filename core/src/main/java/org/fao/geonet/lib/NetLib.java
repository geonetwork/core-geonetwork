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

package org.fao.geonet.lib;

import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.XmlRequest;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.oaipmh.requests.Transport;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//=============================================================================

public class NetLib
{
	public static final String ENABLED  = "system/proxy/use";
	public static final String HOST     = "system/proxy/host";
	public static final String PORT     = "system/proxy/port";
	public static final String USERNAME = "system/proxy/username";
	public static final String PASSWORD = "system/proxy/password";

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void setupProxy(ServiceContext context, XmlRequest req)
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getBean(SettingManager.class);

		setupProxy(sm, req);
	}

	//---------------------------------------------------------------------------
	/** Setup proxy for XmlRequest
	  */

	public void setupProxy(SettingManager sm, XmlRequest req)
	{
		boolean enabled = sm.getValueAsBool(ENABLED, false);
		String  host    = sm.getValue(HOST);
		String  port    = sm.getValue(PORT);
		String  username= sm.getValue(USERNAME);
		String  password= sm.getValue(PASSWORD);

		if (!enabled) {
			req.setUseProxy(false);
		} else {
			if (!Lib.type.isInteger(port))
				Log.error(Geonet.GEONETWORK, "Proxy port is not an integer : "+ port);
			else
			{
				req.setUseProxy(true);
				req.setProxyHost(host);
				req.setProxyPort(Integer.parseInt(port));
				if (username.trim().length()!=0) {
					req.setProxyCredentials(username, password);
				} 
			}
		}
	}

	//---------------------------------------------------------------------------

	public void setupProxy(ServiceContext context, HttpClient client)
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getBean(SettingManager.class);

		setupProxy(sm, client);
	}

	//---------------------------------------------------------------------------

	/** Setup proxy for http client
	  */
	public void setupProxy(SettingManager sm, HttpClient client)
	{
		boolean enabled = sm.getValueAsBool(ENABLED, false);
		String  host    = sm.getValue(HOST);
		String  port    = sm.getValue(PORT);
		String  username= sm.getValue(USERNAME);
		String  password= sm.getValue(PASSWORD);

		if (enabled) {
			if (!Lib.type.isInteger(port)) {
				Log.error(Geonet.GEONETWORK, "Proxy port is not an integer : "+ port);
			} else {
				HostConfiguration config = client.getHostConfiguration();
				if (config == null) config = new HostConfiguration();
				config.setProxy(host,Integer.parseInt(port));
				client.setHostConfiguration(config);

				if (username.trim().length()!=0) {
					Credentials cred = new UsernamePasswordCredentials(username, password);
					AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);

					client.getState().setProxyCredentials(scope, cred);
				}
				List<String> authPrefs = new ArrayList<String>(2);
				authPrefs.add(AuthPolicy.DIGEST);
				authPrefs.add(AuthPolicy.BASIC);
				// This will exclude the NTLM authentication scheme
				client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, authPrefs);
			}
		}
	}

	//---------------------------------------------------------------------------

	public void setupProxy(ServiceContext context, Transport t)
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getBean(SettingManager.class);

		setupProxy(sm, t);
	}

	//---------------------------------------------------------------------------

	private void setupProxy(SettingManager sm, Transport t) {
		boolean enabled = sm.getValueAsBool(ENABLED, false);
		String  host    = sm.getValue(HOST);
		String  port    = sm.getValue(PORT);
		String  username= sm.getValue(USERNAME);
		String  password= sm.getValue(PASSWORD);
		if (enabled) {
			if (!Lib.type.isInteger(port)) {
				Log.error(Geonet.GEONETWORK, "Proxy port is not an integer : "+ port);
			} else {
				t.setUseProxy(enabled);
				t.setProxyHost(host);
				t.setProxyPort(Integer.parseInt(port));
				if (username.trim().length() != 0) {
					t.setProxyCredentials(username, password);	
				}
			}
		}
	}

	//---------------------------------------------------------------------------

	public void setupProxy(ServiceContext context)
	{
		GeonetContext  gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		SettingManager sm = gc.getBean(SettingManager.class);

		setupProxy(sm);
	}

	//---------------------------------------------------------------------------

	/** Setup proxy for http client
	  */
	public void setupProxy(SettingManager sm)
	{
		String  host    = sm.getValue(HOST);
		String  port    = sm.getValue(PORT);
		String  username= sm.getValue(USERNAME);

		Properties props = System.getProperties();
		props.put("http.proxyHost", host);
		props.put("http.proxyPort", port);
		if (username.trim().length() > 0) {
			Log.error(Geonet.GEONETWORK, "Proxy credentials cannot be used");
		}

	}

	//---------------------------------------------------------------------------

	public boolean isUrlValid(String url)
	{
		try {
			new URL(url);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}
}

//=============================================================================


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
package org.fao.geonet.test;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import jeeves.utils.JeevesSAXBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Provide API for communicating with a remote HTTP server.
 * <p/>
 * This ServerMediator provides interaction with a remote HTTP webservice provider.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public class HTTPMediator extends ServerMediator {
	private String serverURL;

	/**
	 * Dispatch to server using HTTP request.
	 *
	 * @param aRequest a server request
	 * @return a server response element
	 */
	protected Element doDispatch(Element aRequest) {
		return null;
	}

	/**
	 * Initializes McKOI DB and local jeeves engine .
	 */
	protected void doInit() throws Exception {
		serverURL = getConfigParam("baseurl");
	}

	protected boolean isRunning() {
		return true;
	}


//=============================================================================

	private static class HTTPRequest {

		//---------------------------------------------------------------------------
		//---
		//--- Variables
		//---
		//---------------------------------------------------------------------------

		private String host;
		private int port;
		private String address;
		private Method method;

		private HttpClient client = new HttpClient();
		private HttpState state = new HttpState();
		private Cookie cookie = new Cookie();

		private ArrayList<NameValuePair> alGetParams;

		//--- transient vars

		private String sentData;
		private String receivedData;
		private String postData;

		public enum Method {
			GET, POST
		}

		//---------------------------------------------------------------------------
		//---
		//--- Constructor
		//---
		//---------------------------------------------------------------------------

		public HTTPRequest() {
			this(null);
		}

		//---------------------------------------------------------------------------

		public HTTPRequest(String host) {
			this(host, 80);
		}

		//---------------------------------------------------------------------------

		public HTTPRequest(String host, int port) {
			this.host = host;
			this.port = port;

			setMethod(Method.POST);
			state.addCookie(cookie);
			client.setState(state);
			client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		}

		//---------------------------------------------------------------------------
		//---
		//--- API methods
		//---
		//---------------------------------------------------------------------------

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

		public String getAddress() {
			return address;
		}

		public Method getMethod() {
			return method;
		}

		public String getSentData() {
			return sentData;
		}

		public String getReceivedData() {
			return receivedData;
		}

		public void setHost(String host) {
			this.host = host;
		}

		//---------------------------------------------------------------------------

		public void setPort(int port) {
			this.port = port;
		}

		//---------------------------------------------------------------------------

		public void setAddress(String address) {
			this.address = address;
		}

		//---------------------------------------------------------------------------

		public void setUrl(URL url) {
			this.host = url.getHost();
			this.port = url.getPort();
			this.address = url.getPath();

			if (this.port == -1)
				this.port = 80;
		}

		//---------------------------------------------------------------------------

		public void setMethod(Method m) {
			method = m;
		}


		//---------------------------------------------------------------------------

		public Element execute() throws Exception {
			HttpMethodBase httpMethod = setupHttpMethod();

			client.getHostConfiguration().setHost(host, port, "http");

			byte[] data = null;

			Element response = null;
			try {
				client.executeMethod(httpMethod);

				///data = httpMethod.getResponseBody();
				SAXBuilder builder = new JeevesSAXBuilder();
				builder.setFeature("http://apache.org/xml/features/allow-java-encodings", true);
				Document jdoc = builder.build(httpMethod.getResponseBodyAsStream());

				response = (Element) jdoc.getRootElement().detach();
			}
			finally {
				httpMethod.releaseConnection();

				setupSentData(httpMethod);
				setupReceivedData(httpMethod, data);
			}


			return response;
		}


		private Element getPostParams()
		{
			// TODO implement
			return null;
		}

		private void setupGetParams()
		{
			// TODO implement
		}

		private HttpMethodBase setupHttpMethod() throws UnsupportedEncodingException {
			HttpMethodBase httpMethod;

			if (method == Method.GET) {
				alGetParams = new ArrayList<NameValuePair>();
				setupGetParams();
				httpMethod = new GetMethod();
				httpMethod.setQueryString(alGetParams.toArray(new NameValuePair[1]));
				System.out.println("GET params:" + httpMethod.getQueryString());
			} else {
				Element params = getPostParams();
				PostMethod post = new PostMethod();
				postData = Xml.getString(new Document(params));
				post.setRequestEntity(new StringRequestEntity(postData, "application/xml", "UTF8"));
				System.out.println("POST params:" + Xml.getString(params));
				httpMethod = post;
			}

			return httpMethod;
		}

		//---------------------------------------------------------------------------

		private void setupSentData(HttpMethodBase httpMethod) {
			sentData = httpMethod.getName() + " " + httpMethod.getPath();

			if (httpMethod.getQueryString() != null)
				sentData += "?" + httpMethod.getQueryString();

			sentData += "\r\n";

			for (Header h : httpMethod.getRequestHeaders())
				sentData += h;

			sentData += "\r\n";

			if (httpMethod instanceof PostMethod)
				sentData += postData;
		}

		//---------------------------------------------------------------------------

		private void setupReceivedData(HttpMethodBase httpMethod, byte[] response) {
			receivedData = httpMethod.getStatusText() + "\r\r";

			for (Header h : httpMethod.getResponseHeaders())
				receivedData += h;

			receivedData += "\r\n";

			try {
				if (response != null)
					receivedData += new String(response, "UTF8");
			}
			catch (UnsupportedEncodingException e) {
			}
		}

	}
}

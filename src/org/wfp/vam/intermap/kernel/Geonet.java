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

// JUST A TEST CLASS!!!

package org.wfp.vam.intermap.kernel;

import java.io.*;
import java.net.*;

import org.jdom.*;

import org.wfp.vam.intermap.http.ConcurrentHTTPTransactionHandler;
import org.wfp.vam.intermap.http.cache.HttpGetFileCache;
import jeeves.utils.Xml;
import java.util.Hashtable;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;

public class Geonet {

	public static Element getGeonetRecords(double minx, double miny, double maxx, double maxy, int from, int to)
		throws MalformedURLException, IOException, Exception
	{
		// Get initial state object
		HttpClient httpclient = new HttpClient();
		httpclient.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
		//HostConfiguration hConf = httpclient.getHostConfiguration(); // DEBUG
		//hConf.setProxy("10.11.40.110", 8080); // DEBUG

		// portal.search request
		String request = "http://www.fao.org/geonetwork/srv/en/portal.search?extended=on&remote=off&region=0000&selregion=%3B180%3B-180%3B-90%3B90&relation=overlaps&any=&title=&abstract=&themekey=&radfrom=&siteId=&category=&hitsPerPage=10"
			+ "&northBL=" + maxy + "&westBL=" + minx + "&eastBL=" + maxx + "&southBL=" + miny;
		request += "&from=&to=";

		System.out.println("request: " + request);

		HttpMethod method = new GetMethod(request);

		int statusCode = httpclient.executeMethod(method);
		if (statusCode == HttpStatus.SC_OK)
			method.releaseConnection();
		else
			throw new Exception();

		// portal.present request
		request = "http://www.fao.org/geonetwork/srv/en/portal.present?siteId=&hitsPerPage=10";
		if (from != -1 && to != -1)
			request += "&from=" +  from + "&to=" + to;
		else
			request += "&from=&to=";

		method = new GetMethod(request);
		statusCode = httpclient.executeMethod(method);
		if (statusCode == HttpStatus.SC_OK)
		{
			Element xml = Xml.loadStream(method.getResponseBodyAsStream());
			method.releaseConnection();
			System.out.println(Xml.getString(xml)); // DEBUG
			return xml;
		}
		else
			throw new Exception();

	}

}


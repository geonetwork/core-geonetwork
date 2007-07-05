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
	
	public static Element getGeonetRecords(float minx, float miny, float maxx, float maxy, int from, int to)
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


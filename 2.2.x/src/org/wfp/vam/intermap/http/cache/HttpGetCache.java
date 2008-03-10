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

package org.wfp.vam.intermap.http.cache;

import java.io.*;
import java.util.*;
import org.apache.commons.httpclient.Header;

public class HttpGetCache implements HttpCache {
	
	/**
	 * Method put
	 *
	 * @param    uri                 a  String
	 * @param    response            an InputStream
	 * @param    headers             a  Header[]
	 *
	 * @exception   IOException
	 *
	 */
	public void put(String uri, InputStream response, Header[] headers) throws IOException {
		// TODO
	}
	
	
	/**
	 * Method getHeaderValue
	 *
	 * @param    uri                 a  String
	 * @param    header              a  String
	 *
	 * @return   a String
	 *
	 */
	public String getHeaderValue(String uri, String header) {
		// TODO
		return null;
	}
	
		
	private HashMap uris = new HashMap();
	private HashMap cachedTime = new HashMap();
	
	public void put(String uri, byte[] response, Header[] headers) {
		uris.put(uri, response);
		cachedTime.put(uri, Calendar.getInstance());
	}
	
	public byte[] get(String uri) {
		return (byte[])uris.get(uri);
	}
	
	public Calendar getCachedTime(String uri) {
		return (Calendar)cachedTime.get(uri);
	}
	
	public void clear() {
		uris.clear();
		cachedTime.clear();
	}
	
}


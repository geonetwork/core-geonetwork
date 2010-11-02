//=============================================================================
//===	Copyright (C) 2010 Food and Agriculture Organization of the
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
package org.fao.geonet.services.publisher;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

/**
 * Instances of this class are factories for creating http clients.
 * 
 * @author Éric Lemoine, Camptocamp France SAS
 */
public class HttpClientFactory {
	final String username;
	final String password;

	/**
	 * Constructs an HTTP client factory.
	 * 
	 * @param username
	 *            the username to set in the HTTP credentials
	 * @param password
	 *            the password to set in the HTTP credentials
	 */
	public HttpClientFactory(String username, String password) {
		this.username = username;
		this.password = password;
	}

	/**
	 * Returns an HTTP client whose credentials include the username and
	 * password passed to the factory constructor.
	 * 
	 * @return the HTTP client.
	 */
	public HttpClient newHttpClient() {
		final HttpClient c = new HttpClient();
		c.getState().setCredentials(
				new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
				new UsernamePasswordCredentials(username, password));
		return c;
	}
}

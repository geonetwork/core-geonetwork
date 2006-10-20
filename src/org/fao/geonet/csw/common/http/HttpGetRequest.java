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

package org.fao.geonet.csw.common.http;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import org.jdom.Element;

//=============================================================================

public class HttpGetRequest extends HttpRequest
{
	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public HttpGetRequest() {}

	//---------------------------------------------------------------------------

	public HttpGetRequest(String host)
	{
		super(host);
	}

	//---------------------------------------------------------------------------

	public HttpGetRequest(String host, int port)
	{
		super(host, port);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//---------------------------------------------------------------------------

	protected String getMethod() { return "GET"; }

	//---------------------------------------------------------------------------

	protected String getStartLineParams() throws UnsupportedEncodingException
	{
		List params = getParams().getChildren();

		if (params.isEmpty())
			return "";

		StringBuffer sb = new StringBuffer("?");

		for(Iterator i=params.iterator(); i.hasNext();)
		{
			Element param = (Element) i.next();

			sb.append(URLEncoder.encode(param.getName(), "ISO-8859-1"));
			sb.append("=");
			sb.append(URLEncoder.encode(param.getText(), "ISO-8859-1"));

			if (i.hasNext())
				sb.append("&");
		}

		return sb.toString();
	}

	//---------------------------------------------------------------------------

	protected void sendExtraHeaders(OutputStream os) {}
	protected void sendContent     (OutputStream os) {}
}

//=============================================================================


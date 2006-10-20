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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

//=============================================================================

public abstract class HttpRequest
{
	private String   host;
	private int      port;
	private String   address;
	private String   cookie;
	private Element  params;
	private String   responseCode;
	private String   responseData;
	private String   statusLine;

	private ArrayList alSentData = new ArrayList();

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public HttpRequest()
	{
		this(null);
	}

	//---------------------------------------------------------------------------

	public HttpRequest(String host)
	{
		this(host, 80);
	}

	//---------------------------------------------------------------------------

	public HttpRequest(String host, int port)
	{
		this(host, port, null);
	}

	//---------------------------------------------------------------------------

	public HttpRequest(String host, int port, String address)
	{
		this.host    = host;
		this.port    = port;
		this.address = address;

		clearParams();
	}

	//---------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//---------------------------------------------------------------------------

	public void clearParams()
	{
		params = new Element("REQUEST");
	}

	//--------------------------------------------------------------------------

	public void addParam(String name, Object value)
	{
		addParam(name, value, "");
	}

	//--------------------------------------------------------------------------

	public void addParam(String name, Object value, String prefix)
	{
		if (value != null)
			params.addContent(new Element(name).setText(prefix+value.toString()));
	}

	//--------------------------------------------------------------------------

	public void addParam(Element param)
	{
		params.addContent((Element) param.detach());
	}
	//--------------------------------------------------------------------------

	public void setParams(Element params)
	{
		this.params = (Element) params.detach();
	}

	//--------------------------------------------------------------------------

	public Element   getParams()       { return params;       }
	public String    getHost()         { return host;         }
	public String    getAddress()      { return address;      }
	public String    getResponseCode() { return responseCode; }
	public String    getResponseData() { return responseData; }
	public String    getStatusLine()   { return statusLine;   }
	public ArrayList getSentData()     { return alSentData;   }

	//---------------------------------------------------------------------------

	public void setHost   (String  host)    { this.host    = host;    }
	public void setAddress(String  address) { this.address = address; }
	public void setPort   (int     port)    { this.port    = port;    }

	//---------------------------------------------------------------------------

	public void forceResponse(String code)
	{
		responseCode = code;
		responseData = null;
	}

	//---------------------------------------------------------------------------
	/** @exception IOException if an I/O error occurs
	  * @exception JDOMException if the result is not in XML form
	  * @return    The response content body in XML form or NULL if the response
	  *            code was <> "200".
	  */

	public Element execute() throws IOException, JDOMException, HttpException
	{
		Socket socket = null;

		try
		{
			socket = new Socket(host, port);

			InputStream  is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();

			sendRequest(os);
			getResponse(is);

			return (Element) Xml.loadString(responseData, false).detach();
		}
		finally
		{
			if (socket != null)
				socket.close();
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private void sendRequest(OutputStream os) throws IOException
	{
		alSentData.clear();

		send(os, getMethod() +" "+ address + getStartLineParams() +" HTTP/1.1");
		send(os, "Host: "+host+":"+port);
		send(os, "Connection: close");
		send(os, "Accept: application/xml");
		send(os, "Accept-Charset: utf-8");
		send(os, "Cache-Control: no-cache");
		send(os, "User-Agent: GeoNetwork-csw-client/2.0.1");

		if (cookie != null)
			send(os, "Cookie: "+cookie);

		sendExtraHeaders(os);
		send(os, "");
		sendContent(os);

		os.flush();
	}

	//--------------------------------------------------------------------------

	protected void send(OutputStream os, String text) throws IOException
	{
		os.write(text.getBytes("ISO-8859-1"));
		os.write("\r\n".getBytes("ISO-8859-1"));

		alSentData.add(text);
	}

	//--------------------------------------------------------------------------

	protected void send(OutputStream os, byte[] data) throws IOException
	{
		os.write(data);
		alSentData.add(data);
	}

	//--------------------------------------------------------------------------

	private void getResponse(InputStream is) throws IOException, HttpException
	{
		responseCode = "???";
		responseData = null;

		BufferedReader input = new BufferedReader(new InputStreamReader(is, "UTF-8"));

		//--- read the status response

		String line = input.readLine();

		statusLine = line;

		if (line == null || !line.startsWith("HTTP/1.1 "))
			throw new HttpException("Invalid status line", statusLine);

		line = line.substring(9);

		responseCode = line.substring(0, 3);

		if (!responseCode.equals("200"))
			throw new HttpException("Server returned an error", statusLine);

		//--- read the rest of the header

		while (!line.equals(""))
			line = handleLine(input.readLine());

		//--- read the content

		StringBuffer sb = new StringBuffer();

		line = input.readLine();

		while (line != null)
		{
			sb.append(line +"\n");
			line = input.readLine();
		}

		responseData = sb.toString();
	}

	//--------------------------------------------------------------------------

	private String handleLine(String line)
	{
		if (line.toLowerCase().startsWith("set-cookie: "))
			cookie = line.substring("set-cookie: ".length());

		return line;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Protected methods
	//---
	//--------------------------------------------------------------------------

	protected abstract String getMethod();
	protected abstract String getStartLineParams() throws UnsupportedEncodingException;

	protected abstract void   sendExtraHeaders(OutputStream os) throws IOException;
	protected abstract void   sendContent     (OutputStream os) throws IOException;
}

//=============================================================================


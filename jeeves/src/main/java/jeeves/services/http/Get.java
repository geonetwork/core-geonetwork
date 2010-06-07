//=============================================================================

package jeeves.services.http;

import java.util.*;

import org.jdom.*;

import jeeves.constants.*;
import jeeves.interfaces.*;
import jeeves.server.*;
import jeeves.server.context.ServiceContext;
import jeeves.utils.*;

import java.net.*;
import java.io.*;

//=============================================================================

/** Returns a specific record given its id
  */

public class Get implements Service
{
	public static final String URL_PARAM_NAME = "url";

	private static final int BUFSIZE = 1024;

	private String configUrl;

	//--------------------------------------------------------------------------
	//---
	//--- Init
	//---
	//--------------------------------------------------------------------------

	public void init(String appPath, ServiceConfig params) throws Exception
	{
		configUrl = params.getValue(URL_PARAM_NAME);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// read url
		String sUrl = params.getChildText(URL_PARAM_NAME);
		if (sUrl == null) sUrl = configUrl;
		if (sUrl == null) throw new IllegalArgumentException("The '" + URL_PARAM_NAME + "' configuration parameter is missing");

		// add other parameters to HTTP request
		boolean first = new URL(sUrl).getQuery() == null;
		StringBuffer sb = new StringBuffer(sUrl);
		for (Iterator iter = params.getChildren().iterator(); iter.hasNext(); )
		{
			Element child = (Element)iter.next();

			// skip the url parameter
			if (child.getName().equals(URL_PARAM_NAME)) continue;

			if (first)
			{
				first = false;
				sb.append("?");
			}
			else sb.append("&");
			sb.append(child.getName()).append("=").append(URLEncoder.encode(child.getText(), "UTF-8"));
		}
		URL url = new URL(sb.toString());
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		InputStream input = conn.getInputStream();

		return Xml.loadStream(input);

		/* FIXME: use this instead of previous line to dump HTTP response body
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte buffer[] = new byte[BUFSIZE];
		for (int nRead; (nRead = input.read(buffer, 0, BUFSIZE)) > 0; )
			output.write(buffer, 0, nRead);
		output.close();
		conn.disconnect();
		String sResult = output.toString();
		System.out.println("HTTP response body:\n" + sResult);
		Element result = Xml.loadString(sResult, false); // do not validate
		return result;
		*/
	}
}


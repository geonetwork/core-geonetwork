//=============================================================================

package jeeves.services.http;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Constants;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

//=============================================================================

/** Returns a specific record given its id
  */

public class Get implements Service
{
	public static final String URL_PARAM_NAME = "url";

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

	@SuppressWarnings("unchecked")
	public Element exec(Element params, ServiceContext context) throws Exception
	{
		// read url
		String sUrl = params.getChildText(URL_PARAM_NAME);
		if (sUrl == null) sUrl = configUrl;
		if (sUrl == null) throw new IllegalArgumentException("The '" + URL_PARAM_NAME + "' configuration parameter is missing");

		// add other parameters to HTTP request
		boolean first = new URL(sUrl).getQuery() == null;
		StringBuffer sb = new StringBuffer(sUrl);
		for (Element child : (List<Element>) params.getChildren()) {
			// skip the url parameter
			if (child.getName().equals(URL_PARAM_NAME)) continue;

			if (first) {
				first = false;
				sb.append('?');
			} else {
				sb.append('&');
			}
			sb.append(child.getName()).append('=').append(URLEncoder.encode(child.getText(), Constants.ENCODING));
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


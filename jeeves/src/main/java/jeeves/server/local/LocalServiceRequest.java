package jeeves.server.local;

import jeeves.server.sources.ServiceRequest;
import jeeves.utils.StringBufferOutputStream;
import jeeves.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Represents a Jeeves local XML request (within JVM).
 * <p/>
 * In normal operation Jeeves is invoked through HTTP (HttpServlet).
 * For a LocalJeeves engine a LocalServiceRequest can be used to
 * do direct Jeeves service dispatch.
 *
 * @author Just van den Broecke - just@justobjects.nl
 */
public class LocalServiceRequest extends ServiceRequest
{
	private static final Element NULL_PARAMS = new Element("request");
	private StringBuffer outputBuffer = new StringBuffer(128);

	//---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

	public LocalServiceRequest()
	{
	}

	public static LocalServiceRequest create(String url) throws Exception
	{
		return create(url, null);
	}

	public static LocalServiceRequest create(String url, Element params) throws Exception
	{
		LocalServiceRequest srvReq = new LocalServiceRequest();

		srvReq.setDebug(true);
		srvReq.setLanguage(extractLanguage(url));
		srvReq.setService(extractService(url));
		srvReq.setAddress("127.0.0.1");
		srvReq.setOutputStream(new StringBufferOutputStream(srvReq.outputBuffer));
		srvReq.setInputMethod(InputMethod.XML);
		srvReq.setOutputMethod(OutputMethod.XML);
		if (params == null)
		{
			params = extractParams(url);
		}
		srvReq.setParams(params);

		return srvReq;
	}

	//---------------------------------------------------------------------------

	public void beginStream(String contentType, boolean cache)
	{
		beginStream(contentType, -1, null, cache);
	}

	//---------------------------------------------------------------------------

	public void beginStream(String contentType, int contentLength,
	                        String contentDisposition, boolean cache)
	{
	}

	//---------------------------------------------------------------------------

	public void endStream() throws IOException
	{
	}

	public String getResultString()
	{
		String string = outputBuffer.toString().trim();
		if(string.matches("<\\?xml\\s+version=[^>]+\\s+encoding=[^>]+\\?>")) {
			return "";
		}
		return string;
	}

	public Element getResult() throws IOException, JDOMException
	{
		String resultString = getResultString();
		if(resultString.trim().isEmpty()) return null;
		else return Xml.loadString(resultString, false);
	}

	//---------------------------------------------------------------------------
	/**
	 * Extracts the language code from the url.
	 *
	 * @param url service url like /eng/...
	 * @return the language e.g. "eng"
	 */
	private static String extractLanguage(String url)
	{
		if (url == null)
		{
			return null;
		}

		int indexOfParams = url.indexOf('?');
        if(indexOfParams > -1) {
		    url = url.substring(0,indexOfParams);
		}
		if (url.contains("://")) {
			url = url.substring(url.indexOf("://")+3);
		} else if (url.startsWith("/")){
			url = url.substring(1);
		}

		int pos = url.indexOf('/');

		if (pos == -1)
		{
			return null;
		}

		return url.substring(0, pos);
	}

	//---------------------------------------------------------------------------
	/**
	 * Extracts the service name from the url
	 */
	private static String extractService(String url)
	{
		if (url == null)
		{
			return null;
		}

		// Chop off query string when present
		url = url.split("\\?")[0];

		if (url.endsWith("!"))
		{
			url = url.substring(0, url.length() - 1);
		}

		int pos = url.lastIndexOf('/');

		if (pos == -1)
		{
			return url;
		}

		return url.substring(pos + 1);
	}

	//---------------------------------------------------------------------------
	/**
	 * Extracts the service params from the url
	 */
	private static Element extractParams(String url)
	{
		if (url == null)
		{
			return NULL_PARAMS;
		}

		String[] urlParts = url.split("\\?");
		if (urlParts.length != 2)
		{
			return NULL_PARAMS;
		}

		String queryPart = urlParts[1];

		String[] nvPairs = queryPart.split("\\&");
		Element result = new Element("request");
		String[] nvPair;
		Element param;
		String name, value;
		for (int i = 0; i < nvPairs.length; i++)
		{
			nvPair = nvPairs[i].split("\\=");
			name = nvPair[0];
			if(nvPair.length == 2) {
				value = nvPair[1];
			} else {
				value = "";
			}
			if (name.startsWith("@"))
			{
				// Some params should become attrs (indicated with @)
				name = name.substring(name.indexOf('@') + 1);
				result.setAttribute(name, value);
			} else
			{
				param = new Element(name);
				try {
					param.setText(URLDecoder.decode(value, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					param.setText(value);
				}
				result.addContent(param);
			}
		}
		return result;
	}
}


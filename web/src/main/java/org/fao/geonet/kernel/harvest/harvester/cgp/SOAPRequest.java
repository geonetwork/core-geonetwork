package org.fao.geonet.kernel.harvest.harvester.cgp;

import jeeves.exceptions.BadSoapResponseEx;
import jeeves.exceptions.BadXmlResponseEx;
import jeeves.utils.Xml;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * SOAP request wrapper.
 * Note: could not use Jeeves Xmlrequest since we need to set SOAP header as well.
 */
public class SOAPRequest
{
	public static final Namespace NAMESPACE_ENV = Namespace.getNamespace("env", "http://schemas.xmlsoap.org/soap/envelope/");

	public SOAPRequest(String urlStr) throws MalformedURLException
	{
		url = new URL(urlStr);
	}

	/**
	 * Sends a request and obtains an xml response.
	 */
	public Document execute() throws SOAPFaultEx, JDOMException, IOException, BadXmlResponseEx, BadSoapResponseEx
	{
		HttpMethodBase httpMethod = createHttpMethod();

		config.setHost(url.getHost(), url.getPort());

		if (useProxy)
		{
			config.setProxy(proxyHost, proxyPort);
		}

		client.setHostConfiguration(config);

		// byte[] data = null;
		Document responseDoc = null;
		try
		{
			client.executeMethod(httpMethod);

			// Better not with large queries
			// data = httpMethod.getResponseBody();
			//String s = new String(data);
			SAXBuilder builder = new SAXBuilder();

			// KLUDGE 9.apr.09 Just: ws.geoportal.ch appears to have an encoding that
			// the standard SAX parser fails upon. For now we convert
			// the byte input to a char stream.
			if (url.getHost().indexOf("geoportal.ch") != -1)  {
				responseDoc = builder.build(new InputStreamReader(httpMethod.getResponseBodyAsStream()));
			} else {
				responseDoc = builder.build(httpMethod.getResponseBodyAsStream());
			}
		}
		catch (JDOMException e)
		{
			throw new BadXmlResponseEx("Parse error: " +  e.getMessage());
		}
		finally
		{
			httpMethod.releaseConnection();
		}

		// Check for SOAP errors and Faults
		Element rootElm = responseDoc.getRootElement();
		if (rootElm == null)
		{
			throw new BadSoapResponseEx(rootElm);
		}

		Element bodyElm = rootElm.getChild("Body", NAMESPACE_ENV);
		if (bodyElm == null)
		{
			throw new BadSoapResponseEx(rootElm);
		}

		// Valid Body element: check if it contains a Fault elm
		Element faultElm = bodyElm.getChild("Fault", NAMESPACE_ENV);
		if (faultElm != null)
		{
			throw new SOAPFaultEx(faultElm);
		}

		return responseDoc;
	}

	public String getHost()
	{
		return this.url.getHost();
	}

	public void setBodyContent(Element bodyContentElm) throws UnsupportedEncodingException
	{
		this.bodyContentElm = bodyContentElm;
	}

	public void setHeaderContent(Element headerContentElm) throws UnsupportedEncodingException
	{
		this.headerContentElm = headerContentElm;
	}

	//---------------------------------------------------------------------------

	private HttpMethodBase createHttpMethod() throws UnsupportedEncodingException
	{
		PostMethod postMethod = new PostMethod();
		String postData = Xml.getString(getSOAPDocument());

		postMethod.setRequestEntity(new StringRequestEntity(postData, "application/soap+xml", "UTF8"));
		postMethod.setPath(url.getPath());
		postMethod.setDoAuthentication(false);

		return postMethod;
	}

	public Document getSOAPDocument()
	{
		Element envelopeElm = new Element("Envelope", NAMESPACE_ENV);

		if (headerContentElm != null)
		{
			Element headerElm = new Element("Header", NAMESPACE_ENV);
			headerElm.addContent(headerContentElm);
			envelopeElm.addContent(headerElm);
		}

		if (bodyContentElm != null)
		{
			Element bodyElm = new Element("Body", NAMESPACE_ENV);
			bodyElm.addContent(bodyContentElm);
			envelopeElm.addContent(bodyElm);
		}

		return new Document(envelopeElm);
	}

	//---------------------------------------------------------------------------

	public void setUseProxy(boolean yesno)
	{
		useProxy = yesno;
	}

	//---------------------------------------------------------------------------

	public void setProxyHost(String host)
	{
		proxyHost = host;
	}

	//---------------------------------------------------------------------------

	public void setProxyPort(int port)
	{
		proxyPort = port;
	}

	//---------------------------------------------------------------------------

	public void setProxyCredentials(String username, String password)
	{
		if (username == null || username.trim().length() == 0)
		{
			return;
		}

		Credentials cred = new UsernamePasswordCredentials(username, password);
		AuthScope scope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM);

		client.getState().setProxyCredentials(scope, cred);
	}

	//---------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//---------------------------------------------------------------------------
	private HttpClient client = new HttpClient();
	private HostConfiguration config = new HostConfiguration();
	private URL url;
	private Element bodyContentElm;
	private Element headerContentElm;
	private boolean useProxy;
	private String proxyHost;
	private int proxyPort;
}

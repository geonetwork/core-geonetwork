
package org.fao.geonet.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.fao.geonet.exceptions.BadServerResponseEx;
import org.fao.geonet.exceptions.BadSoapResponseEx;
import org.fao.geonet.exceptions.BadXmlResponseEx;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

//=============================================================================

public class XmlRequest extends AbstractHttpRequest {

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------

    //--- transient vars

    //---------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//---------------------------------------------------------------------------

    XmlRequest(String host, int port, String protocol, GeonetHttpRequestFactory requestFactory)
	{
        super(protocol, host, port, requestFactory);

        setMethod(Method.GET);
	}

	/** Sends an xml request and obtains an xml response */

	public final Element execute(Element request) throws IOException, BadXmlResponseEx, BadSoapResponseEx
	{
		setRequest(request);
		return execute();
	}

	//---------------------------------------------------------------------------
	/** Sends a request and obtains an xml response. The request can be a GET or a
	  * POST depending on the method used to set parameters. Calls to the 'addParam'
	  * method set a GET request while the setRequest method sets a POST/xml request.
	  */

	public final Element execute() throws IOException, BadXmlResponseEx, BadSoapResponseEx
	{
        HttpRequestBase httpMethod = setupHttpMethod();

		Element response = executeAndReadResponse(httpMethod);

		if (useSOAP) {
            response = soapUnembed(response);
        }

		return response;
	}

	//---------------------------------------------------------------------------
	/** Sends a request (using GET or POST) and save the content to a file. This
	  * method does not store received data.
     * @param outFile
     */

	public final void executeLarge(Path outFile) throws IOException
	{
        HttpRequestBase httpMethod = setupHttpMethod();

		doExecuteLarge(httpMethod, outFile);
	}

	//---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//---------------------------------------------------------------------------

	protected final Element executeAndReadResponse(HttpRequestBase httpMethod) throws IOException, BadXmlResponseEx
	{


        final ClientHttpResponse httpResponse = doExecute(httpMethod);

        if (httpResponse.getRawStatusCode() > 399) {
            throw new BadServerResponseEx(httpResponse.getStatusText() + 
                    " -- URI: " + httpMethod.getURI() +
                    " -- Response Code: " + httpResponse.getRawStatusCode());
        }

        byte[] data = null;

		try {
		    data = IOUtils.toByteArray(httpResponse.getBody());
			return Xml.loadStream(new ByteArrayInputStream(data));
		}

		catch(JDOMException e)
		{
			throw new BadXmlResponseEx("Response: '" + new String(data, "UTF8") + "' (from URI " + httpMethod.getURI() + ")");
		}

		finally
		{
			httpMethod.releaseConnection();

			sentData     = getSentData(httpMethod);
		}
	}

    //---------------------------------------------------------------------------

	protected final Path doExecuteLarge(HttpRequestBase httpMethod, Path outFile) throws IOException
	{

		try (ClientHttpResponse httpResponse = doExecute(httpMethod)) {
            Files.copy(httpResponse.getBody(), outFile, StandardCopyOption.REPLACE_EXISTING);
            return outFile;
		} finally {
			httpMethod.releaseConnection();

			sentData = getSentData(httpMethod);
			//--- we do not save received data because it can be very large
		}
	}

}

//=============================================================================


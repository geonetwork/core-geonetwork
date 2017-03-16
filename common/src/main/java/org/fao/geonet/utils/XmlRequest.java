/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

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

    XmlRequest(String host, int port, String protocol, GeonetHttpRequestFactory requestFactory) {
        super(protocol, host, port, requestFactory);

        setMethod(Method.GET);
    }

    /**
     * Sends an xml request and obtains an xml response
     */

    public final Element execute(Element request) throws IOException, BadXmlResponseEx, BadSoapResponseEx {
        setRequest(request);
        return execute();
    }

    //---------------------------------------------------------------------------

    /**
     * Sends a request and obtains an xml response. The request can be a GET or a POST depending on
     * the method used to set parameters. Calls to the 'addParam' method set a GET request while the
     * setRequest method sets a POST/xml request.
     */

    public final Element execute() throws IOException, BadXmlResponseEx, BadSoapResponseEx {
        HttpRequestBase httpMethod = setupHttpMethod();

        Element response = executeAndReadResponse(httpMethod);

        if (useSOAP) {
            response = soapUnembed(response);
        }

        return response;
    }

    //---------------------------------------------------------------------------

    /**
     * Sends a request (using GET or POST) and save the content to a file. This method does not
     * store received data.
     */

    public final void executeLarge(Path outFile) throws IOException {
        HttpRequestBase httpMethod = setupHttpMethod();

        doExecuteLarge(httpMethod, outFile);
    }

    //---------------------------------------------------------------------------

    //---------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //---------------------------------------------------------------------------

    protected final Element executeAndReadResponse(HttpRequestBase httpMethod) throws IOException, BadXmlResponseEx {


        final ClientHttpResponse httpResponse = doExecute(httpMethod);

        if (httpResponse.getRawStatusCode() > 399) {
            httpMethod.releaseConnection();
            throw new BadServerResponseEx(httpResponse.getStatusText() +
                " -- URI: " + httpMethod.getURI() +
                " -- Response Code: " + httpResponse.getRawStatusCode());
        }

        byte[] data = null;

        try {
            data = IOUtils.toByteArray(httpResponse.getBody());
            return Xml.loadStream(new ByteArrayInputStream(data));
        } catch (JDOMException e) {
            throw new BadXmlResponseEx("Response: '" + new String(data, "UTF8") + "' (from URI " + httpMethod.getURI() + ")");
        } finally {
            httpMethod.releaseConnection();

            sentData = getSentData(httpMethod);
        }
    }

    //---------------------------------------------------------------------------

    protected final Path doExecuteLarge(HttpRequestBase httpMethod, Path outFile) throws IOException {

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


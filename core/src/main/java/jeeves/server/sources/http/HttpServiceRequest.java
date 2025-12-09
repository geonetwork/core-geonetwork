//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package jeeves.server.sources.http;

import jeeves.server.sources.ServiceRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

//=============================================================================

/**
 * Represents a request from tomcat (ie http)
 */

public class HttpServiceRequest extends ServiceRequest {
    private HttpServletResponse httpRes;

    //---------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //---------------------------------------------------------------------------

    public HttpServiceRequest(HttpServletResponse res) {
        httpRes = res;
    }

    //---------------------------------------------------------------------------

    @Override
    public void beginStream(String contentType, boolean cache) {
        beginStream(contentType, -1, null, cache);
    }

    //---------------------------------------------------------------------------

    @Override
    public void beginStream(String contentType, long contentLength,
                            String contentDisposition, boolean cache) {
        httpRes.setStatus(statusCode);

        if (contentType != null) {
            httpRes.setContentType(contentType);
        }

        if (contentLength != -1) {
            httpRes.setContentLengthLong(contentLength);
        }

        if (contentDisposition != null) {
            httpRes.addHeader("Content-disposition", contentDisposition);
        } else {
            //--- this else is needed by IExplorer6
            //--- maybe we can use the <meta> tag instead of these lines

            if (!cache) {
                httpRes.addHeader("Pragma", "no-cache");
                httpRes.addHeader("Cache-Control", "no-cache");
                httpRes.addHeader("Expires", "-1");
            }
        }
    }

    //---------------------------------------------------------------------------

    @Override
    public void endStream() throws IOException {
        httpRes.flushBuffer();
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpRes;
    }
}

//=============================================================================


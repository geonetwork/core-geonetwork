//=============================================================================
//===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package jeeves.server.sources;

import org.fao.geonet.exceptions.BadParameterEx;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import static org.junit.Assert.assertEquals;

public class ServiceRequestFactoryTest {

    private static MockMultipartHttpServletRequest multipartPost() {
        MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data; boundary=test");
        return request;
    }

    @Test(expected = BadParameterEx.class)
    public void rejectsMultipartFileParts() throws Exception {
        MockMultipartHttpServletRequest request = multipartPost();
        request.addFile(new MockMultipartFile("file", "report.xml", "text/xml", "<a/>".getBytes()));

        ServiceRequestFactory.create(request, new MockHttpServletResponse(), "srv", "eng", "info");
    }

    @Test
    public void ignoresEmptyFileParts() throws Exception {
        // a file input left blank in a form is sent as an empty part with no file name
        MockMultipartHttpServletRequest request = multipartPost();
        request.addFile(new MockMultipartFile("file", "", "application/octet-stream", new byte[0]));
        request.addParameter("type", "site");

        ServiceRequest srvReq = ServiceRequestFactory.create(request, new MockHttpServletResponse(), "srv", "eng", "info");

        assertEquals("site", srvReq.getParams().getChildText("type"));
    }

    @Test
    public void keepsMultipartFormFields() throws Exception {
        MockMultipartHttpServletRequest request = multipartPost();
        request.addParameter("type", "site");

        ServiceRequest srvReq = ServiceRequestFactory.create(request, new MockHttpServletResponse(), "srv", "eng", "info");

        assertEquals("site", srvReq.getParams().getChildText("type"));
    }
}

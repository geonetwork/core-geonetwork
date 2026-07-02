/*
 * Copyright (C) 2001-2026 Food and Agriculture Organization of the
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

package org.fao.geonet.api.es;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class EsResponseContentTypeValidatorTest {

    @Mock
    private HttpURLConnection connection;

    @Mock
    private HttpServletResponse response;

    private EsResponseContentTypeValidator validator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        validator = new EsResponseContentTypeValidator();
    }

    @Test
    public void validateContentType_ShouldReturnError_WhenContentTypeIsNull() throws IOException {
        validator.validateContentType(connection, response, null);

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), contains("content type given by remote host is null"));
    }

    @Test
    public void validateContentType_ShouldDoNothing_WhenContentTypeIsValid() throws IOException {
        validator.validateContentType(connection, response, "application/json");
        validator.validateContentType(connection, response, "text/plain");

        verifyNoInteractions(response);
    }

    @Test
    public void validateContentType_ShouldReturnNotFound_WhenContentTypeIsInvalidAndResponseMessageIsNotFound() throws IOException {
        when(connection.getResponseMessage()).thenReturn("Not Found");

        validator.validateContentType(connection, response, "text/html");

        verify(response).sendError(eq(HttpServletResponse.SC_NOT_FOUND), eq("Remote host not found"));
    }

    @Test
    public void validateContentType_ShouldReturnForbidden_WhenContentTypeIsInvalidAndResponseMessageIsNotNotFound() throws IOException {
        when(connection.getResponseMessage()).thenReturn("Internal Server Error");

        validator.validateContentType(connection, response, "text/html");

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), contains("is not allowed by the proxy rules"));
    }

    @Test
    public void validateContentType_ShouldReturnForbidden_WhenContentTypeIsInvalidAndResponseMessageIsNull() throws IOException {
        when(connection.getResponseMessage()).thenReturn(null);

        validator.validateContentType(connection, response, "text/html");

        verify(response).sendError(eq(HttpServletResponse.SC_FORBIDDEN), contains("is not allowed by the proxy rules"));
    }

    @Test
    public void isContentTypeValid_ShouldReturnTrue_ForValidContentTypes() {
        assertTrue(validator.isContentTypeValid("application/json"));
        assertTrue(validator.isContentTypeValid("APPLICATION/JSON"));
        assertTrue(validator.isContentTypeValid("text/plain"));
        assertTrue(validator.isContentTypeValid("TEXT/PLAIN"));
    }

    @Test
    public void isContentTypeValid_ShouldReturnTrue_ForValidContentTypesWithEncoding() {
        assertTrue(validator.isContentTypeValid("application/json;charset=UTF-8"));
        assertTrue(validator.isContentTypeValid("application/json; charset=UTF-8"));
        assertTrue(validator.isContentTypeValid("text/plain; charset=iso-8859-1"));
        assertTrue(validator.isContentTypeValid("text/plain ; charset=iso-8859-1"));
    }

    @Test
    public void isContentTypeValid_ShouldReturnFalse_ForInvalidContentTypes() {
        assertFalse(validator.isContentTypeValid("text/html"));
        assertFalse(validator.isContentTypeValid("application/xml"));
        assertFalse(validator.isContentTypeValid(""));
    }
}

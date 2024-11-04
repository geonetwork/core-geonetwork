/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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
package org.fao.geonet.security.web.csrf;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.adapters.springsecurity.filter.KeycloakCsrfRequestMatcher;
import org.keycloak.constants.AdapterConstants;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeonetworkCsrfSecurityRequestMatcherTest {

    private static final String ROOT_CONTEXT_PATH = "";
    private static final String SUB_CONTEXT_PATH = "/foo";

    private GeonetworkCsrfSecurityRequestMatcher matcher;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        request = new MockHttpServletRequest();
        Set<String> notCorsPaths = Sets.newLinkedHashSet(Lists.newArrayList(
            "/[a-zA-Z0-9_\\-]+/[a-z]{2,3}/csw-publication!?.*",
            "/[a-zA-Z0-9_\\-]+/[a-z]{2,3}/csw-.*",
            "/[a-zA-Z0-9_\\-]+/[a-z]{2,3}/csw!?.*",
            "/[a-zA-Z0-9_\\-]+/api/search/.*",
            "/[a-zA-Z0-9_\\-]+/api/site")
        );
        matcher = new GeonetworkCsrfSecurityRequestMatcher(notCorsPaths);
    }

    @Test
    public void testDefaultConfigHttpMethods() {

        request.setMethod(HttpMethod.GET.name());
        assertFalse((matcher.matches(request)));

        request.setMethod(HttpMethod.HEAD.name());
        assertFalse((matcher.matches(request)));
        request.setMethod(HttpMethod.TRACE.name());
        assertFalse((matcher.matches(request)));
        request.setMethod(HttpMethod.OPTIONS.name());
        assertFalse((matcher.matches(request)));

        request.setMethod(HttpMethod.PATCH.name());
        assertTrue(matcher.matches(request));
        request.setMethod(HttpMethod.POST.name());
        assertTrue(matcher.matches(request));
        request.setMethod(HttpMethod.PUT.name());
        assertTrue(matcher.matches(request));
        request.setMethod(HttpMethod.DELETE.name());
        assertTrue(matcher.matches(request));
    }

    @Test
    public void testDefaultMatchesMethodPost() throws Exception {
        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "some/random/uri");
        assertTrue(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "some/random/uri");
        assertTrue(matcher.matches(request));

        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "some/random/uri");
        assertTrue(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "some/random/uri");
        assertTrue(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "srv/eng/csw-publication");
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "srv/eng/csw-publication");
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "srv/eng/csw-foo");
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "srv/eng/csw-foo");
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "srv/eng/csw");
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "srv/eng/csw");
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "srv/api/search/foo");
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "srv/api/search/foo");
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "srv/api/site");
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "srv/api/site");
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "k_logout");
        assertTrue(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "k_logout");
        assertTrue(matcher.matches(request));
    }

    @Test
    public void testKeycloakCorsFilter() {
        matcher.addRequestMatcher(new KeycloakCsrfRequestMatcher());

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "some/random/uri");
        assertTrue(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "some/random/uri");
        assertTrue(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "srv/eng/csw-publication");
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "srv/eng/csw-publication");
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "srv/eng/csw-foo");
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "srv/eng/csw-foo");
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "srv/eng/csw");
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "srv/eng/csw");
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "srv/api/search/foo");
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "srv/api/search/foo");
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, "srv/api/site");
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, "srv/api/site");
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, AdapterConstants.K_LOGOUT);
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, AdapterConstants.K_LOGOUT);
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, AdapterConstants.K_PUSH_NOT_BEFORE);
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, AdapterConstants.K_PUSH_NOT_BEFORE);
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, AdapterConstants.K_QUERY_BEARER_TOKEN);
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, AdapterConstants.K_QUERY_BEARER_TOKEN);
        assertFalse(matcher.matches(request));

        prepareRequest(HttpMethod.POST, ROOT_CONTEXT_PATH, AdapterConstants.K_TEST_AVAILABLE);
        assertFalse(matcher.matches(request));
        prepareRequest(HttpMethod.POST, SUB_CONTEXT_PATH, AdapterConstants.K_TEST_AVAILABLE);
        assertFalse(matcher.matches(request));

    }

    private void prepareRequest(HttpMethod method, String contextPath, String uri) {
        request.setMethod(method.name());
        request.setContextPath(contextPath);
        request.setRequestURI(contextPath + "/" + uri);
        request.setServletPath("/" + uri);
    }
}

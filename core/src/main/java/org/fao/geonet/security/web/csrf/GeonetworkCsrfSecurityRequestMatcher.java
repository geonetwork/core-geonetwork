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

import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * RequestMatcher to exclude the CSRF token from requests.
 * <p>
 * Useful to exclude CSW POST requests for GetRecords.
 *
 * @author Jose García.
 */
public class GeonetworkCsrfSecurityRequestMatcher implements RequestMatcher {
    private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
    private RegexRequestMatcher unprotectedMatcher;
    private Set<RequestMatcher> otherMatchers = new LinkedHashSet<>();

    public GeonetworkCsrfSecurityRequestMatcher(Set<String> unprotectedUrlPatterns) {
        unprotectedMatcher = new RegexRequestMatcher(String.join("|", unprotectedUrlPatterns), null);
    }

    /**
     * Adds additional RequestMatchers used if the list of unprotectedUrlPatters don't match. The check is done in the
     * order in what the matchers have been added.
     * The matcher must be negative, that's it, it will return false for the patterns that match the patterns.
     *
     * @param matcher
     */
    public void addRequestMatcher(RequestMatcher... matcher) {
        Assert.notNull(matcher, "To add additional matchers the parameter matcher cannot be null");
        otherMatchers.addAll(Arrays.asList(matcher));
    }

    /**
     * Return {@code}true{@code} if the request doesn't match the methods and patterns defined.
     *
     * @param request the request to check for a match
     * @return
     */
    @Override
    public boolean matches(HttpServletRequest request) {
        boolean result = true;
        if (allowedMethods.matcher(request.getMethod()).matches()) {
            result = false;
        }
        if (result) {
            result = !unprotectedMatcher.matches(request);
        }

        if (result && !otherMatchers.isEmpty()) {
            result = otherMatchers.stream().anyMatch(matcher -> matcher.matches(request));
        }
        return result;
    }
}
